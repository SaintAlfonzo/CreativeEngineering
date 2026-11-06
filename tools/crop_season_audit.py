#!/usr/bin/env python3
"""Crop growth season audit: every growing plant in the pack, its crop season type (pack: crop_seasons.json,
Creative Calendar seasons/CropSeasons, crops.md 2.12.1), and whether its growth fires Forge's crop-grow event at all
(a plant that never fires it ignores its type and grows at its own speed all year).

    python3 tools/crop_season_audit.py

Reads (kubejs/exported/, refresh with /dumpall):
  crops/crop_growth_audit.json   /dumpcropgrowth: every block with an age-style property or a crop class,
                                 its class chain and its item (`item`; dumps older than 2026-10-30 lack it,
                                 FALLBACK_ITEMS covers those)
  tags/block_tags.json           sereneseasons:<season>_crops block tags (the mods' own seasons, inert while
                                 fertility.toml seasonal_crops is off; shown for reference)
  items_by_mod/*.json            item ids, to check the planting item exists
and kubejs/generation_scripts/worldgen/crop_seasons.json (the types; read directly, so no dump is needed after
editing it), and the class files of mods/*.jar (with their META-INF/jarjar jars), scanned for
ForgeHooks.onCropsGrowPre (PACK_HOOKS: classes Creative Calendar's CropSeasonGrowHookMixin adds it to).

Writes:
  feature_writeup/crop_seasons_audit.md   the table and the findings (design: briefs/farming_crops.md crops #5)

Crop seasons: each season is fertile (+25 %), neutral or off (-25 %) for the crop's type; an untyped crop grows
at its own speed all year.
"""
import collections
import glob
import io
import json
import os
import re
import sys
import zipfile
from datetime import date

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EXP = os.path.join(ROOT, 'kubejs', 'exported')
OUT_MD = os.path.join(ROOT, 'feature_writeup', 'crop_seasons_audit.md')
CROP_SEASONS = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'worldgen', 'crop_seasons.json')
SEASONS = ['spring', 'summer', 'autumn', 'winter']
ABBR = {'spring': 'Sp', 'summer': 'Su', 'autumn': 'Au', 'winter': 'Wi'}

# Vanilla classes whose growth Forge 1.19.2 patches to fire onCropsGrowPre.
VANILLA_EVENT = {
    'CropBlock', 'StemBlock', 'CocoaBlock', 'SugarCaneBlock', 'CactusBlock', 'SweetBerryBushBlock',
    'NetherWartBlock', 'BambooBlock', 'BambooSaplingBlock', 'GrowingPlantHeadBlock', 'ChorusFlowerBlock',
}
# Classes Creative Calendar mixin/CropSeasonGrowHookMixin makes fire the event (their tick grows the plant).
PACK_HOOKS = {
    'blusunrize.immersiveengineering.common.blocks.plant.HempBlock',
    'net.mcreator.thedirtystuff.block.TobaccoSeedsBlock',
    'umpaz.nethersdelight.common.block.FungusColonyBlock',
}
# Plants with no growth of their own (bone meal only): no grow event is needed.
BONEMEAL_ONLY = {'ends_delight:chorus_succulent', 'minecraft:sea_pickle'}
# Blocks that grow but /dumpcropgrowth misses (no age-named property and no crop class), or that only
# grow through pack code: id -> (planting item or None, growing class or None for pack code, note).
EXTRA = {
    'neapolitan:vanilla_vine': ('neapolitan:vanilla_pods', 'com.teamabnormals.neapolitan.common.block.VanillaVineTopBlock', None),
    'farmersrespite:small_tea_bush': (None, 'umpaz.farmersrespite.common.block.SmallTeaBushBlock', None),
    'farmersrespite:coffee_bush': (None, 'umpaz.farmersrespite.common.block.CoffeeBushBlock', None),
    'minecraft:attached_melon_stem': (None, None, 'extra fruit (patcherMod StemExtraFruit) fires the event'),
    'minecraft:attached_pumpkin_stem': (None, None, 'extra fruit (patcherMod StemExtraFruit) fires the event'),
    'fruitsdelight:attached_hamimelon_stem': (None, None, 'extra fruit (patcherMod StemExtraFruit) fires the event'),
}
# Planting items for dumps written before /dumpcropgrowth recorded `item` (checked by hand 2026-10-30).
FALLBACK_ITEMS = {
    'minecraft:wheat': 'minecraft:wheat_seeds', 'minecraft:carrots': 'minecraft:carrot',
    'minecraft:potatoes': 'minecraft:potato', 'minecraft:beetroots': 'minecraft:beetroot_seeds',
    'minecraft:melon_stem': 'minecraft:melon_seeds', 'minecraft:pumpkin_stem': 'minecraft:pumpkin_seeds',
    'minecraft:cocoa': 'minecraft:cocoa_beans', 'minecraft:sweet_berry_bush': 'minecraft:sweet_berries',
    'minecraft:cave_vines': 'minecraft:glow_berries', 'farmersdelight:cabbages': 'farmersdelight:cabbage_seeds',
    'farmersdelight:onions': 'farmersdelight:onion', 'farmersdelight:rice': 'farmersdelight:rice',
    'farmersdelight:budding_tomatoes': 'farmersdelight:tomato_seeds',
    'corn_delight:corn_crop': 'corn_delight:corn_seeds', 'delightful:salmonberry_bush': 'delightful:salmonberry_pips',
    'delightful:cantaloupe_plant': 'delightful:cantaloupe_seeds', 'farmersrespite:tea_bush': 'farmersrespite:tea_seeds',
    'farmersrespite:coffee_stem': 'farmersrespite:coffee_beans',
    'rusticdelight:bell_peppers': 'rusticdelight:bell_pepper_seeds',
    'rusticdelight:dark_bell_peppers': 'rusticdelight:dark_bell_pepper_seeds',
    'rusticdelight:pale_bell_peppers': 'rusticdelight:pale_bell_pepper_seeds',
    'rusticdelight:coffee': 'rusticdelight:coffee_beans', 'rusticdelight:cotton': 'rusticdelight:cotton_seeds',
    'supplementaries:flax': 'supplementaries:flax_seeds', 'immersiveengineering:hemp': 'immersiveengineering:seed',
    'the_dirty_stuff:tobacco_seeds': 'the_dirty_stuff:tobacco_seeds',
    'neapolitan:strawberry_bush': 'neapolitan:strawberry_pips', 'neapolitan:adzuki_sprouts': 'neapolitan:adzuki_beans',
    'neapolitan:mint': 'neapolitan:mint_sprout', 'upgrade_aquatic:mulberry_vine': 'upgrade_aquatic:mulberry',
    'oceanic_delight:sea_grape_plant': 'oceanic_delight:sea_grape',
    'miners_delight:cave_carrots': 'miners_delight:cave_carrot', 'endercrop:ender_crop': 'endercrop:ender_seeds',
    'veggiesdelight:broccoli_crop': 'veggiesdelight:broccoli_seeds',
    'veggiesdelight:cauliflower_crop': 'veggiesdelight:cauliflower_seeds',
    'veggiesdelight:garlic_crop': 'veggiesdelight:garlic_clove',
    'veggiesdelight:sweet_potato_crop': 'veggiesdelight:sweet_potato',
    'veggiesdelight:turnip_crop': 'veggiesdelight:turnip_seeds',
    'veggiesdelight:zucchini_crop': 'veggiesdelight:zucchini_seeds',
    'vintagedelight:cucumber_crop': 'vintagedelight:cucumber_seeds',
    'vintagedelight:ghost_pepper_crop': 'vintagedelight:ghost_pepper_seeds',
    'vintagedelight:oat_crop': 'vintagedelight:oat_seeds', 'vintagedelight:peanut_crop': 'vintagedelight:peanut',
    'fruitsdelight:blueberry_bush': 'fruitsdelight:blueberry', 'fruitsdelight:cranberry_bush': 'fruitsdelight:cranberry',
    'fruitsdelight:hamimelon_stem': 'fruitsdelight:hamimelon_seeds', 'fruitsdelight:lemon_tree': 'fruitsdelight:lemon_seeds',
    'fruitsdelight:pineapple': 'fruitsdelight:pineapple_sapling',
}
# Dump rows that are not plants a player farms (decor, weathering, saplings, flower patches, effects).
EXCLUDE_MODS = {'immersive_weathering', 'flowerpatch', 'beautify', 'everycomp', 'alexsmobs', 'brewinandchewin',
                'fantasyfurniture', 'redeco', 'twigs', 'architects_palette', 'myrtrees'}
EXCLUDE_IDS = {'minecraft:fire', 'minecraft:frosted_ice', 'minecraft:grass_block', 'vinery:grass_slab',
               'vinery:basket', 'vinery:grapevine_pot', 'vinery:grapevine_leaves', 'vintagedelight:magic_peanut',
               'immersive_weathering:weeds'}
EXCLUDE_RE = re.compile(r'(sapling|propagule|trellis|^delightful:sliced_)')
# A dump row counts as a plant when its class chain has one of these, or its id is in PLANT_IDS (fruit leaves
# and plants built on plain Block); anything else (mossy stone, leaves, dripleaf) is skipped.
PLANT_BASES = {'BushBlock', 'CropBlock', 'StemBlock', 'CocoaBlock', 'SugarCaneBlock', 'CactusBlock', 'BambooBlock',
               'GrowingPlantHeadBlock', 'ChorusFlowerBlock', 'VineBlock', 'DoublePlantBlock', 'NetherWartBlock'}
PLANT_IDS = re.compile(r'^(fruitsdelight:.*_leaves|vinery:(apple|cherry)_leaves|the_dirty_stuff:tobacco_seeds|'
                       r'oceanic_delight:sea_grape_plant|upgrade_aquatic:mulberry_vine)$')


def load(rel, default=None):
    p = os.path.join(EXP, rel)
    if not os.path.exists(p):
        return default
    with open(p) as f:
        return json.load(f)


def class_index():
    """class path -> bytes loader, over mods/*.jar and the jars nested in them."""
    idx = {}

    def add(zf):
        for n in zf.namelist():
            if n.endswith('.class') and n not in idx:
                idx[n] = zf
            elif n.startswith('META-INF/jarjar/') and n.endswith('.jar'):
                try:
                    add(zipfile.ZipFile(io.BytesIO(zf.read(n))))
                except zipfile.BadZipFile:
                    pass
    for j in sorted(glob.glob(os.path.join(ROOT, 'mods', '*.jar'))):
        try:
            add(zipfile.ZipFile(j))
        except zipfile.BadZipFile:
            pass
    return idx


def grow_event(classes, idx):
    """('yes' | 'no' | '?', evidence) for a class chain (most derived first)."""
    for c in classes:
        simple = c.rsplit('.', 1)[-1]
        if c in PACK_HOOKS:
            return 'yes', simple + ', pack hook'
        if c.startswith('net.minecraft.'):
            if simple in VANILLA_EVENT:
                return 'yes', simple
            continue
        zf = idx.get(c.replace('.', '/') + '.class')
        if zf is None:
            return '?', simple + ' not found in mods/'
        data = zf.read(c.replace('.', '/') + '.class')
        if b'onCropsGrowPre' in data:
            return 'yes', simple
        # a randomTick (m_213898_) override without the event replaces the growth further up the chain
        if b'm_213898_' in data:
            return 'no', simple
    return 'no', next((c.rsplit('.', 1)[-1] for c in classes if not c.startswith('net.minecraft.')), classes[0].rsplit('.', 1)[-1])


def crop_types():
    """block -> type, item -> type, and the problems in the json (an id in two types)."""
    with open(CROP_SEASONS) as f:
        cfg = json.load(f)
    problems = []

    def flat(lists):
        out = {}
        for t, ids in (lists or {}).items():
            for i in ids:
                if i in out:
                    problems.append('`%s` is in %s and %s' % (i, out[i], t))
                else:
                    out[i] = t
        return out
    return flat(cfg.get('blocks')), flat(cfg.get('items')), problems


def main():
    audit = load('crops/crop_growth_audit.json')
    btags = load('tags/block_tags.json')
    if audit is None or btags is None:
        sys.exit('missing dumps - run /dumpcropgrowth and /dumptags (or /dumpall)')
    items = set()
    for f in glob.glob(os.path.join(EXP, 'items_by_mod', '*.json')):
        items |= set(json.load(open(f))['items'])
    bseason = {s: set(btags.get('sereneseasons:%s_crops' % s, [])) for s in SEASONS}
    btype, itype, problems = crop_types()
    idx = class_index()
    if not idx:
        sys.exit('no class files read from mods/')

    rows = {}
    for b in audit['blocks']:
        bid = b['id']
        if b['mod'] in EXCLUDE_MODS or bid in EXCLUDE_IDS or EXCLUDE_RE.search(bid):
            continue
        if not PLANT_IDS.match(bid) and not any(c.rsplit('.', 1)[-1] in PLANT_BASES for c in b['classes']):
            continue
        ev, why = grow_event(b['classes'], idx)
        rows[bid] = {'item': b.get('item') or FALLBACK_ITEMS.get(bid), 'event': ev, 'why': why, 'note': None}
    for bid, (item, cls, note) in EXTRA.items():
        ev, why = grow_event([cls], idx) if cls else ('yes', 'pack code')
        rows[bid] = {'item': item, 'event': ev, 'why': why, 'note': note}

    findings = collections.defaultdict(list)
    findings['json'] = problems
    lines = []
    by_mod = collections.defaultdict(list)
    for bid in sorted(rows):
        by_mod[bid.split(':')[0]].append(bid)
    counts = collections.Counter()
    for mod in sorted(by_mod):
        for bid in by_mod[mod]:
            r = rows[bid]
            ss = '/'.join(ABBR[x] for x in SEASONS if bid in bseason[x])
            t = btype.get(bid)
            item = r['item']
            if item and item not in items:
                findings['item'].append('`%s`: planting item `%s` is not a registered item' % (bid, item))
            if t is None:
                findings['untyped'].append('`%s`' % bid)
                effect = 'own speed all year (untyped)'
            elif r['event'] == 'yes':
                effect = t
            elif bid in BONEMEAL_ONLY:
                effect = t + ' (tooltip only: grows by bone meal only)'
            elif r['event'] == 'no':
                effect = 'own speed all year (no grow event)'
                if t != 'none':
                    findings['noevent'].append('`%s` (%s, %s)' % (bid, t, r['why']))
            else:
                effect = '? (%s)' % r['why']
            counts[t or 'untyped'] += 1
            lines.append('| %s | `%s` | %s | %s | %s | %s | %s |' % (
                mod, bid, '`%s`' % item if item else '-', t or '-', ss or '-',
                r['event'] + ('' if r['event'] == 'yes' and r['why'] in VANILLA_EVENT else ' (%s)' % r['why']),
                effect + (' ' + r['note'] if r['note'] else '')))
    findings['extra'] = sorted('`%s` (%s)' % (b, t) for b, t in btype.items() if b not in rows)

    out = []
    out.append('# Crop growth seasons audit')
    out.append('')
    out.append('Generated by `tools/crop_season_audit.py` (`/cegen run crop_season_audit`) on %s from the dumps in '
               '`kubejs/exported/`, `kubejs/generation_scripts/worldgen/crop_seasons.json` and the jars in `mods/`. '
               'Do not edit by hand; the design is in [crops.md](crops.md) section 2.12.1.' % date.today().isoformat())
    out.append('')
    out.append('Crop seasons (Creative Calendar `seasons/CropSeasons`): each season is fertile (growth +25 %), neutral or off '
               '(growth -25 %) for the crop\'s type, from the biome\'s calendar (four seasons, or wet/dry in tropical, '
               'dry and arid biomes). Only growth that fires Forge\'s `onCropsGrowPre` is checked, so a plant without '
               'the event grows at its own speed whatever its type. Serene Seasons\' own season tags (column SS) are '
               'inert: fertility.toml `seasonal_crops = false`.')
    out.append('')
    out.append('Counts: ' + ', '.join('%s %d' % (k, counts[k]) for k in sorted(counts)) + '.')
    out.append('')
    out.append('## Findings')
    out.append('')
    sections = [
        ('json', 'crop_seasons.json problems'),
        ('noevent', 'Typed, but growth never fires the event (the type has no effect; needs a hook)'),
        ('untyped', 'In no type (own speed all year)'),
        ('item', 'Planting item problems'),
        ('extra', 'Typed blocks outside the audited plants (no growth of their own, or not in the dump)'),
    ]
    for key, title in sections:
        vals = findings.get(key, [])
        out.append('### %s (%d)' % (title, len(vals)))
        out.append('')
        out.append(', '.join(vals) if key in ('untyped', 'extra', 'noevent') and vals else
                   '\n'.join('- ' + v for v in vals) if vals else 'none')
        out.append('')
    out.append('## Table')
    out.append('')
    out.append('| Mod | Block | Planting item | Type | SS (inert) | Event | Today |')
    out.append('|---|---|---|---|---|---|---|')
    out.extend(lines)
    out.append('')
    with open(OUT_MD, 'w') as f:
        f.write('\n'.join(out))
    print('crop_season_audit: %d plants, %d untyped, %d typed without the event, %d json problems -> %s' % (
        len(rows), len(findings['untyped']), len(findings['noevent']), len(problems), os.path.relpath(OUT_MD, ROOT)))


if __name__ == '__main__':
    main()

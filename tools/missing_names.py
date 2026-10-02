#!/usr/bin/env python3
"""Missing-name catalog: blocks, items, effects and potions whose en_us name the client can't find,
so JEI / WTHIT / tooltips show the raw translation key (block.mod.id, effect.mod.id, ...).

Names come from tools/lang_index.py (vanilla + Forge + mod jars + KubeJS + enabled resource packs).
What exists comes from:
  blocks   every assets/<ns>/blockstates/*.json in the vanilla jar and mods/ (namespace must be one a
           mod ships, so compat blockstates for absent mods drop out)
  items    kubejs/exported/items_by_mod/*.json (/dumpitemsbymod)
  effects  kubejs/exported/mob_effects.json, potions kubejs/exported/potions.json (/dumpall)

A block is "partial" when no item has its id: crops, potted plants, placed food, wall variants,
fluid blocks. Those never show in JEI, only in WTHIT / Jade / the F3 screen and death messages,
so they are the ones that get forgotten.

Offline limits (see feature_writeup/names.md section 4): a mod can override getDescriptionId (wall
torches/signs/banners/heads borrow their item's name), and runtime-generated packs (Everycomp,
Moonlight, KubeJS builders) add lang the scan can't see. Rows from those cases are marked.

With kubejs/exported/names.json present (/dumpnames, run in single player) that dump is used instead:
it has each entry's real description id and the game's own answer, so the offline caveats go away and
enchantments, entities, fluids and villager professions are covered too. --offline ignores the dump.

    python3 tools/missing_names.py             rewrite the generated sections of feature_writeup/names.md
    python3 tools/missing_names.py --print     print the counts only
    python3 tools/missing_names.py --offline   jar scan even when a dump exists
"""
import argparse
import json
import re
import sys
import zipfile
from collections import defaultdict
from datetime import datetime
from pathlib import Path

import lang_index

INSTANCE = lang_index.INSTANCE
EXPORTED = INSTANCE / 'kubejs/exported'
DOC = INSTANCE / 'feature_writeup/names.md'
BS_RE = re.compile(r'^assets/([^/]+)/blockstates/(.+)\.json$')

# Namespaces whose blocks/items are registered by KubeJS startup scripts. KubeJS 1902 builders get a
# name from .displayName() or an id-derived default at runtime, which the offline scan can't see.
KUBEJS_NS = {'kubejs'}

FOOD_WORDS = re.compile(r'(^|_)(cake|pie|feast|plate|plated|bowl|food|soup|stew|sandwich|slice|'
                        r'pizza|roll|burger|salad|pasta|ham|chicken|rice|tart|cookie|bread|pancakes?)(_|$)')


def scan_blocks(namespaces):
    """{block id: blockstate text} from the vanilla jar and every mod jar."""
    blocks = {}
    jars = [lang_index.VANILLA_JAR] + sorted(lang_index.MODS.glob('*.jar'))
    for jar in jars:
        try:
            with zipfile.ZipFile(jar) as z:
                for name in z.namelist():
                    m = BS_RE.match(name)
                    # subfolders (another_furniture's unused/...) are model variants, not block ids
                    if m and m.group(1) in namespaces and '/' not in m.group(2):
                        bid = '%s:%s' % (m.group(1), m.group(2))
                        blocks.setdefault(bid, (z.read(name).decode('utf-8', 'replace'), jar.name))
        except (zipfile.BadZipFile, OSError):
            pass
    return blocks


def load_items(namespaces):
    items = set()
    for f in sorted((EXPORTED / 'items_by_mod').glob('*.json')):
        if f.stem not in namespaces:
            continue
        items.update(json.loads(f.read_text()).get('items', []))
    return items


def key_of(kind, rid):
    ns, path = rid.split(':', 1)
    return '%s.%s.%s' % (kind, ns, path.replace('/', '.'))


def fluid_names():
    f = EXPORTED / 'fluids.json'
    if not f.exists():
        return set()
    return {k.split(':', 1)[1].replace('_flowing', '').replace('flowing_', '')
            for k in json.loads(f.read_text())}


def classify(bid, state, fluids):
    ns, path = bid.split(':', 1)
    leaf = path.rsplit('/', 1)[-1]
    if 'potted' in leaf:
        return 'potted'
    if 'cauldron' in leaf:
        return 'cauldron variant'
    if ns == 'displaydelight' or FOOD_WORDS.search(leaf):
        return 'placed food'
    if re.search(r'"age=|"age"', state) or re.search(r'(crop|crops|stem|bush|plant|vine)s?$', leaf):
        return 'crop'
    if 'wall_' in leaf and not leaf.endswith('_wall'):
        return 'wall variant'
    if leaf.endswith('_fluid_block') or '"level=' in state or leaf in fluids or leaf.rstrip('_block') in fluids or leaf.endswith('_fluid'):
        return 'fluid block'
    return 'other'


ORDER = ['crop', 'potted', 'placed food', 'cauldron variant', 'wall variant', 'fluid block', 'other']
NOTES = {
    'wall variant': 'Expected, not a gap: wall torches, signs, banners and heads return their item\'s name '
                    '(vanilla\'s own wall banners have no key either). Only a dump row here is a real gap.',
    'cauldron variant': 'Filled-cauldron blocks. WTHIT shows these when you look at the cauldron.',
    'placed food': 'Food placed as a block (pies, feasts, Display Delight plates). Display Delight is named by '
                   '`tools/gen_display_delight_names.py`.',
    'crop': 'Growing crops and climbing plants. The item is the seed, so only WTHIT shows the block name.',
    'potted': 'Flower-pot variants: one block per plant.',
    'fluid block': 'Shown by WTHIT when looking at a fluid with the fluid option on, and in some death messages.',
    'other': 'Technical blocks (moving pieces, multiblock parts, light/air blocks) included. Many are never '
             'seen; name the ones that show in WTHIT.',
}


DUMP = EXPORTED / 'names.json'


def build_from_dump(d):
    """Same shape as build(), from /dumpnames output."""
    fluids = fluid_names()
    miss = d['missing']
    partial = defaultdict(list)
    for row in miss.get('block', []):
        if row.get('hasItem'):
            continue
        props = row.get('props', [])
        fake_state = ('"age"' if 'age' in props else '') + ('"level=' if 'level' in props else '')
        partial[classify(row['id'], fake_state, fluids)].append((row['id'], ''))
    items = [r['id'] for r in miss.get('item', [])]
    mtime = DUMP.stat().st_mtime
    return {
        'source': 'dump', 'partial': partial, 'items': sorted(items),
        'effects': sorted((r['id'], r['key']) for r in miss.get('effect', [])),
        'potions': sorted((r['key'].rsplit('.', 1)[-1], r['id'], r['forms']) for r in miss.get('potion', [])),
        'more': {k: sorted((r['id'], r['key']) for r in miss.get(k, []))
                 for k in ('enchantment', 'entity', 'fluid', 'profession')},
        'n_blocks': d['totals'].get('block', 0), 'n_items': d['totals'].get('item', 0),
        'totals': d['totals'], 'stamp': {'items': mtime},
    }


def build():
    names, source = lang_index.build()
    namespaces = lang_index.mod_namespaces()
    items = load_items(namespaces)
    fluids = fluid_names()
    blocks = scan_blocks(namespaces)

    partial = defaultdict(list)
    full_blocks_missing = []
    for bid, (state, jar) in sorted(blocks.items()):
        ns = bid.split(':', 1)[0]
        if ns in KUBEJS_NS or key_of('block', bid) in names:
            continue
        if bid in items:
            full_blocks_missing.append((bid, jar))
            continue
        partial[classify(bid, state, fluids)].append((bid, jar))

    missing_items = []
    for iid in sorted(items):
        ns = iid.split(':', 1)[0]
        if ns in KUBEJS_NS:
            continue
        if key_of('item', iid) not in names and key_of('block', iid) not in names:
            missing_items.append(iid)

    missing_effects = []
    f = EXPORTED / 'mob_effects.json'
    if f.exists():
        for ns, effs in json.loads(f.read_text()).items():
            for eid, info in effs.items():
                # /dumppotions resolved the name in game: a raw key back means it's missing.
                shown = info.get('name', '')
                if shown == eid or re.match(r'^[a-z0-9_]+(\.[a-z0-9_:]+){2,}$', shown):
                    missing_effects.append((eid, shown))

    missing_potions = []
    f = EXPORTED / 'potions.json'
    if f.exists():
        forms = [('potion', 'item.minecraft.potion.effect.'),
                 ('splash', 'item.minecraft.splash_potion.effect.'),
                 ('lingering', 'item.minecraft.lingering_potion.effect.'),
                 ('arrow', 'item.minecraft.tipped_arrow.effect.')]
        seen = set()
        for ns, pots in json.loads(f.read_text()).items():
            for pid, info in pots.items():
                base = info.get('name') or pid.split(':', 1)[1]
                if base in seen:
                    continue
                seen.add(base)
                lacking = [label for label, pre in forms if pre + base not in names]
                if lacking:
                    missing_potions.append((base, pid, lacking))

    stamp = {
        'items': max((p.stat().st_mtime for p in (EXPORTED / 'items_by_mod').glob('*.json')), default=0),
    }
    return {
        'source': 'offline', 'more': {}, 'partial': partial, 'full_blocks': full_blocks_missing, 'items': missing_items,
        'effects': sorted(missing_effects), 'potions': sorted(missing_potions),
        'n_blocks': len(blocks), 'n_items': len(items), 'stamp': stamp,
    }


def mod_of(rid):
    return rid.split(':', 1)[0]


def table_by_mod(rows, ids=True):
    """Rows of ids grouped per mod into one line each (counts only when ids is False)."""
    by = defaultdict(list)
    for r in rows:
        by[mod_of(r)].append(r.split(':', 1)[1])
    out = ['| Mod | Count | Ids |', '|---|---|---|'] if ids else ['| Mod | Count |', '|---|---|']
    for ns in sorted(by, key=lambda n: (-len(by[n]), n)):
        if ids:
            out.append('| %s | %d | %s |' % (ns, len(by[ns]), ', '.join('`%s`' % i for i in sorted(by[ns]))))
        else:
            out.append('| %s | %d |' % (ns, len(by[ns])))
    return out


def render_partial(r):
    total = sum(len(v) for v in r['partial'].values())
    when = datetime.fromtimestamp(r['stamp']['items']).strftime('%Y-%m-%d')
    if r['source'] == 'dump':
        src = 'the `/dumpnames` export of %s (%d blocks, the game\'s own lang check)' % (when, r['n_blocks'])
    else:
        src = ('an **offline** scan of %d blockstates and the item dump of %s (has false positives, see '
               'section 4; run `/dumpnames` in single player for the real list)' % (r['n_blocks'], when))
    lines = ['Generated by `python3 tools/missing_names.py` on %s from %s. **%d partial blocks have no name** '
             '(no item shares their id, so they only show in WTHIT, F3 and death messages).'
             % (datetime.now().strftime('%Y-%m-%d %H:%M'), src, total), '']
    lines += ['| Kind | Count |', '|---|---|']
    for kind in ORDER:
        lines.append('| %s | %d |' % (kind, len(r['partial'].get(kind, []))))
    for kind in ORDER:
        rows = r['partial'].get(kind, [])
        if not rows:
            continue
        lines += ['', '### %s (%d)' % (kind.capitalize(), len(rows)), '']
        if kind in NOTES:
            lines += [NOTES[kind], '']
        brief = r['source'] == 'offline' and kind == 'other'
        if brief:
            lines += ['Offline this bucket is mostly blockstates for compat blocks that only register when another '
                      'mod is installed (Lightman\'s Currency, Dramatic Doors), so only counts are listed.', '']
        lines += table_by_mod([b for b, _ in rows], ids=not brief)
    return lines


def render_rest(r):
    lines = ['Same run. Blocks that do have an item but no `block.*` key are listed with items (the item '
             'shows the block key in JEI).', '']
    lines += ['### Items and full blocks (%d)' % len(r['items']), '']
    offline = r['source'] == 'offline'
    if r['items'] and offline:
        lines += ['Offline this count is mostly false positives: Everycomp writes its lang at runtime, and mods like '
                  'Lightman\'s Currency build names in code. Counts only; `/dumpnames` gives the real ids.', '']
    lines += table_by_mod(r['items'], ids=not offline) if r['items'] else ['None.']
    lines += ['', '### Effects (%d)' % len(r['effects']), '']
    if r['effects']:
        lines += ['| Effect | Key |', '|---|---|']
        lines += ['| `%s` | `%s` |' % (e, n) for e, n in r['effects']]
    else:
        lines += ['None.']
    lines += ['', '### Potions (%d)' % len(r['potions']), '',
              'A potion needs four keys: the bottle, splash, lingering and tipped arrow. The table lists the forms '
              'that fall back to the raw key.', '']
    if not r['potions']:
        lines += ['None.']
    else:
        lines += ['| Potion name | Registered as | Missing forms |', '|---|---|---|']
        lines += ['| `%s` | `%s` | %s |' % (b, p, ', '.join(l)) for b, p, l in r['potions']]
    for kind, rows in r['more'].items():
        title = {'entity': 'Entities'}.get(kind, kind.capitalize() + 's')
        lines += ['', '### %s (%d)' % (title, len(rows)), '']
        lines += table_by_mod([i for i, _ in rows]) if rows else ['None.']
    if r['source'] == 'offline':
        lines += ['', 'Enchantments, entities, fluids and villager professions need the `/dumpnames` export.']
    return lines


def splice(doc, tag, lines):
    begin, end = '<!-- BEGIN GENERATED: %s -->' % tag, '<!-- END GENERATED: %s -->' % tag
    pat = re.compile(re.escape(begin) + r'.*?' + re.escape(end), re.S)
    if not pat.search(doc):
        sys.exit('%s: no %s block' % (DOC.name, begin))
    return pat.sub(lambda _: begin + '\n' + '\n'.join(lines) + '\n' + end, doc)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--print', action='store_true')
    ap.add_argument('--offline', action='store_true')
    args = ap.parse_args()
    dump = json.loads(DUMP.read_text()) if DUMP.exists() and not args.offline else None
    if dump and dump.get('dedicated'):
        print('WARNING: %s came from a dedicated server (no client lang); using the offline scan' % DUMP.name)
        dump = None
    r = build_from_dump(dump) if dump else build()
    print('source: %s' % r['source'])
    counts = {k: len(v) for k, v in r['partial'].items()}
    print('partial blocks: %s' % ', '.join('%s %d' % (k, counts.get(k, 0)) for k in ORDER))
    print('items %d, effects %d, potions %d%s' % (len(r['items']), len(r['effects']), len(r['potions']),
                                                ''.join(', %s %d' % (k, len(v)) for k, v in r['more'].items())))
    if args.print:
        return 0
    doc = DOC.read_text()
    doc = splice(doc, 'partial-blocks', render_partial(r))
    doc = splice(doc, 'other-kinds', render_rest(r))
    DOC.write_text(doc)
    print('wrote %s' % DOC.relative_to(INSTANCE))
    return 0


if __name__ == '__main__':
    sys.exit(main())

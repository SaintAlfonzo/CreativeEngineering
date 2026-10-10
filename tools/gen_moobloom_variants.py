#!/usr/bin/env python3
"""More Mooblooms data from the variant table in feature_writeup/mooblooms.md (the table with a "Flower pool"
column, section 4.4).

    python3 tools/gen_moobloom_variants.py           # write the files
    python3 tools/gen_moobloom_variants.py --check   # print what would change, write nothing (exit 1 if anything)
    python3 tools/gen_moobloom_variants.py --doc other.md   # read the table from another copy of the writeup

The table is the source; edit it, then rerun this. Per row it writes, under
development/moreMooblooms/src/main/resources/data/more_mooblooms/:
  moobloom_variants/<id>.json   flowers, feed, scale (keys it doesn't own, like egg_colors, any_weight,
                                breed_weight and spawns_in_dark_on, are kept from the existing file)
  tags/items/feed/<id>.json     the Extra food column, when it has entries
  moobloom_breeding/<id>.json   the Parent Combo columns: every pair that makes <id>, each with its chance
Files under feed/ and moobloom_breeding/ named after a variant in the table, that the table no longer asks for,
are deleted. A variant file whose id left the table is reported, not deleted. Then runs the mod's
tools/egg_colors.py so new coats get egg colours.

Cell syntax:
  Flower pool   comma-separated "name weight", e.g. "rose_bush 5, poppy 4, red_tulip 1". A bare name is
                minecraft:<name>; sunflower, lilac, rose_bush and peony get [half=upper] (the bloom).
                "(...)" notes are ignored, except "(only when ... installed)" / "(if installed)", which make the
                entry optional (skipped in game when its block is missing). Ids are checked against the
                /dumptags + items_by_mod exports: a bare name must be vanilla, other mods need the full id.
                Modifiers, separated by commas like entries:
                  "`scale` 0.5"         the variant's flower scale
                  "drop `chance` 0.1"   drop chance of the entry before it (default 1)
                  "feed `weight` 9"     feed_weight of the entry before it: when a flower is in several pools,
                                        feeding it to a cow picks a variant by these weights (default 1 each)
                An entry without a weight is not a pool entry (skipped with a note).
  Extra food    backticked item ids, "(only when ... installed)" makes one optional (tag entry required: false)
  Parent Combo  (every column with "parent" in its name) one pair per cell: "a+b (N%)", e.g. "rose+lily (35%)".
                Names are variant ids, with or without "bloom" (rose = rosebloom); the chance defaults to 50%
                (D35). Blank, "-" or "feeding only" = none. A pair may only make one variant.
"""
import glob
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOC = os.path.join(ROOT, 'feature_writeup', 'mooblooms.md')
MOD = os.path.join(ROOT, 'development', 'moreMooblooms')
DATA = os.path.join(MOD, 'src', 'main', 'resources', 'data', 'more_mooblooms')
NS = 'more_mooblooms'
TALL = {'sunflower', 'lilac', 'rose_bush', 'peony'}
DEFAULT_CHANCE = 0.5
BLOCK_DUMP = os.path.join(ROOT, 'kubejs', 'exported', 'tags', 'block_tags_by_block.json')
ITEM_DUMPS = os.path.join(ROOT, 'kubejs', 'exported', 'items_by_mod', '*.json')

notes = []
errors = []


def note(where, msg):
    notes.append(f'{where}: {msg}')


def error(where, msg):
    errors.append(f'{where}: {msg}')


def table_rows():
    """The rows of the first table with a "Flower pool" column, as {header: cell} dicts."""
    lines = open(DOC, encoding='utf8').read().splitlines()
    i = next((n for n, l in enumerate(lines) if l.startswith('|') and 'flower pool' in l.lower()), None)
    if i is None:
        sys.exit(f'no table with a "Flower pool" column in {DOC}')
    header = [c.strip() for c in lines[i].strip().strip('|').split('|')]
    rows = []
    for line in lines[i + 2:]:
        if not line.startswith('|'):
            break
        cells = [c.strip() for c in line.strip().strip('|').split('|')]
        cells += [''] * (len(header) - len(cells))
        rows.append(dict(zip(header, cells)))
    return header, rows


def column(header, what, test):
    for h in header:
        if test(h.lower()):
            return h
    sys.exit(f'variant table has no {what} column (columns: {header})')


def known_ids():
    """Block and item ids from the /dumptags and items_by_mod exports (None when they're missing)."""
    if not os.path.exists(BLOCK_DUMP):
        return None
    ids = set()
    for blocks in json.load(open(BLOCK_DUMP, encoding='utf8')).values():
        ids.update(blocks)
    for f in glob.glob(ITEM_DUMPS):
        ids.update(json.load(open(f, encoding='utf8')).get('items', []))
    return ids


KNOWN = known_ids()


def state_of(vid, name, optional):
    """A pool name as a block state string, or None (error/note recorded)."""
    name = name.strip('`')
    full = name if ':' in name else f'minecraft:{name}'
    if KNOWN is not None and full not in KNOWN:
        if ':' not in name:
            error(vid, f'"{name}" is not a vanilla block; write the full id (e.g. `farmersdelight:wild_onions 1`)')
            return None
        if not optional:
            error(vid, f'{full} is not in the block/item dumps; mark it "(only when ... installed)" if it is another mod\'s')
            return None
        note(vid, f'{full} is not installed here; optional, so the game skips it')
    return full + ('[half=upper]' if full.startswith('minecraft:') and full[10:] in TALL else '')


def variant_name(text, variants):
    """A variant id from a table name: the id itself, or the id without "bloom" (rose -> rosebloom)."""
    t = text.strip().strip('`').strip().lower()
    if ':' in t:
        return t
    for name in (t, t + 'bloom'):
        if name in variants:
            return f'{NS}:{name}'
    return None


def parse_combo(vid, cell, variants):
    """One "a+b (N%)" cell -> {parents, chance} or None (blank)."""
    t = cell.strip()
    if not t or t == '-' or 'feeding only' in t.lower():
        return None
    m = re.fullmatch(r'`?([a-z0-9_:./-]+)`?\s*\+\s*`?([a-z0-9_:./-]+)`?\s*(?:\(\s*([0-9.]+)\s*%\s*\))?', t, re.I)
    if not m:
        error(vid, f'can\'t read parent combo "{cell}" (write it as "a+b (N%)")')
        return None
    parents = []
    for raw in (m.group(1), m.group(2)):
        p = variant_name(raw, variants)
        if p is None:
            error(vid, f'parent "{raw}" in "{cell}" is not a variant in the table')
            return None
        parents.append(p)
    chance = float(m.group(3)) / 100 if m.group(3) else DEFAULT_CHANCE
    if not 0 < chance <= 1:
        error(vid, f'chance in "{cell}" must be above 0% and at most 100%')
        return None
    return {'parents': parents, 'chance': round(chance, 4)}


def split_entries(cell):
    """Comma/semicolon separated parts, ignoring separators inside (...)."""
    parts, depth, cur = [], 0, ''
    for ch in cell:
        if ch == '(':
            depth += 1
        elif ch == ')':
            depth = max(0, depth - 1)
        if ch in ',;' and depth == 0:
            parts.append(cur.strip())
            cur = ''
        else:
            cur += ch
    if cur.strip():
        parts.append(cur.strip())
    return parts


def optional_of(part):
    return bool(re.search(r'\((only when|if)\b[^)]*install', part, re.I))


def strip_notes(part):
    return re.sub(r'\([^)]*\)', '', part).strip()


def parse_pool(vid, cell):
    pool, scale = [], None
    for part in split_entries(cell):
        optional = optional_of(part)
        text = strip_notes(part)
        if not text:
            continue
        m = re.fullmatch(r'`?scale`?\s+([0-9.]+)', text)
        if m:
            scale = float(m.group(1))
            continue
        m = re.fullmatch(r'drop\s+`?chance`?\s+([0-9.]+)', text)
        if m:
            if not pool:
                error(vid, f'"{part}" has no pool entry before it')
            else:
                pool[-1]['chance'] = float(m.group(1))
            continue
        m = re.fullmatch(r'feed\s+`?weight`?\s+([0-9]+)', text)
        if m:
            if not pool:
                error(vid, f'"{part}" has no pool entry before it')
            else:
                pool[-1]['feed_weight'] = int(m.group(1))
            continue
        m = re.fullmatch(r'(`?[a-z0-9_:./-]+`?)\s+([0-9]+)', text)
        if m:
            state = state_of(vid, m.group(1), optional)
            if state is None:
                continue
            entry = {'state': state, 'weight': int(m.group(2))}
            if optional:
                entry['optional'] = True
            pool.append(entry)
            continue
        if re.fullmatch(r'`?[a-z0-9_:./-]+`?', text):
            note(vid, f'pool entry "{text.strip("`")}" has no weight: not in the pool (food only?)')
            continue
        error(vid, f'can\'t read pool entry "{part}"')
    if not pool:
        error(vid, 'empty flower pool')
    return pool, scale


def parse_food(vid, cell):
    out = []
    for part in split_entries(cell):
        text = strip_notes(part).strip('`').strip()
        if not text:
            continue
        if not re.fullmatch(r'[a-z0-9_.-]+:[a-z0-9_./-]+', text):
            error(vid, f'can\'t read extra food "{part}" (needs a full item id)')
            continue
        if KNOWN is not None and text not in KNOWN and not optional_of(part):
            error(vid, f'extra food {text} is not in the item dumps; mark it "(only when ... installed)" if it is another mod\'s')
            continue
        out.append({'id': text, 'required': False} if optional_of(part) else text)
    return out


def variant_json(old, pool, scale, feed_tag):
    out = {}
    # simplest form for a plain one-flower pool
    if len(pool) == 1 and set(pool[0]) == {'state', 'weight'}:
        out['flowers'] = pool[0]['state']
    else:
        out['flowers'] = [e if set(e) != {'state'} else e['state'] for e in pool]
    if feed_tag:
        out['feed'] = [f'#{NS}:{feed_tag}']
    if scale is not None and scale != 1.0:
        out['scale'] = scale
    owned = {'flowers', 'feed', 'scale'}
    for k, v in old.items():
        if k not in owned:
            out[k] = v
    return out


def main():
    global DOC
    check = '--check' in sys.argv
    if '--doc' in sys.argv:  # a copy of the plan, e.g. to try a table edit
        DOC = sys.argv[sys.argv.index('--doc') + 1]
    header, rows = table_rows()
    c_id = column(header, 'id', lambda h: h.startswith('id'))
    c_pool = column(header, 'flower pool', lambda h: 'pool' in h)
    c_food = column(header, 'extra food', lambda h: 'food' in h)
    c_parents = [h for h in header if 'parent' in h.lower()]

    names = []
    for row in rows:
        m = re.search(r'`([a-z0-9_./-]+)`', row[c_id])
        if m:
            names.append(m.group(1))
        else:
            error('table', f'row without a `id`: {row[c_id]}')

    wanted = {}  # path -> json object, or None to delete
    variants = []
    pairs = {}
    for row in rows:
        m = re.search(r'`([a-z0-9_./-]+)`', row[c_id])
        if not m:
            continue
        name = m.group(1)
        vid = f'{NS}:{name}'
        variants.append(name)
        vpath = os.path.join(DATA, 'moobloom_variants', name + '.json')
        old = json.load(open(vpath, encoding='utf8')) if os.path.exists(vpath) else {}

        pool, scale = parse_pool(vid, row[c_pool])
        food = parse_food(vid, row[c_food])
        tag_path = os.path.join(DATA, 'tags', 'items', 'feed', name + '.json')
        wanted[tag_path] = {'replace': False, 'values': food} if food else None
        wanted[vpath] = variant_json(old, pool, scale, f'feed/{name}' if food else None)

        rules = []
        for col in c_parents:
            combo = parse_combo(vid, row[col], names)
            if combo is None:
                continue
            key = frozenset(combo['parents'])
            if key in pairs:
                error(vid, f'{" + ".join(combo["parents"])} already makes {pairs[key]}; a pair makes one variant')
                continue
            pairs[key] = vid
            rules.append({'parents': combo['parents'], 'chance': combo['chance'], 'results': [{'variant': vid, 'weight': 1}]})
        bpath = os.path.join(DATA, 'moobloom_breeding', name + '.json')
        wanted[bpath] = None if not rules else (rules[0] if len(rules) == 1 else {'rules': rules})

    existing = os.path.join(DATA, 'moobloom_variants')
    for f in sorted(os.listdir(existing)) if os.path.isdir(existing) else []:
        if f.endswith('.json') and f[:-5] not in variants:
            note('moobloom_variants', f'{f} is not in the table (left alone; delete it by hand if it should go)')

    if errors:
        for e in errors:
            print('  ERROR', e)
        sys.exit(f'{len(errors)} error(s); nothing written')

    changed = 0
    for path, obj in sorted(wanted.items()):
        rel = os.path.relpath(path, ROOT)
        if obj is None:
            if os.path.exists(path):
                changed += 1
                print(f'delete {rel}')
                if not check:
                    os.remove(path)
            continue
        text = json.dumps(obj, indent=2) + '\n'
        if os.path.exists(path) and open(path, encoding='utf8').read() == text:
            continue
        changed += 1
        print(f'write  {rel}')
        if not check:
            os.makedirs(os.path.dirname(path), exist_ok=True)
            with open(path, 'w', encoding='utf8') as fh:
                fh.write(text)
    for n in notes:
        print('  note', n)
    print(f'{len(variants)} variants, {len(pairs)} combinations, '
          f'{changed} file(s) {"would change" if check else "changed"}')
    if not check:
        sys.stdout.flush()
        subprocess.run([sys.executable, os.path.join(MOD, 'tools', 'egg_colors.py')], check=True)
    elif changed:
        sys.exit(1)


if __name__ == '__main__':
    main()

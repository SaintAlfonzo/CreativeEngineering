#!/usr/bin/env python3
"""Give hand-filled chests/barrels in the ported structure templates (kubejs/data) a loot table.

Lootr only converts a container that has a LootTable. The data-only structure mods ported by
port_structure_jars.py were built with chests and barrels holding fixed items (`Items`, no
`LootTable`), so players share them like plain vanilla chests. For every minecraft:chest,
trapped_chest and barrel in kubejs/data/*/structures/**.nbt that has Items and no LootTable this:

  - writes <ns>:chests/fixed/<hash>, a loot table that drops exactly those stacks (one pool per
    stack, count and item NBT kept; slot positions are lost, Lootr/vanilla scatter them);
    identical contents share one table
  - sets LootTable on the block entity in the template and removes Items

Idempotent (a converted container has a LootTable, so it is skipped). Re-run after every
port_structure_jars.py run, since that re-copies the original templates.

  python3 tools/lootr_fixed_containers.py [--dry-run]
"""
import argparse, collections, glob, gzip, hashlib, json, os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import port_structure_jars as P

ROOT = P.ROOT
KJS_DATA = P.KJS_DATA
CONTAINERS = ('minecraft:chest', 'minecraft:trapped_chest', 'minecraft:barrel')


def snbt(tag):
    """Typed NBT -> SNBT string, for the set_nbt loot function."""
    t, v = tag.t, tag.v
    if t == 1: return f'{v}b'
    if t == 2: return f'{v}s'
    if t == 3: return str(v)
    if t == 4: return f'{v}L'
    if t == 5: return f'{v!r}f'
    if t == 6: return f'{v!r}d'
    if t == 7: return '[B;' + ','.join(f'{b - 256 if b > 127 else b}b' for b in v) + ']'
    if t == 8: return json.dumps(v, ensure_ascii=False)
    if t == 9: return '[' + ','.join(snbt(x) for x in v) + ']'
    if t == 10: return '{' + ','.join(f'{json.dumps(k)}:{snbt(x)}' for k, x in v.items()) + '}'
    if t == 11: return '[I;' + ','.join(map(str, v)) + ']'
    if t == 12: return '[L;' + ','.join(f'{x}L' for x in v) + ']'
    raise ValueError(t)


def stack_entry(item):
    d = item.v
    count = d['Count'].v if 'Count' in d else 1
    entry = {'type': 'minecraft:item', 'name': d['id'].v}
    funcs = []
    if count != 1:
        funcs.append({'function': 'minecraft:set_count', 'count': count})
    if 'tag' in d:
        funcs.append({'function': 'minecraft:set_nbt', 'tag': snbt(d['tag'])})
    if funcs:
        entry['functions'] = funcs
    return {'rolls': 1, 'entries': [entry]}


def table_for(items):
    return {'type': 'minecraft:chest', 'pools': [stack_entry(i) for i in items]}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()

    tables = {}      # table id -> json
    report = collections.defaultdict(lambda: collections.Counter())
    rewritten = 0
    for path in sorted(glob.glob(os.path.join(KJS_DATA, '*/structures/**/*.nbt'), recursive=True)):
        rel = os.path.relpath(path, KJS_DATA).split(os.sep)
        ns = rel[0]
        template = f"{ns}:{'/'.join(rel[2:])[:-4]}"
        raw = gzip.decompress(open(path, 'rb').read())
        name, root = P.nbt_read(raw)
        assert P.nbt_write(name, root) == raw, f'NBT round-trip mismatch: {template}'
        d = root.v
        palettes = d['palettes'].v if 'palettes' in d else [d['palette']]
        names = [[s.v['Name'].v for s in pal.v] for pal in palettes]
        changed = False
        for b in d['blocks'].v:
            bd = b.v
            nb = bd.get('nbt')
            if nb is None or 'LootTable' in nb.v or 'Items' not in nb.v or not nb.v['Items'].v:
                continue
            state = bd['state'].v
            block = next((n[state] for n in names if state < len(n) and n[state] in CONTAINERS), None)
            if block is None:
                continue
            tbl = table_for(nb.v['Items'].v)
            blob = json.dumps(tbl, sort_keys=True)
            tid = f"{ns}:chests/fixed/{hashlib.sha1(blob.encode()).hexdigest()[:10]}"
            tables[tid] = tbl
            nb.v['LootTable'] = P.S(tid)
            del nb.v['Items']
            report[template][block] += 1
            changed = True
            rewritten += 1
        if changed and not args.dry_run:
            with open(path, 'wb') as fh:
                fh.write(gzip.compress(P.nbt_write(name, root), mtime=0))

    if not args.dry_run:
        for tid, tbl in tables.items():
            ns, p = tid.split(':')
            out = os.path.join(KJS_DATA, ns, 'loot_tables', p + '.json')
            os.makedirs(os.path.dirname(out), exist_ok=True)
            with open(out, 'w') as fh:
                json.dump(tbl, fh, indent=2)
                fh.write('\n')
    by_mod = collections.Counter()
    for t, c in report.items():
        by_mod[t.split(':')[0]] += sum(c.values())
    for m, n in sorted(by_mod.items()):
        print(f'  {m}: {n}')
    print(f"{'would convert' if args.dry_run else 'converted'} {rewritten} containers in {len(report)} templates "
          f'-> {len(tables)} loot tables')


if __name__ == '__main__':
    main()

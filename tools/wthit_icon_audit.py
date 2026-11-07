#!/usr/bin/env python3
"""Report on the WTHIT icon audit: which blocks the block model icon draws, and which icons are still blank.

    python3 tools/wthit_icon_audit.py [wthit_icons.json] [--md] [--all]

Reads kubejs/exported/wthit_icons.json (client command /cdevdumpicons, or [debug.wthit] dumpIconsAtTitle on a
scratch client; Creative Devtools dump/WthitIconDump). Each block's default state has a verdict from
compat/BlockModelIcons:

  ITEM           its clone stack has a working item model: WTHIT's own icon, nothing to do
  MODEL          no item / missing item model, so the plugin draws the block's own model
  EXCLUDED       would be MODEL, but [wthit] iconExclude lists it
  SUBSTITUTE     [wthit] iconItems names an item for it
  NOT_MODEL      no item icon and the block isn't a baked model (air, fluids, block-entity renderers): blank
  MISSING_MODEL  no item icon and the block's model is missing too: blank (or purple)

In game the plugin also tries Forge's hit-aware pick block before the model (the dump has no hit, so a block
listed NOT_MODEL here can still get an item there: Supplementaries' skull pile). Blank icons for blocks you can
actually look at are the holes; most NOT_MODEL rows are technical blocks
(air, moving pistons, fluids, which WTHIT gives a bucket icon) you never see. The dump also lists every item
whose own model is missing (the purple cube in JEI and inventories), which no WTHIT plugin can fix.

--md prints markdown tables for feature_writeup/wthit_lines.md; --all lists every MODEL block, not counts.
Exit status 0 always: this is a report.
"""
import collections
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DUMP = os.path.join(ROOT, 'kubejs/exported/wthit_icons.json')
BLANK = ('NOT_MODEL', 'MISSING_MODEL')


def ns(block_id):
    return block_id.split(':', 1)[0]


def main(argv):
    md = '--md' in argv
    show_all = '--all' in argv
    paths = [a for a in argv if not a.startswith('--')]
    data = json.load(open(paths[0] if paths else DUMP, encoding='utf8'))
    blocks = data['blocks']

    by_verdict = collections.defaultdict(list)
    for b in blocks:
        by_verdict[b.get('verdict', 'ERROR')].append(b)

    print(f"wthit_icons.json from {data.get('generated')}: {len(blocks)} blocks")
    for v in ('ITEM', 'MODEL', 'EXCLUDED', 'SUBSTITUTE', 'NOT_MODEL', 'MISSING_MODEL', 'ERROR'):
        print(f'  {v:14} {len(by_verdict.get(v, []))}')
    print(f"  items with a missing model: {len(data.get('missingItemModels', []))}")

    def per_ns(rows):
        c = collections.Counter(ns(r['id']) for r in rows)
        return sorted(c.items(), key=lambda kv: (-kv[1], kv[0]))

    def table(title, rows, detail):
        print()
        if md:
            print(f'**{title}** ({len(rows)})\n')
            print('| Namespace | Blocks | Examples |')
            print('|---|---|---|')
        else:
            print(f'{title} ({len(rows)}):')
        groups = collections.defaultdict(list)
        for r in rows:
            groups[ns(r['id'])].append(r)
        for name, n in per_ns(rows):
            ids = [detail(r) for r in groups[name]]
            shown = ids if show_all else ids[:4]
            more = '' if len(shown) == len(ids) else f', +{len(ids) - len(shown)}'
            if md:
                print(f"| {name} | {n} | {', '.join(shown)}{more} |")
            else:
                print(f"  {name:28} {n:4}  {', '.join(shown)}{more}")

    def why(r):
        path = r['id'].split(':', 1)[1]
        if r.get('clone') and r.get('cloneModelMissing'):
            return f'{path} (item model missing)'
        return path

    table('MODEL: the plugin draws the block', by_verdict.get('MODEL', []), why)
    if by_verdict.get('EXCLUDED'):
        table('EXCLUDED by [wthit] iconExclude', by_verdict['EXCLUDED'], why)
    blank = [r for v in BLANK for r in by_verdict.get(v, [])]
    table('Still blank (no item icon, not drawable)', blank,
          lambda r: f"{r['id'].split(':', 1)[1]} [{r['shape'] if r['verdict'] == 'NOT_MODEL' else 'no model'}]")
    if by_verdict.get('ERROR'):
        table('ERROR (pick item needs a real world; check in game)', by_verdict['ERROR'],
              lambda r: r['id'].split(':', 1)[1])

    missing = data.get('missingItemModels', [])
    if missing:
        print()
        c = collections.Counter(ns(i) for i in missing)
        if md:
            print(f'**Items with a missing model** ({len(missing)})\n')
            print('| Namespace | Items | Examples |')
            print('|---|---|---|')
        else:
            print(f'Items with a missing model ({len(missing)}):')
        groups = collections.defaultdict(list)
        for i in missing:
            groups[ns(i)].append(i.split(':', 1)[1])
        for name, n in sorted(c.items(), key=lambda kv: (-kv[1], kv[0])):
            ids = groups[name] if show_all else groups[name][:4]
            more = '' if len(ids) == n else f', +{n - len(ids)}'
            print(f"| {name} | {n} | {', '.join(ids)}{more} |" if md else f"  {name:28} {n:4}  {', '.join(ids)}{more}")
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))

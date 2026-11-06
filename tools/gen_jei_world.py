#!/usr/bin/env python3
"""Write the generated rows of the world-interaction JEI tabs (feature_writeup/jei_coverage.md 4).

Every tools/jei_world/tab_*.py module returns rows per tab; this writes them, merged per tab, to
kubejs/assets/creativecommon/jei_world/<tab>/generated.json. Hand-written rows sit next to it in manual.json
and are never touched here. patcherMod's WorldInteractionJEI reads both on the client (row format in
compat/jei/world/WorldRow.java). A generated.json whose tab got no rows is deleted.

Inputs: mod jars' data/ (kubejs/data on top) and kubejs/exported/ (run /dumpworldjei and /dumploot first).

  python3 tools/gen_jei_world.py [--dry-run] [--only tab_module]

Exit 1 when a module warned (missing dump, unreadable file); the files are still written.
"""
import argparse
import importlib
import json
import sys
import traceback
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from jei_world.common import OUT, Context  # noqa: E402


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    ap.add_argument('--only', help='run one module, e.g. tab_weathering (others keep their files)')
    args = ap.parse_args()

    modules = sorted(p.stem for p in (Path(__file__).parent / 'jei_world').glob('tab_*.py'))
    if args.only:
        modules = [m for m in modules if m == args.only]
    ctx = Context()
    by_tab = {}
    failed = False
    for name in modules:
        try:
            mod = importlib.import_module(f'jei_world.{name}')
            result = mod.build(ctx)
        except Exception:
            print(f'! {name} failed:')
            traceback.print_exc()
            failed = True
            continue
        for tab, rows in result.items():
            by_tab.setdefault(tab, []).extend(rows)
            print(f'{name}: {tab} {len(rows)} rows')

    if not args.dry_run:
        for tab, rows in sorted(by_tab.items()):
            d = OUT / tab
            d.mkdir(parents=True, exist_ok=True)
            text = json.dumps({'_generated_by': 'tools/gen_jei_world.py', 'rows': rows}, indent=1, ensure_ascii=False) + '\n'
            (d / 'generated.json').write_text(text, encoding='utf-8')
        if not args.only:
            for old in OUT.glob('*/generated.json'):
                if old.parent.name not in by_tab:
                    old.unlink()
                    print(f'removed {old.relative_to(OUT)}')

    for w in ctx.warnings:
        print(f'WARNING {w}')
    total = sum(len(r) for r in by_tab.values())
    print(f'[gen_jei_world] {total} rows in {len(by_tab)} tabs' + (' (dry run)' if args.dry_run else ''))
    return 1 if (ctx.warnings or failed) else 0


if __name__ == '__main__':
    sys.exit(main())

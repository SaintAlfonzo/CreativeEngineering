#!/usr/bin/env python3
"""Check that every growing plant WTHIT leaves out has a rule in crop_growth.json.

    python3 tools/crop_growth_coverage.py [audit.json] [--md]

Reads kubejs/exported/crops/crop_growth_audit.json (from /dumpcropgrowth) and the rule file the
WTHIT plugin ships (development/patcherMod/.../assets/creativeengineering/crop_growth.json). Every
block the audit says WTHIT doesn't cover lands in one of:

  rule      a rule in crop_growth.json matches it, and the rule's properties / stages / numbers fit the block
  fallback  no rule, but the plugin's fallback (BushBlock, not a sapling, integer "age") picks it up
  skipped   not a crop: leaves, saplings, leaf piles, vines and kelp, decorations... with the reason
  UNKNOWN   none of the above - a new mod's plant. Add a rule (or a skip reason below) and rerun

Exit status 1 when anything is UNKNOWN or a rule doesn't fit its blocks. --md prints the catalog
tables used in feature_writeup/crop_growth.md.
"""
import fnmatch
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUDIT = os.path.join(ROOT, 'kubejs/exported/crops/crop_growth_audit.json')
RULES = os.path.join(ROOT, 'development/patcherMod/src/main/resources/assets/creativeengineering/crop_growth.json')


def simple(name):
    return name.rsplit('.', 1)[-1]


def skip_reason(row):
    """Why a block with a stage-like property is not a crop, or None."""
    props = {p['name'] for p in row['props']}
    classes = {simple(c) for c in row['classes']}
    if 'distance' in props and 'LeavesBlock' in classes:
        return 'leaves (distance to a log)'
    if 'SaplingBlock' in classes:
        return 'sapling (stage 0/1 is the tree-growth roll, WTHIT shows none for vanilla saplings either)'
    if 'layers' in props:
        return 'leaf pile / layer count'
    if 'bites' in props:
        return 'sliced food (bites left)'
    if props & {'flowers', 'mushrooms', 'potflower', 'pickles'}:
        return 'decoration or item count (flowers, pots, pickles)'
    if props & {'sand_age', 'weathering', 'mossing', 'sandiness'}:
        return 'weathering state'
    if props & {'fill_stage', 'storage'} or row['id'] in ('vinery:basket',):
        return 'container fill level'
    if 'GrowingPlantBlock' in classes or classes & {'CactusBlock', 'SugarCaneBlock', 'BambooBlock', 'ChorusFlowerBlock'}:
        return 'column plant / vine (age is a growth counter, nothing to harvest by stage)'
    if classes & {'FireBlock', 'FrostedIceBlock', 'MultifaceBlock', 'HalfTransparentBlock'} or 'ender_residue' in row['id']:
        return 'effect block (fire, ice, spray, residue)'
    if classes & {'GrassBlock', 'SpreadingSnowyDirtBlock'}:
        return 'grass spread counter'
    if 'UnripeCheeseWheelBlock' in classes:
        return 'maturing cheese, not a plant'
    if classes & {'MossyBlock', 'MossySlabBlock', 'MossyStairsBlock', 'MossyWallBlock', 'VerticalSlabBlock'}:
        return 'weathering state'
    if 'BigDripleafBlock' in classes:
        return 'tilt state'
    if 'StoneLanternBlock' in classes:
        return 'lantern state'
    return None


def fallback_ok(row):
    classes = {simple(c) for c in row['classes']}
    return ('BushBlock' in classes and 'SaplingBlock' not in classes
            and any(p['name'] == 'age' and p['type'] == 'IntegerProperty' for p in row['props']))


def rule_for(rules, block_id):
    for i, r in enumerate(rules):
        if any(fnmatch.fnmatchcase(block_id, g) for g in r['blocks'] if not g.startswith('#')):
            return i, r
    return None, None


def rule_problems(rule, row):
    """Ways the rule doesn't fit this block (empty = fine)."""
    out = []
    byname = {p['name']: p for p in row['props']}
    names = rule.get('properties') or [rule.get('property', 'age')]
    top = 0
    for n in names:
        p = byname.get(n)
        if p is None:
            out.append(f'no property {n}')
            continue
        if 'stages' in rule:
            missing = [s for s in rule['stages'] if s not in p.get('values', [])]
            if p['type'] == 'IntegerProperty' or missing:
                out.append(f'stages {missing or "on an integer property"} not in {n}')
        elif p['type'] != 'IntegerProperty':
            out.append(f'{n} is {p["type"]}, rule has no stages')
        else:
            top += p['max']
    limit = len(rule['stages']) - 1 if 'stages' in rule else top
    mx = rule.get('max', limit)
    if mx > limit or rule.get('harvest', mx) > mx or rule.get('empty', -1) >= mx:
        out.append(f'numbers: max {mx} (property top {limit}), harvest {rule.get("harvest")}, empty {rule.get("empty")}')
    for k, v in rule.get('requires', {}).items():
        if k not in byname and k not in ('persistent', 'waterlogged'):
            out.append(f'requires unknown property {k}')
    return out


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    md = '--md' in sys.argv
    audit = json.load(open(args[0] if args else AUDIT))
    rules = json.load(open(RULES))['rules']
    used = set()
    groups = {'rule': [], 'fallback': [], 'skipped': [], 'UNKNOWN': []}
    problems = []
    for row in audit['blocks']:
        if row['wthitNative']:
            continue
        i, rule = rule_for(rules, row['id'])
        if rule is not None:
            used.add(i)
            for p in rule_problems(rule, row):
                problems.append(f'{row["id"]}: rule {i + 1}: {p}')
            groups['rule'].append((row, i))
        elif fallback_ok(row):
            groups['fallback'].append((row, None))
        elif skip_reason(row):
            groups['skipped'].append((row, skip_reason(row)))
        else:
            groups['UNKNOWN'].append((row, None))
    dead = [i + 1 for i in range(len(rules)) if i not in used]

    native = sum(1 for r in audit['blocks'] if r['wthitNative'])
    print(f'audit: {audit["listed"]} growing blocks, {native} WTHIT-native, '
          f'{len(groups["rule"])} by rule, {len(groups["fallback"])} by fallback, '
          f'{len(groups["skipped"])} skipped, {len(groups["UNKNOWN"])} UNKNOWN')
    if md:
        print_md(groups)
    for row, _ in groups['fallback']:
        print(f'FALLBACK only: {row["id"]} (add a rule if its harvest stage is below max)')
    for row, _ in groups['UNKNOWN']:
        print(f'UNKNOWN: {row["id"]} props={[p["name"] for p in row["props"]]} class={simple(row["classes"][0])}')
    for p in problems:
        print('PROBLEM:', p)
    for n in dead:
        print(f'WARN: rule {n} matches no block in the audit')
    return 1 if groups['UNKNOWN'] or problems else 0


def print_md(groups):
    by_mod = {}
    for row, i in groups['rule'] + groups['fallback']:
        by_mod.setdefault(row['mod'], []).append((row, i))
    print('\n| Block | Property | Values | Rule |\n|---|---|---|---|')
    for mod in sorted(by_mod):
        for row, i in by_mod[mod]:
            p = ', '.join(f"{x['name']} {x['min']}-{x['max']}" if 'max' in x else f"{x['name']} ({'/'.join(x['values'])})"
                          for x in row['props'] if x['name'] in ('age', 'age1', 'growth', 'blockstate', 'type'))
            print(f"| `{row['id']}` | {p} | | {'fallback' if i is None else 'rule ' + str(i + 1)} |")
    print('\nSkipped:')
    why = {}
    for row, reason in groups['skipped']:
        why.setdefault(reason, []).append(row['id'])
    for reason, ids in sorted(why.items()):
        print(f'- {reason}: {len(ids)} ({", ".join(ids[:4])}{", ..." if len(ids) > 4 else ""})')


if __name__ == '__main__':
    sys.exit(main())

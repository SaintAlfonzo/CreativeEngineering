#!/usr/bin/env python3
"""Offline checks for More Mooblooms data (feature_writeup/mooblooms.md, MB-45).

    python3 tools/check_moobloom_data.py

Reads the variants, breeding rules and spawn rules the game would load: the mod's own
(development/moreMooblooms/src/main/resources/data and its packs/more_mooblooms/*/data switch packs) with the pack's
kubejs/data on top (same path wins), and checks:
- every variant (mooblooms.md 4.4): only known keys; a non-empty flower pool of block states (or {state,
  weight >= 1, chance 0-1, drop, optional, feed_weight >= 1}); feed and spawns_in_dark_on ids/#tags; any_weight and breed_weight >= 0;
  egg_colors are two hex colours
- a flower (item) in several variants' food is listed with each variant's share when a cow is fed it (by
  feed_weight)
- every variant has a texture (mod resources or kubejs/assets) and an en_us name
- the default variant (more_mooblooms:moobloom) exists
- breeding rules (4.8): parents and results are variants, chance 0-1, result weights >= 1,
  a pair claimed by two rules (the later one never applies: WARNING)
- spawn rules: biome ids exist in kubejs/exported/biome_data.json (/dumpbiomes), variant ids exist,
  a rule's percentages add up to 100 or less
Block/item ids are only checked for shape (namespace:path); the game logs unknown ones at load.
Exit code 1 on any problem.
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MOD_RES = os.path.join(ROOT, 'development', 'moreMooblooms', 'src', 'main', 'resources')
KUBEJS = os.path.join(ROOT, 'kubejs')
BIOMES = os.path.join(KUBEJS, 'exported', 'biome_data.json')
ID = re.compile(r'^[a-z0-9_.-]+:[a-z0-9_./-]+$')
STATE = re.compile(r'^[a-z0-9_.-]+:[a-z0-9_./-]+(\[[a-z0-9_=,]+\])?$')
COLOR = re.compile(r'^#?[0-9A-Fa-f]{6}$')

VARIANT_KEYS = {'flowers', 'feed', 'scale', 'any_weight', 'breed_weight', 'spawns_in_dark_on', 'egg_colors'}

problems = []
warnings = []


def problem(where, msg):
    problems.append(f'{where}: {msg}')


def warning(where, msg):
    warnings.append(f'{where}: {msg}')


def data_files(kind):
    """{resource id: path} for data/<ns>/<kind>/**.json, kubejs/data overriding the mod."""
    out = {}
    bases = [os.path.join(MOD_RES, 'data')]
    bases += sorted(glob.glob(os.path.join(MOD_RES, 'packs', 'more_mooblooms', '*', 'data')))  # core switch packs
    bases.append(os.path.join(KUBEJS, 'data'))
    for base in bases:
        if not os.path.isdir(base):
            continue
        for ns in sorted(os.listdir(base)):
            d = os.path.join(base, ns, kind)
            for dirpath, _, files in os.walk(d):
                for f in files:
                    if f.endswith('.json'):
                        rel = os.path.relpath(os.path.join(dirpath, f), d)[:-5].replace(os.sep, '/')
                        out[f'{ns}:{rel}'] = os.path.join(dirpath, f)
    return dict(sorted(out.items()))


def lang():
    keys = {}
    for base in (MOD_RES, KUBEJS):
        p = os.path.join(base, 'assets', 'more_mooblooms', 'lang', 'en_us.json')
        if os.path.exists(p):
            keys.update(json.load(open(p, encoding='utf8')))
    return keys


def pool_items(v):
    """The items a variant's pool and plain feed entries stand for (block id = item id unless drop is given)."""
    flowers = v.get('flowers')
    entries = flowers if isinstance(flowers, list) else [flowers]
    out = set()
    for e in entries:
        if isinstance(e, str):
            out.add(e.split('[')[0])
        elif isinstance(e, dict) and isinstance(e.get('state'), str):
            out.add(e.get('drop') or e['state'].split('[')[0])
    out.update(f for f in v.get('feed', []) if isinstance(f, str) and not f.startswith('#'))
    return out


def check_variant(vid, path, names):
    try:
        v = json.load(open(path, encoding='utf8'))
    except ValueError as e:
        problem(vid, f'bad JSON: {e}')
        return None
    for key in v:
        if key not in VARIANT_KEYS:
            problem(vid, f'unknown key "{key}"')
    flowers = v.get('flowers')
    if isinstance(flowers, dict) and 'state' not in flowers:
        problem(vid, 'flowers must be a block state, a {state, ...} entry or a list of them')
        flowers = None
    entries = flowers if isinstance(flowers, list) else ([flowers] if flowers is not None else [])
    if not entries:
        problem(vid, 'no flowers')
    for i, e in enumerate(entries):
        where = f'flowers[{i}]'
        if isinstance(e, str):
            e = {'state': e}
        if not isinstance(e, dict):
            problem(vid, f'{where} must be a block state or an object')
            continue
        if not STATE.match(str(e.get('state', ''))):
            problem(vid, f'{where}.state missing or not a block state')
        if not (isinstance(e.get('weight', 1), int) and e.get('weight', 1) >= 1):
            problem(vid, f'{where}.weight must be a whole number >= 1')
        if not 0 <= e.get('chance', 1.0) <= 1:
            problem(vid, f'{where}.chance outside 0-1')
        if 'drop' in e and not ID.match(str(e['drop'])):
            problem(vid, f'{where}.drop is not an item id')
        if not (isinstance(e.get('feed_weight', 1), int) and e.get('feed_weight', 1) >= 1):
            problem(vid, f'{where}.feed_weight must be a whole number >= 1')
        if 'optional' in e and not isinstance(e['optional'], bool):
            problem(vid, f'{where}.optional must be true/false')
    for f in v.get('feed', []):
        if not ID.match(f.lstrip('#')):
            problem(vid, f'feed entry {f} is not an id or #tag')
    for b in v.get('spawns_in_dark_on', []):
        if not ID.match(str(b).lstrip('#')):
            problem(vid, f'spawns_in_dark_on entry {b} is not a block id or #tag')
    for key in ('any_weight', 'breed_weight'):
        if v.get(key, 1) < 0:
            problem(vid, f'{key} below 0')
    colors = v.get('egg_colors')
    if colors is not None and (len(colors) != 2 or not all(COLOR.match(c) for c in colors)):
        problem(vid, 'egg_colors needs two #RRGGBB colours')

    ns, p = vid.split(':', 1)
    tex = os.path.join('assets', ns, 'textures', 'entity', 'moobloom', p + '.png')
    if not any(os.path.exists(os.path.join(b, tex)) for b in (MOD_RES, KUBEJS)):
        problem(vid, f'no texture {tex}')
    key = 'entity.more_mooblooms.moobloom.' + (p if ns == 'more_mooblooms' else f'{ns}.{p}')
    if key not in names:
        problem(vid, f'no en_us name {key}')
    return v


def feed_weights(v):
    """{item: feed_weight} for a variant's food (pool entries by drop item, feed items at 1)."""
    flowers = v.get('flowers')
    entries = flowers if isinstance(flowers, list) else [flowers]
    out = {}
    for e in entries:
        if isinstance(e, str):
            e = {'state': e}
        if isinstance(e, dict) and isinstance(e.get('state'), str):
            item = e.get('drop') or e['state'].split('[')[0]
            out[item] = max(out.get(item, 0), e.get('feed_weight', 1))
    for f in v.get('feed', []):
        if isinstance(f, str) and not f.startswith('#'):
            out.setdefault(f, 1)
    return out


def check_shared_flowers(loaded):
    owners = {}
    for vid, v in loaded.items():
        for item, w in feed_weights(v).items():
            owners.setdefault(item, []).append((vid, w))
    shared = []
    for item, vids in sorted(owners.items()):
        if len(vids) > 1:
            total = sum(w for _, w in vids)
            shared.append(f'{item}: ' + ', '.join(f'{vid.split(":", 1)[1]} {100 * w / total:.0f}%' for vid, w in vids))
    return shared


def breeding_rules(path):
    root = json.load(open(path, encoding='utf8'))
    return root['rules'] if isinstance(root, dict) and 'rules' in root else [root]


def check_breeding(files, known):
    seen = {}
    n = 0
    for fid, path in files.items():
        try:
            rules = breeding_rules(path)
        except (ValueError, KeyError, TypeError) as e:
            problem(fid, f'bad JSON: {e}')
            continue
        for i, rule in enumerate(rules):
            where = f'{fid} rule {i}'
            n += 1
            parents = rule.get('parents', [])
            if len(parents) != 2:
                problem(where, 'parents needs two variant ids')
                continue
            for p in parents:
                if p not in known:
                    problem(where, f'unknown parent {p}')
            if not 0 <= rule.get('chance', 0.5) <= 1:
                problem(where, 'chance outside 0-1')
            results = rule.get('results', [])
            if not results:
                problem(where, 'no results')
            for r in results:
                rv = r if isinstance(r, str) else r.get('variant')
                if rv not in known:
                    problem(where, f'unknown result {rv}')
                if isinstance(r, dict) and not (isinstance(r.get('weight', 1), int) and r.get('weight', 1) >= 1):
                    problem(where, f'result {rv} weight must be a whole number >= 1')
            pair = frozenset(known.get(p, p) for p in parents)
            if pair in seen:
                warning(where, f'pair {" + ".join(sorted(pair))} already handled by {seen[pair]}; this rule never applies')
            else:
                seen[pair] = where
    return n


def check_rules(path, variants, biomes):
    try:
        root = json.load(open(path, encoding='utf8'))
    except ValueError as e:
        problem(path, f'bad JSON: {e}')
        return 0
    n = 0
    for i, rule in enumerate(root.get('rules', [])):
        where = f'{os.path.relpath(path, ROOT)} rule {i}'
        for b in rule.get('biomes', []):
            if not b.startswith('#') and biomes is not None and b not in biomes:
                problem(where, f'unknown biome {b}')
        total = 0
        for e in rule.get('replace', []):
            v = e.get('variant')
            if v != 'any' and v not in variants:
                problem(where, f'unknown variant {v}')
            total += e.get('percent', 0)
        if total > 100:
            problem(where, f'adds up to {total}% (the game scales it down to 100)')
        n += 1
    return n


def main():
    names = lang()
    variants = data_files('moobloom_variants')
    loaded = {}
    for vid, path in variants.items():
        v = check_variant(vid, path, names)
        if v is not None:
            loaded[vid] = v
    shared = check_shared_flowers(loaded)
    if 'more_mooblooms:moobloom' not in variants:
        problem('variants', 'no more_mooblooms:moobloom (the default)')
    biomes = json.load(open(BIOMES)) if os.path.exists(BIOMES) else None
    known = set(variants)
    rules = sum(check_rules(p, known, biomes) for p in data_files('moobloom_spawns').values())
    combos = check_breeding(data_files('moobloom_breeding'), {v: v for v in variants})
    print(f'{len(variants)} variants, {combos} breeding rules, {rules} spawn rules'
          + ('' if biomes is not None else ' (no biome_data.json: biome ids not checked)'))
    for line in shared:
        print('  shared food', line)
    for w in warnings:
        print('  WARNING', w)
    for p in problems:
        print('  PROBLEM', p)
    print('OK' if not problems else f'{len(problems)} problem(s)')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())

#!/usr/bin/env python3
"""Nether blocks in Overworld biomes: turns kubejs/generation_scripts/worldgen/nether_worldgen_rules.json into kubejs/data
overrides (feature_writeup/nether_worldgen.md). Structures are handled by gen_nether_swaps.py instead.

    python3 tools/gen_nether_worldgen.py [--dry-run]

- surface: rewrites block results in Terralith's minecraft:overworld and minecraft:large_biomes noise settings and
  writes full copies to kubejs/data/minecraft/worldgen/noise_settings/ (a surface rule has no additive form). A rule
  touches a block result only where every biome condition on its path lies inside the rule's biomes. A weighted
  target becomes a per-block random mix: one vertical_gradient per choice and 16-block Y band, with the probability
  held at the weight's share inside each band (the gradient spans GRADIENT_SPAN blocks, so it drifts about 0.8 % across
  a band). "remove" replaces the rule with an empty sequence, so the next rule decides.
- features: copies a configured feature json from its jar with block states swapped (properties carried over) or
  dropped from a weighted state provider, or converted ("convert": "flower_patch" turns a nylium-only
  nether_forest_vegetation patch into a vanilla flower patch). Rules on one feature apply in order.
- literal: a whole data file written as given (for code-registered entries with no json to copy, such as Create's
  layered ores, which a datapack file of the same id overrides).
- remove_features: one forge:remove_features modifier per rule, steps read from the biomes' json.

Writes the list of generated files to kubejs/generation_scripts/worldgen/nether_worldgen_manifest.json; the next run
deletes generated files no longer needed. It never overwrites a kubejs/data file it didn't write.
Apply: restart the server (worldgen registries load with the world). Only newly generated chunks change.
"""
import argparse
import copy
import json
import re
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_crop_worldgen_table as C  # noqa: E402

INSTANCE = C.INSTANCE
WORLDGEN = INSTANCE / 'kubejs/generation_scripts/worldgen'
RULES = WORLDGEN / 'nether_worldgen_rules.json'
MANIFEST = WORLDGEN / 'nether_worldgen_manifest.json'
DATA = INSTANCE / 'kubejs/data'
NOISE_SETTINGS = ('overworld', 'large_biomes')
# Creative Worldgen's jar (moved from kubejs/data on 2026-11-06; ids unchanged). Instance-relative.
MODIFIER_DIR = 'development/creativeWorldgen/src/main/resources/data/creativeengineering/forge/biome_modifier/nether_worldgen'
BAND = 16
Y_MIN, Y_MAX = -64, 320
GRADIENT_SPAN = 1900       # anchors must stay within +-2032 (VerticalAnchor's absolute range)
STEPS = ['raw_generation', 'lakes', 'local_modifications', 'underground_structures', 'surface_structures',
         'strongholds', 'underground_ores', 'underground_decoration', 'fluid_springs', 'vegetal_decoration',
         'top_layer_modification']


def loads(raw):
    text = raw.decode('utf-8-sig', 'ignore') if isinstance(raw, bytes) else raw
    return json.loads(re.sub(r'//[^\n"]*$', '', text, flags=re.M))


def jar_file(path):
    """(jar name, parsed json) of the one mod jar holding `path`."""
    found = []
    for jar in sorted(C.MODS.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        if path in z.namelist():
            found.append((jar.name, loads(z.read(path))))
    if len(found) != 1:
        sys.exit(f'{path}: expected one jar, found {[j for j, _ in found]}')
    return found[0]


# ── surface rules ────────────────────────────────────────────────────────────

def block(name):
    return {'type': 'minecraft:block', 'result_state': {'Name': name}}


def mix(rule_id, choices):
    """Sequence that picks one of [(block, weight)] per block position at random."""
    names = [f'creativeengineering:surface_mix/{rule_id}/{i}' for i in range(len(choices) - 1)]
    bands = []
    for lo in range(Y_MIN, Y_MAX, BAND):
        c = lo + BAND // 2
        seq, left = [], sum(w for _, w in choices)
        for i, (b, w) in enumerate(choices[:-1]):
            p = w / left
            left -= w
            top = round(c + p * GRADIENT_SPAN)
            seq.append({'type': 'minecraft:condition',
                        'if_true': {'type': 'minecraft:vertical_gradient', 'random_name': names[i],
                                    'true_at_and_below': {'absolute': top - GRADIENT_SPAN},
                                    'false_at_and_above': {'absolute': top}},
                        'then_run': block(b)})
        seq.append(block(choices[-1][0]))
        body = {'type': 'minecraft:sequence', 'sequence': seq}
        if lo + BAND < Y_MAX:
            body = {'type': 'minecraft:condition',
                    'if_true': {'type': 'minecraft:not', 'invert': {
                        'type': 'minecraft:y_above', 'anchor': {'absolute': lo + BAND},
                        'surface_depth_multiplier': 0, 'add_stone_depth': False}},
                    'then_run': body}
        bands.append(body)
    return {'type': 'minecraft:sequence', 'sequence': bands}


def rewrite_surface(rule_tree, rules, counts):
    """Rewrites matching block results in place; counts[i] = results rule i changed."""
    def target(i, r):
        to = r['to']
        if to == 'remove':
            return {'type': 'minecraft:sequence', 'sequence': []}
        if isinstance(to, str):
            return block(to)
        return mix(f"{r['biomes'][0].replace(':', '/')}/{r['from'].split(':')[1]}", [(c['block'], c['weight']) for c in to])

    def walk(node, biomes):
        t = node.get('type', '').removeprefix('minecraft:')
        if t == 'sequence':
            node['sequence'] = [walk(ch, biomes) for ch in node['sequence']]
        elif t == 'condition':
            cond = node['if_true']
            ct = cond.get('type', '').removeprefix('minecraft:')
            if ct == 'biome':
                inner = set(cond['biome_is']) if biomes is None else biomes & set(cond['biome_is'])
            else:
                inner = biomes
            node['then_run'] = walk(node['then_run'], inner)
        elif t == 'block' and biomes:
            for i, r in enumerate(rules):
                if node['result_state']['Name'] == r['from'] and biomes <= set(r['biomes']):
                    counts[i] += 1
                    return target(i, r)
        return node

    return walk(rule_tree, None)


# ── features ─────────────────────────────────────────────────────────────────

PREDICATE_KEYS = {'target', 'predicate', 'blocks', 'can_replace', 'replaceable', 'matching_blocks', 'can_grow_through'}


def rewrite_feature(data, frm, to):
    """(new data, changes) with every placed state `frm` swapped or dropped."""
    data = copy.deepcopy(data)
    n = 0

    def walk(v):
        nonlocal n
        if isinstance(v, list):
            keep = []
            for x in v:
                if to == 'remove' and isinstance(x, dict) and isinstance(x.get('data'), dict) and x['data'].get('Name') == frm:
                    n += 1
                    continue
                keep.append(walk(x))
            return keep
        if isinstance(v, dict):
            if 'predicate_type' in v:
                return v
            if v.get('Name') == frm:
                if to == 'remove':
                    sys.exit(f'{frm}: "remove" works only on weighted state provider entries')
                n += 1
                return {**v, 'Name': to}
            return {k: (x if k in PREDICATE_KEYS else walk(x)) for k, x in v.items()}
        return v

    return walk(data), n


def convert_feature(data, to):
    """flower_patch: a nether_forest_vegetation feature (places only on nylium) as a vanilla flower patch with the same
    tries and spread (NetherForestVegetationFeature: width^2 tries, offsets nextInt(w) - nextInt(w))."""
    if to != 'flower_patch' or data.get('type', '').removeprefix('minecraft:') != 'nether_forest_vegetation':
        sys.exit(f'convert {to}: unsupported for feature type {data.get("type")}')
    cfg = data['config']
    w, h = cfg.get('spread_width', 8), cfg.get('spread_height', 4)
    return {'type': 'minecraft:flower', 'config': {
        'tries': w * w, 'xz_spread': w - 1, 'y_spread': h - 1,
        'feature': {'feature': {'type': 'minecraft:simple_block', 'config': {'to_place': cfg['state_provider']}},
                    'placement': [{'type': 'minecraft:block_predicate_filter',
                                   'predicate': {'type': 'minecraft:matching_blocks', 'blocks': ['minecraft:air']}}]}}}


def steps_of(feature, biomes, biome_files):
    out = set()
    for b in biomes:
        for i, step in enumerate(biome_files.get(b, (None, {}))[1].get('features', [])):
            if feature in step:
                out.add(STEPS[i])
    return sorted(out, key=STEPS.index)


# ── main ─────────────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()
    rules = json.loads(RULES.read_text())
    old = set(json.loads(MANIFEST.read_text())['files']) if MANIFEST.exists() else set()
    out, problems = {}, []

    srules = rules.get('surface', [])
    if srules:
        for ns in NOISE_SETTINGS:
            jar, doc = jar_file(f'data/minecraft/worldgen/noise_settings/{ns}.json')
            counts = [0] * len(srules)
            doc['surface_rule'] = rewrite_surface(doc['surface_rule'], srules, counts)
            for r, n in zip(srules, counts):
                print(f'surface {ns}: {r["note"]}: {n} block rule(s)')
                if not n:
                    problems.append(f'surface rule matched nothing in {ns}: {r["note"]}')
            out[f'kubejs/data/minecraft/worldgen/noise_settings/{ns}.json'] = doc

    for r in rules.get('features', []):
        ns, _, path = r['feature'].partition(':')
        rel = f'data/{ns}/worldgen/configured_feature/{path}.json'
        key = f'kubejs/{rel}'
        if key in out:
            data = out[key]
        else:
            jar, data = jar_file(rel)
        if 'convert' in r:
            data = convert_feature(data, r['convert'])
            print(f'feature {r["feature"]}: {r["note"]}: converted to {r["convert"]}')
        else:
            data, n = rewrite_feature(data, r['from'], r['to'])
            print(f'feature {r["feature"]}: {r["note"]}: {n} state(s)')
            if not n:
                problems.append(f'feature rule matched nothing: {r["note"]}')
        out[key] = data

    for r in rules.get('literal', []):
        # a path under development/ is instance-relative (a jar's resources); anything else is under kubejs/
        out[r['path'] if r['path'].startswith('development/') else f'kubejs/{r["path"]}'] = r['json']
        print(f'literal {r["path"]}: {r["note"]}')

    if rules.get('remove_features'):
        _, biome_files, placed, _, _ = C.load_all()
        for r in rules['remove_features']:
            missing = [f for f in r['features'] if f not in placed]
            if missing:
                problems.append(f'{r["name"]}: unknown placed features {missing}')
            steps = sorted({s for f in r['features'] for s in steps_of(f, r['biomes'], biome_files)}, key=STEPS.index)
            mod = {'type': 'forge:remove_features', 'biomes': r['biomes'], 'features': r['features']}
            if steps:
                mod['steps'] = steps
            out[f'{MODIFIER_DIR}/{r["name"]}.json'] = mod
            print(f'remove {r["name"]}: {len(r["features"])} feature(s) from {len(r["biomes"])} biome(s), steps {steps}')

    for key in out:
        if (INSTANCE / key).exists() and key not in old:
            problems.append(f'{key} exists and is not ours (not in {MANIFEST.name}): refusing to overwrite')
    if problems:
        print('\n'.join('ERROR: ' + p for p in problems))
        sys.exit(1)
    if args.dry_run:
        print(f'dry run: {len(out)} file(s), {len(old - set(out))} to delete')
        return
    for key, data in out.items():
        p = INSTANCE / key
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(data, indent=2) + '\n')
    for key in sorted(old - set(out)):
        (INSTANCE / key).unlink(missing_ok=True)
        print(f'removed {key}')
    MANIFEST.write_text(json.dumps({'_readme': 'Written by tools/gen_nether_worldgen.py: the kubejs/data overrides it '
                                    'owns. Do not edit.', 'files': sorted(out)}, indent=1) + '\n')
    print(f'{len(out)} file(s) -> kubejs/data; manifest {MANIFEST.relative_to(INSTANCE)}')


if __name__ == '__main__':
    main()

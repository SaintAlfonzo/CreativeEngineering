#!/usr/bin/env python3
"""Let wild plants, bushes and trees generate on Immersive Weathering soils as well as on grass.

Immersive Weathering lays its humus (Dark Forest), fluvisol + silt (swamps) and vertisol (badlands, desert)
patches at raw_generation, before any vegetation. Plant features whose ground test names
minecraft:grass_block outright (vanilla pumpkin/melon/berry patches, Vinery grape bushes, Geophilic grass,
Terralith flowers/grass/trees, Neapolitan strawberries/adzuki, Fruits Delight bushes) then skip every block
of those patches, although the plants themselves survive on them (the soils are in #minecraft:dirt).
Cryosol needs nothing: it is placed at top_layer_modification, after vegetation, and only over snow blocks.

For every placed/configured feature in the mod jars with a block predicate that tests the block below
(offset [0,-1,0]) against a block list containing minecraft:grass_block, this writes an override to
kubejs/data/<ns>/worldgen/<kind>/<path>.json in which that predicate becomes
    any_of(<the original list>, matching_block_tag #creativeengineering:worldgen/grass_like_soils)
(the tag is filled in kubejs/data/creativeworldgen/kubejs/tags/blocktags.js). The only other change is STATE_SWAPS (merged blocks).
Features registered in Java (no JSON in the jar) are rebuilt from the decompiled source in JAVA_BASES.

Snow-layer, lake and snow-removal features also test grass, but they shape terrain rather than place
plants; EXCLUDE leaves them alone. The pack's own creativeengineering:wild/* copies are hand-written and
take the tag directly.

Writes the list of generated files to kubejs/generation_scripts/worldgen/soil_compat_manifest.json, and on
the next run deletes generated files that are no longer needed. It never overwrites a kubejs/data file
it didn't write.

  python3 tools/gen_soil_compat.py [--dry-run]

Apply: restart the server (worldgen registries are read at world load). Only newly generated chunks change.
"""
import argparse
import copy
import glob
import json
import re
import sys
import zipfile
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
DATA = INSTANCE / 'kubejs/data'
MANIFEST = INSTANCE / 'kubejs/generation_scripts/worldgen/soil_compat_manifest.json'
TAG = 'creativeengineering:worldgen/grass_like_soils'
GRASS = 'minecraft:grass_block'
BELOW = [0, -1, 0]
# Block states the overrides it writes swap as well: blocks purged by an item merge (feature_writeup/item_merges.md).
# Vintage Delight's salt patch lays a salt block under its layers; since the salt merge it is The Salt's raw rock salt
# block, which does not dissolve (patcherMod block.SaltBlockTicks), so shoreline patches stay.
STATE_SWAPS = {'vintagedelight:salt_block': 'salt:raw_rock_salt_block'}
KINDS = ('placed_feature', 'configured_feature')

# Terrain shaping, not plants: snow layers/fixes, lakes, snow removal.
EXCLUDE = re.compile(r'(^|/)(snowy/|.*lake|.*remove_some_snow|.*fix_snow|.*fix_grass)')


def state(name, props=None):
    s = {'Name': name}
    if props:
        s['Properties'] = props
    return s


def simple_patch(tries, xz, y, feature_type, to_place, below_blocks):
    """FeatureUtils.simplePatchConfiguration / PlacementUtils.filtered(..., simplePatchPredicate(blocks))."""
    return {
        'type': 'minecraft:random_patch',
        'config': {
            'tries': tries, 'xz_spread': xz, 'y_spread': y,
            'feature': {
                'feature': {'type': feature_type, 'config': {
                    'to_place': {'type': 'minecraft:simple_state_provider', 'state': to_place}}},
                'placement': [{'type': 'minecraft:block_predicate_filter', 'predicate': {
                    'type': 'minecraft:all_of', 'predicates': [
                        {'type': 'minecraft:matching_blocks', 'blocks': ['minecraft:air']},
                        {'type': 'minecraft:matching_blocks', 'offset': BELOW, 'blocks': below_blocks}]}}],
            },
        },
    }


# Java-registered configured features (decompiled 2026-10-05). Keep in step with the mod versions.
JAVA_BASES = {
    # neapolitan-1.19.2-4.1.0 NeapolitanFeatures.NeapolitanConfiguredFeatures
    'neapolitan:patch_strawberry_bush': simple_patch(512, 5, 3, 'neapolitan:strawberry_bush',
        state('neapolitan:strawberry_bush'), [GRASS, 'minecraft:coarse_dirt']),
    'neapolitan:patch_adzuki_sprouts': simple_patch(256, 3, 2, 'neapolitan:adzuki_sprouts',
        state('neapolitan:adzuki_sprouts'), [GRASS]),
    # fruitsdelight-0.5.11 FDBushes.registerConfigs: simplePatchConfiguration(SIMPLE_BLOCK, bush age 4, [grass], 20)
    'fruitsdelight:blueberry_bush': simple_patch(20, 7, 3, 'minecraft:simple_block',
        state('fruitsdelight:blueberry_bush', {'age': '4'}), [GRASS]),
    'fruitsdelight:cranberry_bush': simple_patch(20, 7, 3, 'minecraft:simple_block',
        state('fruitsdelight:cranberry_bush', {'age': '4'}), [GRASS]),
    'fruitsdelight:lemon_tree': simple_patch(20, 7, 3, 'minecraft:simple_block',
        state('fruitsdelight:lemon_tree', {'age': '4'}), [GRASS]),
}

# Data the Backported Vinery port serves at runtime from its donor jars (vinery_upstream/, never in mods/; the port
# translates 1.5.5 data to 1.19.2 in memory, alcohol.md B4.8). Only the grape bush patches are read: they pass through
# the translation unchanged, while other 1.5.5 worldgen (trees) is rewritten by the port and must not be copied raw.
DONOR_JARS = {
    'vinery_upstream/letsdo-vinery-fabric-1.5.5.jar': re.compile(r'^vinery:(taiga_|savanna_)?(red|white)_grape_bush$'),
}

# Java-only features that test grass_block inside the feature code; no JSON override can reach them.
# None of them generates in a biome that has an Immersive Weathering soil, so they are left alone.
JAVA_UNREACHABLE = {
    'neapolitan:mint_pond': 'MintPondFeature: flat grass_block with grass/dirt around (cold grassy mountains)',
    'neapolitan:banana_plant': 'BananaPlantFeature: grass_block, sand or gravel (jungles, warm wet forests)',
}


def blocks_of(pred):
    b = pred.get('blocks')
    return [b] if isinstance(b, str) else list(b or [])


def is_target(o):
    return (isinstance(o, dict) and o.get('type') == 'minecraft:matching_blocks'
            and list(o.get('offset') or [0, 0, 0]) == BELOW and GRASS in blocks_of(o))


def patch(o):
    """Return (new object, number of predicates widened)."""
    if is_target(o):
        return {'type': 'minecraft:any_of', 'predicates': [
            o, {'type': 'minecraft:matching_block_tag', 'offset': BELOW, 'tag': TAG}]}, 1
    if isinstance(o, dict):
        if (o.get('type') == 'minecraft:any_of'
                and any(isinstance(p, dict) and p.get('tag') == TAG for p in o.get('predicates', []))):
            return o, 0
        n, out = 0, {}
        for k, v in o.items():
            out[k], c = patch(v)
            n += c
        if out.get('Name') in STATE_SWAPS:
            out['Name'] = STATE_SWAPS[out['Name']]
        return out, n
    if isinstance(o, list):
        n, out = 0, []
        for v in o:
            nv, c = patch(v)
            out.append(nv)
            n += c
        return out, n
    return o, 0


def jar_sources():
    found = {}
    for jar in sorted(glob.glob(str(INSTANCE / 'mods/*.jar'))):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            m = re.match(r'data/([^/]+)/worldgen/(placed_feature|configured_feature)/(.+)\.json$', n)
            if not m:
                continue
            key = (m[2], f'{m[1]}:{m[3]}')
            try:
                body = json.loads(z.read(n).decode('utf8'))
            except ValueError:
                print(f'  (unparseable, skipped: {Path(jar).name} {n})')
                continue
            found.setdefault(key, []).append((Path(jar).name, body))
    for rel, wanted in DONOR_JARS.items():
        jar = INSTANCE / rel
        if not jar.exists():
            print(f'  ! donor jar missing: {rel}; its features are not widened')
            continue
        z = zipfile.ZipFile(jar)
        for n in z.namelist():
            m = re.match(r'data/([^/]+)/worldgen/(placed_feature|configured_feature)/(.+)\.json$', n)
            if m and wanted.match(f'{m[1]}:{m[3]}'):
                found.setdefault((m[2], f'{m[1]}:{m[3]}'), []).append((jar.name, json.loads(z.read(n).decode('utf8'))))
    return found


def out_path(kind, rid):
    ns, path = rid.split(':', 1)
    return DATA / ns / 'worldgen' / kind / f'{path}.json'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dry-run', action='store_true')
    args = ap.parse_args()

    old = set(json.loads(MANIFEST.read_text())['files']) if MANIFEST.exists() else set()
    sources = jar_sources()
    for rid, body in JAVA_BASES.items():
        if ('configured_feature', rid) in sources:
            print(f'  ! {rid} now ships as JSON in its jar; the JAVA_BASES entry is ignored')
        else:
            sources[('configured_feature', rid)] = [('Java (JAVA_BASES)', body)]

    writes = {}
    for (kind, rid), entries in sorted(sources.items()):
        if EXCLUDE.search(rid.split(':', 1)[1]):
            continue
        patched = [(src, *patch(copy.deepcopy(body))) for src, body in entries]
        if not any(n for _, _, n in patched):
            continue
        if len(entries) > 1:
            names = ', '.join(e[0] for e in entries)
            print(f'  ! {kind} {rid} is in several jars ({names}); load order decides which wins, skipped')
            continue
        src, new, n = patched[0]
        rel = str(out_path(kind, rid).relative_to(INSTANCE))
        if out_path(kind, rid).exists() and rel not in old:
            print(f'  ! {rel} exists and was not written by this script; left alone')
            continue
        writes[rel] = (new, src, kind, rid, n)

    print(f'{len(writes)} features widened to #{TAG}:')
    for rel, (_, src, kind, rid, n) in sorted(writes.items(), key=lambda w: (w[1][3], w[1][2])):
        print(f'  {rid:55} {kind:19} {n} test(s)  [{src}]')
    for rid, why in JAVA_UNREACHABLE.items():
        print(f'  not reachable from JSON: {rid} ({why})')

    stale = sorted(old - set(writes))
    for rel in stale:
        print(f'  removed {rel} (no longer needed)')
    if args.dry_run:
        print('dry run: nothing written')
        return 0

    for rel in stale:
        p = INSTANCE / rel
        if p.exists():
            p.unlink()
    for rel, (body, *_rest) in writes.items():
        p = INSTANCE / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(body, indent=2) + '\n')
    MANIFEST.write_text(json.dumps({
        '_readme': 'Written by tools/gen_soil_compat.py: the kubejs/data overrides it owns. Do not edit.',
        'tag': TAG,
        'files': sorted(writes),
    }, indent=2) + '\n')
    print(f'wrote {len(writes)} files, manifest {MANIFEST.relative_to(INSTANCE)}')
    return 0


if __name__ == '__main__':
    sys.exit(main())

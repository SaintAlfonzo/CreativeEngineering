#!/usr/bin/env python3
"""Nether blocks the Overworld places by itself: surface rules, biome features, and structures after the swaps
(feature_writeup/nether_worldgen.md, the decision table built from this scan).

    python3 tools/nether_worldgen_scan.py [--json OUT] [--no-structures]

Four sources:
- Surface rules: Terralith's minecraft:overworld noise settings, or the pack's copy from gen_nether_worldgen.py when
  it exists (Tectonic only adds Terratonic density functions when Terralith is present). Every block rule whose result is a Nether block, with the biome conditions on its path
  (intersected; "not biome" subtracts) and the other conditions (noise, depth, Y) as text.
- Biome features: biome json features (Terralith ships every Overworld biome; deep dark and mangrove swamp are
  vanilla code biomes with no Nether features), plus forge:add_features modifiers, minus forge:remove_features.
  Placed and configured json is walked for block states ("Name"), skipping rule tests and block predicates (what a
  feature checks, not what it places). Features registered in Java have no json: JAVA_FEATURES restates the ones that
  place Nether blocks (decompiled); every other json-less feature an Overworld biome gets is listed so nothing hides.
- Code-added: carvers and Create-style layered ores, which no biome json or modifier names (CODE_ADDED, decompiled).
  Found 2026-10-19 by searching every mod jar's worldgen classes for Nether Blocks fields; rerun that search
  (nether_fields() over all jars) after adding worldgen mods.
- Structures: nether_blocks_scan.scan() with nether_block_swaps.json applied (what TemplateBlockSwaps leaves, plus
  the Nether blocks the swaps themselves put in, such as magma and basalt).
- Structure processors: every worldgen/processor_list json (jars, then kubejs/data wins). A processor can place a
  block its templates don't hold, so the template scan can't see it: the Nether blocks a processor puts out
  (output_state of minecraft:rule, any other Name outside predicate keys), per list. Mod processor classes that
  hardcode blocks are restated in PROCESSOR_CLASSES (decompiled).

"Nether block" is nether_blocks_scan.classify (minecraft: blocks only, tiers 1 to 3). Smooth basalt is not one.
Writes feature_writeup/nether_worldgen_scan.md.
"""
import glob
import json
import os
import re
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_crop_worldgen_table as C  # noqa: E402
import loot_breakdown_scan as S  # noqa: E402
import nether_blocks_scan as N  # noqa: E402
from pack_paths import loads_lenient  # noqa: E402  (comments anywhere: Terralith's magma_veins `"count": 0 //8`)

ROOT = C.INSTANCE
OUT_MD = ROOT / 'feature_writeup/nether_worldgen_scan.md'
SURFACE_JAR = 'Terralith'
SURFACE_PATH = 'data/minecraft/worldgen/noise_settings/overworld.json'

# Placed features registered in Java that place Nether blocks (decompiled 1.19.2 jars). blocks, rate text, notes.
VC = 'Volcanic Caverns 1.2.2 (VCPlacedFeatures / VCConfiguredFeatures)'
JAVA_FEATURES = {
    'volcanic_caverns:basalt_pillar': (['minecraft:basalt'], '4 per chunk', 'Y bottom+9 to bottom+24',
                                       f'pillar from cave floor to ceiling; {VC}'),
    'volcanic_caverns:basalt_spike': (['minecraft:basalt'], '2 to 8 per chunk', 'Y bottom+2 to bottom+28',
                                      f'large dripstone-shaped spikes; {VC}'),
    'volcanic_caverns:delta': (['minecraft:magma_block'], '6 per chunk', 'Y bottom+8 to bottom+18, biased low',
                               f'lava delta (vanilla delta feature) with a magma rim; {VC}'),
    'volcanic_caverns:underground_magma': (['minecraft:magma_block'], '10 per chunk', 'Y bottom+8 to bottom+15, biased low',
                                           f'ore blob size 33 in natural stone; {VC}'),
}
# Json-less vanilla 1.19.2 code features Overworld biomes list; none places a Nether block.
JAVA_CLEAN_PREFIXES = ('minecraft:ore_', 'minecraft:patch_', 'minecraft:trees_', 'minecraft:spring_',
                       'minecraft:iceberg_', 'minecraft:lake_lava')
JAVA_CLEAN = {'minecraft:amethyst_geode', 'minecraft:cave_vines', 'minecraft:dripstone_cluster', 'minecraft:glow_lichen',
              'minecraft:mushroom_island_vegetation', 'minecraft:rooted_azalea_tree', 'minecraft:spore_blossom',
              'minecraft:warm_ocean_vegetation', 'volcanic_caverns:extra_coal_ore', 'volcanic_caverns:extra_diamond_ore',
              'volcanic_caverns:underground_smooth_basalt', 'volcanic_caverns:lava_spring'}
# Other json-less mod features are checked by jar: worldgen classes that reference a Nether Blocks field (SRG name).
# Hits read by hand and found to only test for the block (never place it):
JAVA_CHECKED = {
    'upgrade_aquatic': 'PrismarineCoralFeature only looks for magma/obsidian below (UnderwaterCanyonWorldCarver: CODE_ADDED)',
    'farmersrespite': 'FRGeneration: coffee bush placement predicate (on basalt, Nether only)',
}
# Vanilla feature types that hardcode the block they place (no block state in the json)
TYPE_BLOCKS = {
    'minecraft:underwater_magma': ['minecraft:magma_block'], 'minecraft:basalt_columns': ['minecraft:basalt'],
    'minecraft:basalt_pillar': ['minecraft:basalt'], 'minecraft:glowstone_blob': ['minecraft:glowstone'],
    'minecraft:weeping_vines': ['minecraft:weeping_vines', 'minecraft:weeping_vines_plant', 'minecraft:nether_wart_block'],
    'minecraft:twisting_vines': ['minecraft:twisting_vines', 'minecraft:twisting_vines_plant'],
}
# Added in code, so no biome json or modifier names them: carvers, and Create-style layered ores (Create's
# OreFeatureConfigEntry adds its own biome modifier). id: (source, blocks, biome spec, rate text, notes)
CODE_ADDED = {
    'upgrade_aquatic:underwater_canyon': ('Java carver', ['minecraft:magma_block'], '#minecraft:is_ocean',
                                          '1 in 30 chunks starts one',
                                          'canyon Y 38 to 58; where it reaches Y 10 the floor is 25% magma, 75% obsidian '
                                          '(UnderwaterCanyonWorldCarver, Upgrade Aquatic 5.1.0)'),
    'create_dd:striated_eroded_overworld': ('Java (Create layers)', ['minecraft:glowstone'], '#minecraft:is_overworld',
                                            '1 in 48 chunks', 'Y -64 to 0, layered blob size 24 (breccia, gabbro, '
                                            'granite, glowstone 1-2 thick: 1 layer weight in 5; DesiresLayerPatterns.'
                                            'SPACE_ROCK, Create: Dreams & Desires 1.2c). Create\'s own Overworld '
                                            'layers hold no Nether block (its soul sand/basalt layers are Nether only)'),
}
# Mod structure processor classes that place or touch Nether blocks in code (no json to read). id: note
PROCESSOR_CLASSES = {
    'betterdungeons:nether_block_processor': 'only removes Nether blocks from YUNG\'s Better Dungeons pieces',
    'betterfortresses:item_frame_processor': 'Better Nether Fortresses item frames (glowstone dust and the like as '
                                             'frame items, not blocks); Nether only',
}
MAPPING_GLOB = '~/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.19.2-*/srg_to_official_1.19.2.tsrg'

# keys whose subtree is a test of existing blocks, not something placed
PREDICATE_KEYS = {'target', 'predicate', 'blocks', 'can_replace', 'cannot_replace', 'replaceable', 'matching_blocks',
                  'can_grow_through', 'valid_base_block', 'can_place_on', 'can_survive_on', 'target_condition',
                  'cannot_replace_blocks', 'replaceable_blocks', 'block_predicate'}



# ── surface rules ────────────────────────────────────────────────────────────

def anchor(a):
    (k, v), = a.items()
    return {'absolute': f'{v}', 'above_bottom': f'bottom+{v}', 'below_top': f'top-{v}'}[k]


def cond_text(c):
    t = c.get('type', '').removeprefix('minecraft:')
    if t == 'noise_threshold':
        return f"noise {c['noise'].removeprefix('minecraft:')} {c.get('min_threshold')}..{c.get('max_threshold')}"
    if t == 'stone_depth':
        extra = ' + surface depth' if c.get('add_surface_depth') else ''
        sec = f", secondary {c['secondary_depth_range']}" if c.get('secondary_depth_range') else ''
        return f"{c['surface_type']} depth {c.get('offset', 0)}{extra}{sec}"
    if t == 'y_above':
        mul = f" (+surface x{c['surface_depth_multiplier']})" if c.get('surface_depth_multiplier') else ''
        return f"Y >= {anchor(c['anchor'])}{mul}"
    if t == 'vertical_gradient':
        return f"gradient {anchor(c['true_at_and_below'])} to {anchor(c['false_at_and_above'])}"
    if t == 'water':
        return f"water offset {c.get('offset')}"
    if t == 'not':
        return f"not ({cond_text(c['invert'])})"
    if t == 'biome':
        bs = c['biome_is']
        return 'biome ' + ', '.join(b.split(':')[1] for b in bs[:4]) + (' ...' if len(bs) > 4 else '')
    return t


def surface_hits(rule, overworld):
    """[(block, biomes, [condition text])] for every Nether block result in the surface rule tree."""
    hits = []

    def walk(node, conds):
        t = node.get('type', '').removeprefix('minecraft:')
        if t == 'sequence':
            for child in node['sequence']:
                walk(child, conds)
        elif t == 'condition':
            walk(node['then_run'], conds + [node['if_true']])
        elif t == 'block':
            name = node['result_state']['Name']
            if N.classify(name):
                bs, other = set(overworld), []
                for c in conds:
                    ct = c.get('type', '').removeprefix('minecraft:')
                    if ct == 'biome':
                        bs &= set(c['biome_is'])
                    elif ct == 'not' and c['invert'].get('type', '').removeprefix('minecraft:') == 'biome':
                        bs -= set(c['invert']['biome_is'])
                    else:
                        other.append(cond_text(c))
                hits.append((name, sorted(bs), other))

    walk(rule, [])
    return hits


def load_surface_rule():
    override = ROOT / 'kubejs' / SURFACE_PATH     # gen_nether_worldgen.py's copy wins over Terralith's
    if override.exists():
        return loads_lenient(override.read_text())['surface_rule']
    for jar in sorted(C.MODS.glob(f'{SURFACE_JAR}*.jar')):
        return loads_lenient(zipfile.ZipFile(jar).read(SURFACE_PATH))['surface_rule']
    sys.exit(f'no {SURFACE_JAR} jar in mods/')


# ── jars ─────────────────────────────────────────────────────────────────────

_JARS = {}


def jars():
    """[(jar path, ZipFile, set of mod ids)] for every mod jar."""
    if not _JARS:
        for jar in sorted(C.MODS.glob('*.jar')):
            try:
                z = zipfile.ZipFile(jar)
            except zipfile.BadZipFile:
                continue
            ids = set()
            if 'META-INF/mods.toml' in z.namelist():
                toml = z.read('META-INF/mods.toml').decode('utf-8', 'ignore')
                for block in re.split(r'^\s*\[\[', toml, flags=re.M):       # [[mods]] blocks, not [[dependencies.x]]
                    if block.startswith('mods]]'):
                        ids |= set(re.findall(r'^\s*modId\s*=\s*"([^"]+)"', block, re.M))
            _JARS[jar] = (z, ids)
    return [(j, z, ids) for j, (z, ids) in _JARS.items()]


_TEMPLATES = {}


def template_nether(loc):
    """Nether blocks (classify) in the structure template a feature config names."""
    if loc not in _TEMPLATES:
        ns, _, path = loc.partition(':')
        name, found = f'data/{ns}/structures/{path}.nbt', set()
        for _, z, _ in jars():
            if name in z.namelist():
                d = S.read_template(z.read(name)) or {}
                pal = d.get('palette') or (d.get('palettes') or [[]])[0]
                found = {p.get('Name') for p in pal if N.classify(p.get('Name', ''))}
        _TEMPLATES[loc] = found
    return _TEMPLATES[loc]


def nether_fields():
    """{SRG field name: official name} for the Blocks fields classify() counts as Nether, or {} without the mapping."""
    paths = sorted(glob.glob(os.path.expanduser(MAPPING_GLOB)))
    if not paths:
        return {}
    out, inside = {}, False
    for line in open(paths[-1]):
        if not line.startswith('\t'):
            inside = line.startswith('net/minecraft/world/level/block/Blocks ')
        elif inside and not line.startswith('\t\t'):
            p = line.split()
            if len(p) >= 2 and N.classify('minecraft:' + p[1].lower()):
                out[p[0]] = p[1].lower()
    return out


def jar_nether_refs(ns, fields):
    """{class: [blocks]} for worldgen classes of the jar holding mod id `ns` that reference a Nether Blocks field."""
    hits = {}
    for jar, z, ids in jars():
        if ns not in ids:
            continue
        for n in z.namelist():
            if n.endswith('.class') and re.search(r'(?i)world|gen|feature|biome|placement|decorat', n):
                refs = {fields[s.decode()] for s in set(re.findall(rb'f_\d+_', z.read(n))) if s.decode() in fields}
                if refs:
                    hits[n.rsplit('/', 1)[-1]] = sorted(refs)
    return hits


# ── processors ───────────────────────────────────────────────────────────────

# processor keys naming the block a processor looks for, not what it places (YUNG's block_replace_processor target)
PROC_INPUT_KEYS = {'target_block', 'input_block', 'input_state', 'input', 'targets'}
PROC_PREFIX = re.compile(r'^data/([^/]+)/worldgen/processor_list/(.+)\.json$')


def processor_rows():
    """[(list id, source, {nether block: processor types})] for processor lists that output a Nether block, and
    the number of lists read. kubejs/data overrides a jar's list of the same id."""
    lists = {}
    for jar, z, _ in jars():
        for n in z.namelist():
            m = PROC_PREFIX.match(n)
            if m:
                lists[f'{m[1]}:{m[2]}'] = (jar.name, z.read(n))
    for f in sorted((ROOT / 'kubejs/data').glob('*/worldgen/processor_list/**/*.json')):
        m = PROC_PREFIX.match('data/' + f.relative_to(ROOT / 'kubejs/data').as_posix())
        if m:
            lists[f'{m[1]}:{m[2]}'] = ('kubejs/data', f.read_bytes())
    rows = []
    for lid, (src, raw) in sorted(lists.items()):
        try:
            d = loads_lenient(raw.decode('utf-8', 'ignore'))
        except ValueError:
            continue
        procs = d.get('processors', d) if isinstance(d, dict) else d
        found = defaultdict(set)

        def walk(v, ptype):
            if isinstance(v, dict):
                if isinstance(v.get('Name'), str) and N.classify(v['Name']):
                    found[v['Name']].add(ptype)
                for k, x in v.items():
                    if k not in PREDICATE_KEYS and k not in PROC_INPUT_KEYS and not k.endswith('_predicate'):
                        walk(x, ptype)
            elif isinstance(v, list):
                for x in v:
                    walk(x, ptype)

        for pr in procs if isinstance(procs, list) else []:
            if isinstance(pr, dict):
                walk(pr, pr.get('processor_type', '?').removeprefix('minecraft:'))
        if found:
            rows.append((lid, src, {b: sorted(t) for b, t in found.items()}))
    return rows, len(lists)


# ── features ─────────────────────────────────────────────────────────────────

def blocks_in(fid, kind, placed, configured, seen, path):
    """{block: set of feature ids it was found in} reachable from a placed ('p') or configured ('c') id."""
    out = defaultdict(set)
    key = (kind, fid)
    if key in seen:
        return out
    seen.add(key)
    src = placed if kind == 'p' else configured
    if fid not in src:
        return out
    data = src[fid][1]
    if kind == 'p':
        f = data.get('feature')
        sub = blocks_in(f, 'c', placed, configured, seen, path + [fid]) if isinstance(f, str) else \
            walk_value(f, placed, configured, seen, path + [fid], fid)
    else:
        sub = walk_value(data, placed, configured, seen, path + [fid], fid)
    for b, ids in sub.items():
        out[b] |= ids
    return out


def walk_value(v, placed, configured, seen, path, owner):
    out = defaultdict(set)

    def merge(d):
        for b, ids in d.items():
            out[b] |= ids

    if isinstance(v, str):
        if ':' in v and v != owner:
            if v in placed:
                merge(blocks_in(v, 'p', placed, configured, seen, path))
            elif v in configured:
                merge(blocks_in(v, 'c', placed, configured, seen, path))
        return out
    if isinstance(v, list):
        for x in v:
            merge(walk_value(x, placed, configured, seen, path, owner))
        return out
    if isinstance(v, dict):
        if 'predicate_type' in v:
            return out
        for b in TYPE_BLOCKS.get(v.get('type'), []):
            out[b].add(owner)
        loc = (v.get('config') or {}).get('location') if isinstance(v.get('config'), dict) else None
        if isinstance(loc, str):                                        # template-placing feature (YUNG's Extras)
            for b in template_nether(loc):
                out[b].add(f'{owner} (template {loc})')
        if 'placement' in v and isinstance(v.get('feature'), str):     # inline placed feature
            merge(blocks_in(v['feature'], 'c', placed, configured, seen, path))
        for k, x in v.items():
            if k in PREDICATE_KEYS:
                continue
            if k == 'Name' and isinstance(x, str):
                if N.classify(x):
                    out[x].add(owner)
            elif not (k == 'feature' and 'placement' in v and isinstance(x, str)):
                merge(walk_value(x, placed, configured, seen, path, owner))
    return out


def placed_off(placed, fid):
    """True when the placed feature's own placement has a constant count of 0 (switched off in the json)."""
    mods = placed.get(fid, (None, {}))[1].get('placement', [])
    return any(m.get('type') == 'minecraft:count' and m.get('count') == 0 for m in mods)


def biome_features(world, overworld):
    """{feature: {biome: how}} for every placed feature an Overworld biome gets (Forge order: json, adds, removes)."""
    tags, biome_files, placed, configured, modifiers = world
    cache, fb, removes = {}, defaultdict(dict), []
    for b in overworld:
        if b in biome_files:
            label, data = biome_files[b]
            for step in data.get('features', []):
                for fid in step if isinstance(step, list) else [step]:
                    if isinstance(fid, str):
                        fb[fid][b] = 'biome json'
    for mid, (label, data) in sorted(modifiers.items()):
        t = data.get('type')
        feats = data.get('features')
        feats = [feats] if isinstance(feats, str) else [f for f in (feats or []) if isinstance(f, str)]
        if t == 'forge:remove_features':
            removes.append((data.get('biomes'), feats))
        elif t == 'forge:add_features':
            for b in C.holder_set(data.get('biomes'), tags, cache, overworld) & set(overworld):
                for f in feats:
                    fb[f][b] = f'modifier {mid}'
    for spec, feats in removes:
        for b in C.holder_set(spec, tags, cache, overworld):
            for f in feats:
                fb[f].pop(b, None)
    return {f: bs for f, bs in fb.items() if bs}


def feature_rows(world, overworld):
    tags, biome_files, placed, configured, modifiers = world
    fb = biome_features(world, overworld)
    rows, unresolved = [], []
    for fid, bs in sorted(fb.items()):
        if fid in JAVA_FEATURES:
            blocks, rate, height, notes = JAVA_FEATURES[fid]
            rows.append({'feature': fid, 'source': 'Java', 'blocks': {b: [fid] for b in blocks}, 'rate': rate,
                         'height': height, 'notes': notes, 'off': False, 'biomes': bs})
            continue
        if fid not in placed:
            if fid not in JAVA_CLEAN and not fid.startswith(JAVA_CLEAN_PREFIXES):
                unresolved.append((fid, sorted(bs)))
            continue
        found = blocks_in(fid, 'p', placed, configured, set(), [])
        if not found:
            continue
        rate, height, notes = C.placement_info(placed[fid][1].get('placement', []))
        rows.append({'feature': fid, 'source': placed[fid][0], 'blocks': {b: sorted(i) for b, i in found.items()},
                     'rate': '0' if rate == 0 else C.fmt_rate(rate), 'height': height, 'notes': ', '.join(notes),
                     'off': placed_off(placed, fid), 'biomes': bs})
    cache = {}
    for cid, (source, blocks, spec, rate, notes) in CODE_ADDED.items():
        ns, _, path = cid.partition(':')
        if (ROOT / f'kubejs/data/{ns}/worldgen/configured_feature/{path}.json').exists():
            continue    # the pack overrides it (gen_nether_worldgen.py literal: D&D's layer is granite)
        bs = {b: 'code' for b in C.holder_set(spec, tags, cache, overworld) & set(overworld)}
        rows.append({'feature': cid, 'source': source, 'blocks': {b: [cid] for b in blocks}, 'rate': rate,
                     'height': '', 'notes': notes, 'off': False, 'biomes': bs})
    return rows, unresolved


def check_unresolved(unresolved):
    """{mod id: (features, {class: [blocks]} or None when the mapping is missing, hand note)}"""
    fields = nether_fields()
    by_ns = defaultdict(list)
    for fid, _ in unresolved:
        by_ns[fid.split(':')[0]].append(fid)
    return {ns: (fids, jar_nether_refs(ns, fields) if fields else None, JAVA_CHECKED.get(ns, ''))
            for ns, fids in sorted(by_ns.items())}


# ── output ───────────────────────────────────────────────────────────────────

def short(b):
    return b.split(':', 1)[1]


def biome_list(bs, area, limit=8):
    """Biomes by area, largest first, with their share of the Overworld."""
    bs = sorted(bs, key=lambda b: (-area.get(b, 0), b))
    txt = ', '.join(f'{short(b)} {area.get(b, 0):g}%' for b in bs[:limit])
    return txt + (f' (+{len(bs) - limit} more)' if len(bs) > limit else '')


def write(surface, feats, unresolved, structs, area, overworld, procs=None):
    L = ['# Nether blocks in Overworld generation (scan)', '',
         'Written by `python3 tools/nether_worldgen_scan.py`. Do not edit. The decision table built from it is '
         '[nether_worldgen.md](nether_worldgen.md). Biome shares are `result_pct` from `biome_config.json` (share of the '
         'Overworld after biome tuning; 0 = never hit in the climate samples).', '']
    L += ['## 1. Surface rules (Terralith `minecraft:overworld` noise settings)', '',
          '| Block | Tier | Biomes | Other conditions on the path |', '|---|---:|---|---|']
    for name, bs, other in surface:
        btxt = 'every biome that reaches it' if len(bs) == len(overworld) else biome_list(bs, area)
        L.append(f"| `{short(name)}` | {N.classify(name)[0]} | {btxt} | {'; '.join(other) or '-'} |")
    L += ['', '## 2. Biome features', '',
          'Rate is the placed feature\'s attempts per chunk; nested features keep their own placement. "OFF" = the '
          'json sets count 0.', '',
          '| Feature | Source | Nether blocks | Rate | Height | Biomes | Notes |', '|---|---|---|---|---|---|---|']
    for r in sorted(feats, key=lambda r: (r['off'], min(N.classify(b)[0] for b in r['blocks']), r['feature'])):
        bl = ', '.join(f"{short(b)} (T{N.classify(b)[0]})" for b in sorted(r['blocks']))
        nested = sorted({i for ids in r['blocks'].values() for i in ids} - {r['feature']})
        notes = '; '.join(x for x in (r['notes'], 'via ' + ', '.join(f'`{n}`' for n in nested) if nested else '') if x)
        src = r['source'].split('_')[0].split('-')[0]
        L.append(f"| `{r['feature']}`{' OFF' if r['off'] else ''} | {src} | {bl} | {r['rate']} | {r['height']} | "
                 f"{biome_list(r['biomes'], area)} | {notes} |")
    L += ['', '### Json-less mod features (checked by jar)', '',
          'Features an Overworld biome gets that have no json. Each mod\'s worldgen classes are searched for a Nether '
          '`Blocks` field (SRG name); "clean" = none referenced.', '',
          '| Mod | Features | Nether references in worldgen classes | Verdict |', '|---|---:|---|---|']
    for ns, (fids, hits, note) in unresolved.items():
        if hits is None:
            refs, verdict = 'mapping not found', 'not checked'
        else:
            refs = '; '.join(f"{c}: {', '.join(b)}" for c, b in sorted(hits.items())) or '-'
            verdict = note or ('clean' if not hits else 'CHECK BY HAND')
        L.append(f'| `{ns}` | {len(fids)} | {refs} | {verdict} |')
    if structs is not None:
        live = [r for r in structs if not r['off'] and not r['blocked']]
        L += ['', '## 3. Structures after the swaps', '',
              'What `nether_block_swaps.json` leaves in each live Overworld structure, plus the Nether blocks the swaps '
              'put in ("swapped in"). Counts sum every template the structure can place. "Shared" = templates a '
              'Nether or End structure also reaches: never swapped, and usually that structure\'s own pieces the pool '
              'walk over-reaches into (it ignores jigsaw names), so not counted in the tiers.', '',
              '| Structure | Group | T1 | T2 | T3 | Blocks left | Swapped in | Shared |',
              '|---|---|---:|---:|---:|---|---|---|']
        for r in live:
            bl = ', '.join(f"{short(b)} {n}" for b, n in r['blocks']) or '-'
            sw = ', '.join(f"{short(b)} {n}" for b, n in r['swapped_in']) or '-'
            sh = ', '.join(f"{short(b)} {n}" for b, n in r['shared']) or '-'
            L.append(f"| `{r['id']}` | {r['group']} | {r['tiers'].get(1, '')} | {r['tiers'].get(2, '')} | "
                     f"{r['tiers'].get(3, '')} | {bl} | {sw} | {sh} |")
        off = [r['id'] for r in structs if r['off'] or r['blocked']]
        L += ['', f"Not live (off or blocked): {', '.join(f'`{s}`' for s in off) or 'none'}."]
    if procs is not None:
        rows, n = procs
        L += ['', '## 4. Structure processors', '',
              f'{n} processor lists read (jars, kubejs/data wins). Nether blocks a processor puts out (input and '
              'location predicates skipped): it can place them where the template has none. The lists are not '
              'tied to a dimension here; check which structures use a hit.', '',
              '| Processor list | Source | Nether blocks out (processor type) |', '|---|---|---|']
        for lid, src, found in rows:
            L.append(f"| `{lid}` | {src.split('-')[0].split('_')[0]} | "
                     f"{', '.join(f'{short(b)} ({chr(47).join(t)})' for b, t in sorted(found.items()))} |")
        if not rows:
            L.append('| none | | |')
        L += ['', 'Mod processor classes (decompiled, no json): ' +
              '; '.join(f'`{k}` {v}' for k, v in PROCESSOR_CLASSES.items()) + '.']
    OUT_MD.write_text('\n'.join(L) + '\n')


def main():
    out_json = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None
    bc = json.loads(C.BIOME_CONFIG.read_text())['dimensions']['minecraft:overworld']['biomes']
    overworld = sorted(bc)
    area = {b: v.get('result_pct', 0) for b, v in bc.items()}
    world = C.load_all()
    surface = surface_hits(load_surface_rule(), overworld)
    feats, unresolved = feature_rows(world, overworld)
    unresolved = check_unresolved(unresolved)
    structs = None if '--no-structures' in sys.argv else N.scan(N.load_swaps())
    procs = processor_rows()
    if out_json:
        Path(out_json).write_text(json.dumps({'surface': surface, 'features': feats, 'unresolved': unresolved,
                                              'structures': structs}, indent=1, default=sorted))
    write(surface, feats, unresolved, structs, area, overworld, procs)
    print(f'{len(surface)} surface rule hits, {len(feats)} features ({len(unresolved)} json-less unchecked)'
          f'{"" if structs is None else f", {len(structs)} structures"}, {len(procs[0])} of {procs[1]} processor '
          f'lists put out Nether blocks -> {OUT_MD.relative_to(ROOT)}')


if __name__ == '__main__':
    main()

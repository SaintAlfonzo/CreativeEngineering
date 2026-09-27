#!/usr/bin/env python3
"""
Proposes biome-pool merges: which modded biomes (Terralith / Nullscape / Hearths / Quark ...)
fit a structure's theme but are missing from its spawn pool.

For every tracked structure (kubejs/generation_scripts/worldgen/structure_config.json):
  1. Resolve its TRUE current biome pool: the structure's own biomes field if a mod ships one,
     else vanilla's convention #minecraft:has_structure/<name>; tags are resolved recursively and
     unioned across every installed jar (so Terralith's additions to vanilla tags are counted).
  2. Infer a single theme category (snowy, desert, jungle, ...) if >=70% of the pool belongs to it.
  3. Any modded biome in that category (per the c:/forge:/minecraft: convention tags) that is not
     already in the pool becomes a suggestion.

Suggestions are grouped into numbered proposals (theme x target mod) in
kubejs/generation_scripts/worldgen/biome_merge_proposals.{md,json}. NOTHING is applied -- each
proposal lists the exact tag(s) an additive tag file would extend.

Run: python3 tools/analyze_structure_biome_merges.py
"""
import glob
import json
import re
import zipfile
from collections import defaultdict
from pathlib import Path

from gen_structure_config import VANILLA_TAG_OVERRIDES   # structure id -> has_structure tag name, where they differ

INSTANCE = Path(__file__).resolve().parent.parent
MODS_DIR = INSTANCE / 'mods'
WORLDGEN_DIR = INSTANCE / 'kubejs/generation_scripts/worldgen'
CONFIG_PATH = WORLDGEN_DIR / 'structure_config.json'
OUT_JSON = WORLDGEN_DIR / 'biome_merge_proposals.json'
OUT_MD = WORLDGEN_DIR / 'biome_merge_proposals.md'

_vj = glob.glob(str(Path.home() / '.local/share/atlauncher/libraries/net/minecraft/client/1.19.2*/*-extra.jar'))
VANILLA_JAR = Path(_vj[0]) if _vj else None

# Vanilla defines no snowy/desert/swamp/plains/mushroom tags of its own (only the is_* ones read
# from the jar), so these are hardcoded.
VANILLA_EXTRA_CATEGORIES = {
    'snowy': ['minecraft:snowy_plains', 'minecraft:ice_spikes', 'minecraft:snowy_taiga', 'minecraft:snowy_slopes',
              'minecraft:frozen_peaks', 'minecraft:jagged_peaks', 'minecraft:frozen_river', 'minecraft:snowy_beach',
              'minecraft:frozen_ocean', 'minecraft:deep_frozen_ocean', 'minecraft:grove'],
    'desert': ['minecraft:desert'],
    'swamp': ['minecraft:swamp', 'minecraft:mangrove_swamp'],
    'plains': ['minecraft:plains', 'minecraft:sunflower_plains'],
    'mushroom': ['minecraft:mushroom_fields'],
    'end': ['minecraft:the_end', 'minecraft:end_highlands', 'minecraft:end_midlands',
            'minecraft:end_barrens', 'minecraft:small_end_islands'],
}

# Deliberately narrow: no c:tree_* (vegetation, not theme), c:wasteland, c:aquatic (generic water,
# drags rivers into "ocean"), and no mountain category (matches meadow/hill villages far too broadly).
CATEGORY_TAG_NAMES = {
    'snowy':    ['c:snowy', 'c:snowy_plains', 'forge:is_snowy'],
    'desert':   ['c:desert', 'forge:is_desert'],
    'savanna':  ['minecraft:is_savanna', 'c:savanna'],
    'jungle':   ['minecraft:is_jungle', 'c:jungle'],
    'taiga':    ['minecraft:is_taiga', 'c:taiga'],
    'badlands': ['minecraft:is_badlands', 'c:badlands', 'c:mesa'],
    'swamp':    ['c:swamp', 'forge:is_swamp'],
    'plains':   ['c:plains', 'forge:is_plains'],
    'forest':   ['minecraft:is_forest', 'c:forest', 'c:flower_forests'],
    'ocean':    ['minecraft:is_ocean', 'minecraft:is_deep_ocean', 'c:ocean', 'c:deep_ocean', 'c:shallow_ocean'],
    'beach':    ['minecraft:is_beach', 'c:beach'],
    'mushroom': ['c:mushroom', 'forge:is_mushroom'],
    'end':      ['minecraft:is_end'],
}

# Deliberately broad / dimension-wide / empty pools -- nothing thematic to extend.
SKIP_STRUCTURES = {
    'minecraft:mineshaft', 'minecraft:mineshaft_mesa', 'minecraft:ruined_portal_standard',
    'minecraft:stronghold', 'minecraft:ocean_monument', 'minecraft:shipwreck', 'minecraft:shipwreck_beached',
    'minecraft:buried_treasure', 'minecraft:ruined_portal_ocean', 'minecraft:ruined_portal_nether',
    'minecraft:fortress', 'minecraft:bastion_remnant',
}


def iter_jars():
    if VANILLA_JAR:
        yield VANILLA_JAR
    yield from sorted(MODS_DIR.glob('*.jar'))


def load_all_tags():
    tags = defaultdict(list)
    for jar in iter_jars():
        try:
            z = zipfile.ZipFile(jar)
        except (zipfile.BadZipFile, FileNotFoundError):
            continue
        for name in z.namelist():
            m = re.match(r'^data/([^/]+)/tags/worldgen/biome/(.+)\.json$', name)
            if not m:
                continue
            try:
                data = json.loads(z.read(name))
            except (json.JSONDecodeError, UnicodeDecodeError):
                continue
            tags[f'{m.group(1)}:{m.group(2)}'].extend(data.get('values', []))
    return tags


def resolve_tag(tag_id, raw_tags, cache, stack=frozenset()):
    if tag_id in cache:
        return cache[tag_id]
    if tag_id in stack:
        return frozenset()
    stack = stack | {tag_id}
    out = set()
    for v in raw_tags.get(tag_id, []):
        if isinstance(v, str) and v.startswith('#'):
            out |= resolve_tag(v[1:], raw_tags, cache, stack)
        elif isinstance(v, str):
            out.add(v)
    cache[tag_id] = frozenset(out)
    return cache[tag_id]


def scan_structure_biome_fields():
    out, src = {}, {}
    for jar in sorted(MODS_DIR.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for name in z.namelist():
            m = re.match(r'^data/([^/]+)/worldgen/structure/(.+)\.json$', name)
            if not m:
                continue
            try:
                data = json.loads(z.read(name))
            except (json.JSONDecodeError, UnicodeDecodeError):
                continue
            sid = f'{m.group(1)}:{m.group(2)}'
            out[sid] = data.get('biomes')
            src[sid] = jar.name
    return out, src


def structure_pool(struct_id, field, raw_tags, cache):
    """-> (pool frozenset | None, target_tags list). target_tags empty = literal list, not tag-extensible."""
    if field is None:
        ns, _, path = struct_id.partition(':')
        tag_id = f'minecraft:has_structure/{VANILLA_TAG_OVERRIDES.get(struct_id, path)}'
        if ns == 'minecraft' and tag_id in raw_tags:
            return resolve_tag(tag_id, raw_tags, cache), [tag_id]
        return None, []
    if isinstance(field, str):
        if field.startswith('#'):
            return resolve_tag(field[1:], raw_tags, cache), [field[1:]]
        return frozenset([field]), []
    if isinstance(field, list):
        pool, tags = set(), []
        for v in field:
            if isinstance(v, str) and v.startswith('#'):
                pool |= resolve_tag(v[1:], raw_tags, cache)
                tags.append(v[1:])
            elif isinstance(v, str):
                pool.add(v)
        return frozenset(pool), tags
    return None, []


def dominant_category(pool, category_biomes, threshold=0.7):
    best, best_frac = None, 0.0
    for cat, biomes in category_biomes.items():
        frac = len(pool & biomes) / len(pool)
        if frac > best_frac:
            best, best_frac = cat, frac
    return best if best_frac >= threshold else None


def compute():
    raw_tags = load_all_tags()
    cache = {}
    field_by_struct, jar_by_struct = scan_structure_biome_fields()

    category_biomes = defaultdict(set)
    for cat, tagnames in CATEGORY_TAG_NAMES.items():
        for t in tagnames:
            category_biomes[cat] |= resolve_tag(t, raw_tags, cache)
        category_biomes[cat] |= set(VANILLA_EXTRA_CATEGORIES.get(cat, []))

    cfg = json.loads(CONFIG_PATH.read_text())

    per_structure = []
    seen = set()
    for set_id, entry in cfg['structure_sets'].items():
        for s in entry['structures']:
            sid = s['structure']
            if sid in seen or sid in SKIP_STRUCTURES:
                continue
            seen.add(sid)
            pool, target_tags = structure_pool(sid, field_by_struct.get(sid), raw_tags, cache)
            if not pool:
                continue
            cat = dominant_category(pool, category_biomes)
            if not cat:
                continue
            own_ns = sid.split(':')[0]
            missing = sorted(b for b in category_biomes[cat]
                             if not b.startswith(('minecraft:', own_ns + ':')) and b not in pool)
            if not missing:
                continue
            per_structure.append({
                'structure': sid, 'structure_set': set_id,
                'structure_mod': own_ns, 'category': cat,
                'source': jar_by_struct.get(sid, 'vanilla (assumed has_structure tag)'),
                'target_tags': target_tags, 'current_pool': sorted(pool), 'missing': missing,
            })

    # group into proposals: (category, structure mod)
    groups = defaultdict(list)
    for p in per_structure:
        groups[(p['category'], p['structure_mod'])].append(p)

    proposals = []
    for (cat, mod), items in sorted(groups.items()):
        biomes = sorted({b for i in items for b in i['missing']})
        biome_mods = sorted({b.split(':')[0] for b in biomes})
        proposals.append({
            'category': cat, 'target_mod': mod, 'biome_mods': biome_mods,
            'biomes': biomes,
            'tags_to_extend': sorted({t for i in items for t in i['target_tags']}),
            'literal_only_structures': sorted(i['structure'] for i in items if not i['target_tags']),
            'structures': sorted(
                ({'structure': i['structure'], 'missing': i['missing']} for i in items),
                key=lambda x: x['structure']),
        })
    for n, p in enumerate(proposals, 1):
        p['id'] = n
    for p in per_structure:
        p['proposal'] = next(q['id'] for q in proposals
                             if q['category'] == p['category'] and q['target_mod'] == p['structure_mod'])

    return {'raw_tags': raw_tags, 'cache': cache, 'category_biomes': category_biomes,
            'field_by_struct': field_by_struct, 'jar_by_struct': jar_by_struct,
            'per_structure': per_structure, 'proposals': proposals, 'cfg': cfg}


def main():
    ctx = compute()
    proposals, per_structure = ctx['proposals'], ctx['per_structure']
    OUT_JSON.write_text(json.dumps(proposals, indent=2))

    lines = [
        '# Structure biome merge proposals', '',
        'Each proposal adds modded biomes of one theme to the spawn pool of one mod\'s structures. '
        'Pools are fully tag-resolved (Terralith\'s existing additions to vanilla tags are already '
        'counted as "current"). Approve by number; nothing is applied yet. Execution = one additive '
        'tag file per tag listed (`"replace": false`) under `kubejs/data/<ns>/tags/worldgen/biome/`.',
        '', f'{len(proposals)} proposals, {len(per_structure)} structures.', '']
    for p in proposals:
        lines.append(f"## {p['id']}. {p['category']} biomes ({', '.join(p['biome_mods'])}) -> {p['target_mod']} structures")
        lines.append('')
        lines.append(f"**Add:** {', '.join(p['biomes'])}")
        lines.append('')
        lines.append(f"**Tags extended ({len(p['tags_to_extend'])}):** {', '.join(p['tags_to_extend']) or '(none)'}")
        lines.append('')
        lines.append('**Structures:**')
        for st in p['structures']:
            lines.append(f"- {st['structure']} (+{len(st['missing'])})")
        lines.append('')
    OUT_MD.write_text('\n'.join(lines))

    print(f'{len(proposals)} proposals covering {len(per_structure)} structures')
    for p in proposals:
        print(f"  {p['id']:>2}. {p['category']:9} -> {p['target_mod']:22} {len(p['structures']):>2} structures, {len(p['biomes'])} biomes, {len(p['tags_to_extend'])} tags")


if __name__ == '__main__':
    main()

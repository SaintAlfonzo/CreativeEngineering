#!/usr/bin/env python3
"""
Structure groups: builds, syncs and checks kubejs/generation_scripts/worldgen/structure_groups.json.

THE MODEL
  A group is ONE structure_set, creativeengineering:group/<name>, holding every structure in the
  structure tag group/<name> (structure_tags.json). All members share its grid, so no two of them
  ever start closer than the group's separation, whatever mod they come from. Per member:
    weight     who wins a grid slot where several members could spawn (vanilla set weight)
    frequency  chance that the winner really spawns there; otherwise the slot stays EMPTY (it does
               not fall through to another member), so turning one member down never makes the
               others more common. Needs patcherMod (worldgen/StructureFrequencies).
  Where a member is the only one that fits the biome, it takes every slot there x its frequency.
  Every structure not in a group keeps its own set ("standalone") and is tuned in the same file,
  with the same per-structure frequency.
  kubejs/server_scripts/worldgen/structure_overrides.js applies the file at every world load.

MODES
  python3 tools/gen_structure_groups.py             sync: adds groups, members and standalone sets that
                                                    are missing from structure_groups.json (a new mod, a
                                                    new group/* tag), fitted to their mod defaults. Never
                                                    changes a value that is already there. Then writes the
                                                    estimates report.
  python3 tools/gen_structure_groups.py --check     only rewrites structure_group_estimates.md
  ... --refit GROUP [GROUP ...]                     new grid for those groups, every member refitted to the
                                                    rate the file gives it now. Spacing and separation are
                                                    re-derived: set a hand-picked separation again afterwards.
  ... --strip-weights [GROUP ...]                   every group member without "tuned" (and no meta or
                                                    replacement) back to weight MEDIAN_WEIGHT, fit_base
                                                    dropped, fit_keep false (--fit-rates leaves it alone);
                                                    frequencies kept. Standalone members lose their weight
                                                    override (back to the mod's own). Prints old -> new and
                                                    the rate ratio; estimates compared to the file before.
  ... --migration-report                            also rewrites structure_group_migration.md: the current
                                                    file against the pre-groups tuning (from the archive)
  python3 tools/gen_structure_groups.py --migrate   one-time conversion of the pre-2026-09-27 tuning
                                                    ("current" per set + tag multipliers), see migrate()

THE FIT (new members, and --migrate)
  For every member and every biome it can spawn in, the target is the density (structures per chunk^2)
  it had before. In biome b, the group gives member i
      K * frequency_i * weight_i / (sum of weights of members valid in b),   K = 1 / spacing^2
  so members that share biomes share slots. Weights and frequencies are solved (least squares on log
  density, each biome weighted by its real area from biome_config.json) to keep every member as close
  to its target as possible. The group's spacing comes from the crowding: K covers the combined old
  density of its most crowded biome (QUANTILE 1.0), so no biome loses structures and the gain is the
  guaranteed gap. Lower QUANTILE (e.g. 0.9 = the 90% least crowded area) to cap the most crowded
  biomes at the group rate instead; the estimates report lists what gets capped.

Biome pools come from every jar's biome tags + kubejs/data, then biome_fits.json and the biome rules.
"""
import argparse
import json
import math
import shutil
import sys
import zipfile
import zlib
from collections import OrderedDict, defaultdict
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_structure_config import MODS_DIR, VANILLA_JAR, VANILLA_TAG_OVERRIDES, loads_lenient  # noqa: E402

INSTANCE = Path(__file__).resolve().parent.parent
WG = INSTANCE / 'kubejs/generation_scripts/worldgen'
CONFIG = WG / 'structure_config.json'
TAGS = WG / 'structure_tags.json'
GROUPS = WG / 'structure_groups.json'
FITS = WG / 'biome_fits.json'
BIOME_CONFIG = WG / 'biome_config.json'
ESTIMATES = WG / 'structure_group_estimates.md'
MIGRATION_REPORT = WG / 'structure_group_migration.md'
KUBEJS_DATA = INSTANCE / 'kubejs/data'
ARCHIVE = INSTANCE / 'kubejs/archive/worldgen/pre_groups_2026-09-27'

GROUP_SET_PREFIX = 'creativeengineering:group/'
QUANTILE = 1.0
MEDIAN_WEIGHT = 100
EPS_AREA = 1e-4          # a biome that exists but never showed up in the climate sample

# The groups the pack starts with (2026-09-27). --migrate writes these as group/<name> tags; after
# that the tags in structure_tags.json are the source of truth and this table is only a record.
GROUP_SPECS = OrderedDict([
    ('settlements', {
        'note': 'Friendly settlements: vanilla and Towns & Towers villages, Terralith fortified villages. Meta structures join later.',
        'values': ['#type/village', '!towns_and_towers:village_ocean',
                   '!towns_and_towers:exclusives/village_wandering_trader_camp'],
    }),
    ('pillager_settlements', {
        'note': 'Pillager outposts: vanilla, and every Towns & Towers tower, fort and camp outpost except the ship (boats).',
        'values': ['minecraft:pillager_outpost', 'towns_and_towers:pillager_outpost_*',
                   'towns_and_towers:exclusives/pillager_outpost_*', '!towns_and_towers:pillager_outpost_ocean'],
        'exclusion': ('settlements', 4),
    }),
    ('ruins', {
        'note': "Surface land ruins: Philip's Ruins and Terralith's glacial hut. Underground ruins stay standalone.",
        'values': ['#type/ruin', '!#type/underground'],
    }),
    ('surface_dungeons', {
        'note': "Surface dungeons: YUNG's temples and witch huts, the woodland mansion, Terralith's spire, the T&T desert mimic, Philip's badlands dungeon. The vanilla temples YUNG's blocks in code stay out.",
        'values': ['#dungeon/surface', '!#type/sky', '!minecraft:desert_pyramid', '!minecraft:jungle_pyramid',
                   '!minecraft:swamp_hut'],
    }),
    ('ocean_ruins', {
        'note': "Ocean ruins and wrecks: vanilla, Hopo's and Philip's ocean ruins, Philip's ocean fortress, vanilla shipwrecks, the T&T wreckage.",
        'values': ['#type/underwater_ruin', '#type/shipwreck'],
    }),
    ('ocean_dungeons', {
        'note': 'Ocean monuments, on the monuments\' own grid (same spacing, spread and salt, so positions match vanilla). Monument meta structures join later.',
        'values': ['minecraft:monument'],
        'grid_from': 'minecraft:ocean_monuments',
    }),
    ('sky', {
        'note': "Sky structures: Cloud Storage's hot air balloons and sky temples. Meta structures join later.",
        'values': ['#type/sky'],
    }),
    ('boats', {
        'note': 'Ships at sea: the Towns & Towers ship village and pillager ship. Meta structures join later.',
        'values': ['towns_and_towers:village_ocean', 'towns_and_towers:pillager_outpost_ocean'],
    }),
    ('flotsam', {
        'note': 'Floating debris at sea: the driftwood flotilla (meta structure). Its own grid, so it takes no slots from ruins or ships.',
        'values': ['creativeengineering:meta/driftwood_flotilla'],
    }),
    ('remote', {
        'note': 'Lone camps and cabins: the T&T wandering trader camp and every single-building house mod in the Overworld. The two Farmer\'s Structures houses whose biomes come from mods this pack does not have (Aether, Undergarden) stay out.',
        'values': ['towns_and_towers:exclusives/village_wandering_trader_camp', '#type/house', '!#nether',
                   '!farmers_structures:aethers', '!farmers_structures:undergardens_sea'],
    }),
])


# ── small helpers ────────────────────────────────────────────────────────────

def read_json(path):
    return json.loads(path.read_text(), object_pairs_hook=OrderedDict)


def write_json(path, obj):
    path.write_text(json.dumps(obj, indent=2) + '\n')


def js_round(x):
    """JS Math.round (half up), which structure_overrides.js uses."""
    return math.floor(x + 0.5)


def round6(x):
    return js_round(x * 1e6) / 1e6


def spacing_equiv(d):
    """Density (per chunk^2) -> 'one per N x N chunks'."""
    return math.sqrt(1 / d) if d > 0 else float('inf')


def fmt_sp(d):
    s = spacing_equiv(d)
    return '-' if s == float('inf') else f'{s:.0f}'


def group_salt(name):
    return zlib.crc32(('creativeengineering:group/' + name).encode()) & 0x7fffffff


# ── structure tags (same syntax + semantics as structure_overrides.js) ───────

def resolve_tags(tag_defs, all_ids):
    import fnmatch
    id_set = set(all_ids)
    members, resolving, errors = {}, set(), []

    def glob_match(pattern):
        return [i for i in all_ids if fnmatch.fnmatchcase(i, pattern)]

    def resolve(name, frm):
        if name in members:
            return members[name]
        if name not in tag_defs:
            errors.append(f'{frm}: unknown tag "#{name}"')
            return set()
        if name in resolving:
            errors.append(f'{frm}: tag cycle through "#{name}"')
            return set()
        resolving.add(name)
        inc, exc = set(), set()
        for raw in tag_defs[name].get('values', []):
            neg = raw.startswith('!')
            v = raw[1:] if neg else raw
            if v.startswith('#'):
                ids = resolve(v[1:], name)
            elif '*' in v:
                ids = set(glob_match(v))
            elif v in id_set:
                ids = {v}
            else:
                errors.append(f'{name}: unknown structure "{v}"')
                ids = set()
            (exc if neg else inc).update(ids)
        resolving.discard(name)
        members[name] = inc - exc
        return members[name]

    for name in tag_defs:
        resolve(name, name)
    return members, errors


# ── biome pools and areas ────────────────────────────────────────────────────

def load_biome_tags():
    """Every biome tag from the vanilla jar, every mod jar (lenient: Beautify's have comments) and kubejs/data."""
    import re
    raw = defaultdict(list)
    jars = ([VANILLA_JAR] if VANILLA_JAR else []) + sorted(MODS_DIR.glob('*.jar'))
    for jar in jars:
        try:
            z = zipfile.ZipFile(jar)
        except (zipfile.BadZipFile, FileNotFoundError):
            continue
        for name in z.namelist():
            m = re.match(r'^data/([^/]+)/tags/worldgen/biome/(.+)\.json$', name)
            if not m:
                continue
            try:
                data = loads_lenient(z.read(name).decode('utf-8', 'replace'))
            except (ValueError, UnicodeDecodeError):
                continue
            if isinstance(data, dict):
                raw[f'{m.group(1)}:{m.group(2)}'].extend(data.get('values', []))
    for f in KUBEJS_DATA.glob('*/tags/worldgen/biome/**/*.json'):
        ns = f.relative_to(KUBEJS_DATA).parts[0]
        path = f.relative_to(KUBEJS_DATA / ns / 'tags/worldgen/biome').with_suffix('').as_posix()
        try:
            raw[f'{ns}:{path}'].extend(json.loads(f.read_text()).get('values', []))
        except json.JSONDecodeError:
            pass
    return raw


def resolve_biome_tag(tag_id, raw, cache, stack=frozenset()):
    if tag_id in cache:
        return cache[tag_id]
    if tag_id in stack:
        return frozenset()
    out = set()
    for v in raw.get(tag_id, []):
        if isinstance(v, dict):
            v = v.get('id')
        if not isinstance(v, str):
            continue
        if v.startswith('#'):
            out |= resolve_biome_tag(full_id(v[1:]), raw, cache, stack | {tag_id})
        else:
            out.add(full_id(v))
    cache[tag_id] = frozenset(out)
    return cache[tag_id]


def full_id(v):
    """Tag files may leave out the namespace ("#is_ocean", "lush_caves"): it's minecraft."""
    return v if ':' in v else 'minecraft:' + v


# Biome tags a mod fills at run time (its jar ships them empty): read as the tag they get filled with.
# Supplementaries' dynamic pack puts #minecraft:is_overworld into has_way_signs.
RUNTIME_BIOME_TAGS = {'supplementaries:has_way_signs': 'minecraft:is_overworld'}


def structure_pool(sid, field, raw, cache):
    """Every biome the structure may spawn in (not yet filtered to biomes that exist)."""
    ns, _, path = sid.partition(':')
    if ns == 'minecraft' and not isinstance(field, str):
        # the config's lists for vanilla are read from the vanilla jar alone; the tag includes
        # Terralith's additions. A string field means a mod replaced the
        # structure json (Hopo's shipwrecks), so that field is the real one.
        tag = f'minecraft:has_structure/{VANILLA_TAG_OVERRIDES.get(sid, path)}'
        if tag in raw:
            return resolve_biome_tag(tag, raw, cache)
    if field is None:
        return frozenset()
    fields = field if isinstance(field, list) else [field]
    out = set()
    for v in fields:
        if isinstance(v, str) and v.startswith('#'):
            tag = full_id(v[1:])
            out |= resolve_biome_tag(RUNTIME_BIOME_TAGS.get(tag, tag), raw, cache)
        elif isinstance(v, str):
            out.add(full_id(v))
    return frozenset(out)


def load_areas():
    """biome -> share of its dimension layer (0..1), from the live climate sample."""
    areas = {}
    for dim in read_json(BIOME_CONFIG)['dimensions'].values():
        for b, info in dim['biomes'].items():
            pct = info.get('result_pct', info.get('share_pct', 0)) or 0
            areas[b] = max(pct / 100.0, EPS_AREA)
    return areas


# ── densities ────────────────────────────────────────────────────────────────

def grid_density(p):
    """Attempts per chunk^2 of a placement (None for non-grid placements)."""
    if not isinstance(p.get('spacing'), (int, float)):
        return None
    return p.get('frequency', 1) / (p['spacing'] ** 2)


def scale_placement(base, m):
    """Port of structure_overrides.js scalePlacement (pre-2026-09-27): -> (placement, achieved)."""
    p = OrderedDict(base)
    if m == 1:
        return p, 1.0
    f0 = p.get('frequency', 1)
    if f0 == 0:
        return p, 1.0
    if isinstance(p.get('spacing'), (int, float)):
        s0, sep0 = p['spacing'], p.get('separation', 0)
        s = max(1, min(4096, math.floor(s0 / math.sqrt(m) + 1e-9)))
        sep = max(0, min(s - 1, js_round(sep0 * s / s0)))
        f = round6(min(1, f0 * m * (s / s0) ** 2))
        p['spacing'], p['separation'], p['frequency'] = s, sep, f
        achieved = (s0 / s) ** 2 * f / f0
    elif isinstance(p.get('count'), (int, float)):
        c = max(1, min(4095, js_round(p['count'] * m)))
        achieved = c / p['count']
        p['count'] = c
    else:
        p['frequency'] = round6(min(1, f0 * m))
        achieved = p['frequency'] / f0
    if p.get('frequency') != f0 and p.get('frequency_reduction_method') == 'legacy_type_1':
        p['frequency_reduction_method'] = 'default'
    return p, achieved


def exclusion_loss(p, set_density):
    """Share of this placement's grid slots an exclusion_zone removes. The zone checks every chunk in
    a (2r+1)^2 square for a grid slot of the other set (grid + frequency only, no biome check)."""
    ez = p.get('exclusion_zone')
    if not isinstance(ez, dict):
        return 0.0
    d = set_density.get(ez.get('other_set'))
    if not d:
        return 0.0
    return min(1.0, (2 * ez.get('chunk_count', 0) + 1) ** 2 * d)


def member_densities(K, members, pools):
    """members: {id: (weight, chance)} sharing one grid of K slots per chunk^2 ->
    {id: {biome: density}}. Vanilla semantics: the pick is among members valid in that biome."""
    by_biome = defaultdict(float)
    for sid, (w, _) in members.items():
        for b in pools.get(sid, ()):
            by_biome[b] += w
    return {sid: {b: K * c * w / by_biome[b] for b in pools.get(sid, ())}
            for sid, (w, c) in members.items()}


class Pack:
    """Everything the model needs, loaded once."""

    def __init__(self, config_path=None, tags_path=None, fits=True):
        self.config = read_json(config_path or CONFIG)
        self.sets = self.config['structure_sets']
        self.tags_doc = read_json(tags_path or TAGS)
        self.tag_defs = self.tags_doc['tags']
        self.all_ids = [s['structure'] for e in self.sets.values() for s in e['structures']]
        self.all_ids += [m for m in self.config.get('meta_structures', {}) if m not in self.all_ids]
        self.set_of = {}
        for sid, e in self.sets.items():
            for s in e['structures']:
                self.set_of.setdefault(s['structure'], sid)
        self.tag_members, errors = resolve_tags(self.tag_defs, self.all_ids)
        if errors:
            sys.exit('structure_tags.json errors:\n  ' + '\n  '.join(errors))
        self.areas = load_areas()
        raw = load_biome_tags()
        cache = {}
        self._raw, self._cache = raw, cache
        self.pools, self.pool_all = {}, {}
        for e in self.sets.values():
            for s in e['structures']:
                pool = structure_pool(s['structure'], s.get('biomes'), raw, cache)
                self.pool_all[s['structure']] = pool
                self.pools[s['structure']] = frozenset(b for b in pool if b in self.areas)
        self.fitted = OrderedDict()
        if fits:
            self.apply_biome_fits(raw, cache)
        self.blocked = {s['structure'] for e in self.sets.values() for s in e['structures'] if s.get('blocked')}
        self.meta = self.config.get('meta_structures', {})
        # metas with an anchor never win a slot themselves: they take a share of the slots their anchors won
        # (patcherMod MetaSlots), so they are left out of every weighted pick here and estimated from their anchors
        self.converted = {mid: float(m.get('share', 0)) for mid, m in self.meta.items() if m.get('anchor')}
        # group members with "replaces" in structure_groups.json don't win slots by weight either: they take "share" of
        # the slots the member they name wins and keeps (patcherMod MetaSlots); replaces = {replacement: replaced id}
        self.replaces = {}
        if GROUPS.exists():
            for g in read_json(GROUPS).get('groups', {}).values():
                for sid, v in g.get('structures', {}).items():
                    if isinstance(v, dict) and 'replaces' in v and v.get('enabled', True):
                        self.replaces[sid] = v['replaces']
                        self.converted[sid] = float(v.get('share', 0))
        for mid, m in self.meta.items():
            pool = structure_pool(mid, m.get('biomes'), raw, cache)
            # MetaStructure only starts where an anchor and one of every required part can stand
            for need in ([m['anchor']] if m.get('anchor') else []) + m.get('required_parts', []):
                pool = pool & frozenset().union(*(self.pool_all.get(i, frozenset()) for i in self.meta_entries(need)))
            self.pool_all[mid] = pool
            self.pools[mid] = frozenset(b for b in pool if b in self.areas)
        self.apply_biome_rules(raw, cache)

    def meta_entries(self, entries):
        """A meta structure's "#creativeengineering:meta/x" / id entries -> structure ids (meta/x = the
        structure_tags.json tag; other tags aren't known here and count as empty)."""
        out = set()
        for e in entries or []:
            if e.startswith('#creativeengineering:'):
                out |= set(self.tag_members.get(e[len('#creativeengineering:'):], ()))
            elif not e.startswith('#'):
                out.add(e)
        return out - set(self.meta)

    def apply_biome_fits(self, raw, cache):
        """biome_fits.json, as patcherMod StructureBiomeFits applies it: a structure in a fit's structure tag spawns in
        that fit's biomes plus its parent fits' (unless inherit is false), not its own list. Runs before the meta
        structures and biome rules, which see the fitted lists in game too. fitted = {structure id: [fit names]}."""
        fits = read_json(FITS).get('fits', {}) if FITS.exists() else {}

        def chain(name):
            if fits[name].get('inherit', True) is False:
                return [name]
            parts = name.split('/')
            for n in range(len(parts) - 1, 0, -1):
                parent = '/'.join(parts[:n])
                if parent in fits:
                    return [name] + chain(parent)
            return [name]

        def biomes(name):
            out = set()
            for b in fits[name].get('biomes', []):
                out |= resolve_biome_tag(full_id(b[1:]), raw, cache) if b.startswith('#') else {full_id(b)}
            return out

        for name in fits:
            for sid in sorted(self.tag_members.get(name, ())):
                names = self.fitted.setdefault(sid, [])
                names.extend(c for c in chain(name) if c not in names)
        for sid, names in self.fitted.items():
            if sid not in self.pool_all:
                continue
            pool = frozenset().union(*(biomes(n) for n in names))
            self.pool_all[sid] = pool
            self.pools[sid] = frozenset(b for b in pool if b in self.areas)

    def apply_biome_rules(self, raw, cache):
        """biome_rules in structure_groups.json, as patcherMod StructureBiomeRules applies them: a structure
        that isn't allowed loses the rule's biomes. kept_out = {rule: [structure ids that lost biomes]}."""
        self.kept_out = OrderedDict()
        rules = read_json(GROUPS).get('biome_rules', {}) if GROUPS.exists() else {}
        for name, rule in rules.items():
            biomes = set()
            for b in rule.get('biomes', []):
                biomes |= resolve_biome_tag(full_id(b[1:]), raw, cache) if b.startswith('#') else {full_id(b)}
            allow = self.tag_members.get(rule.get('allow', '').lstrip('#'), set())
            out = []
            for sid in self.pools:
                if sid in allow or not (self.pool_all[sid] & biomes):
                    continue
                self.pool_all[sid] = self.pool_all[sid] - biomes
                self.pools[sid] = self.pools[sid] - biomes
                out.append(sid)
            self.kept_out[name] = sorted(out)

    def group_names(self):
        return [t[len('group/'):] for t in self.tag_defs if t.startswith('group/')]

    def biome_entry(self, entry, stack=frozenset()):
        """A biome id or #tag -> biome ids. #creativeengineering:<fit> is the fit's own list (the real tag
        structure_overrides.js adds), #creativeengineering:<name> of biome_fits.json biome_tags likewise."""
        if not entry.startswith('#'):
            return {full_id(entry)}
        tag = full_id(entry[1:])
        doc = read_json(FITS) if FITS.exists() else {}
        name = tag[len('creativeengineering:'):] if tag.startswith('creativeengineering:') else None
        own = (doc.get('fits', {}).get(name) or doc.get('biome_tags', {}).get(name)) if name else None
        if own is not None and tag not in stack:
            return set().union(*(self.biome_entry(b, stack | {tag}) for b in own.get('biomes', [])))
        return set(resolve_biome_tag(tag, self._raw, self._cache))

    def biome_factor(self, rules):
        """A group's biome_frequency -> factor(biome): the first rule the biome matches, else default (1)."""
        resolved = [(None if k == 'default' else self.biome_entry(k), f) for k, f in rules.items()]
        fallback = next((f for b, f in resolved if b is None), 1.0)
        def factor(biome):
            for b, f in resolved:
                if b is not None and biome in b:
                    return f
            return fallback
        return factor

    def group_members(self, name):
        return sorted(self.tag_members.get('group/' + name, ()))

    def weight_in_set(self, sid):
        for s in self.sets[self.set_of[sid]]['structures']:
            if s['structure'] == sid:
                return s['weight']
        return 1

    # Densities of the old model (structure_config "current" + tag multipliers + splits) or, with
    # legacy=False, of the mods' own defaults.
    def old_densities(self, legacy=True):
        active = {t: d['multiplier'] for t, d in self.tag_defs.items()
                  if legacy and isinstance(d.get('multiplier'), (int, float)) and d['multiplier'] != 1}

        def mult(sid):
            m = 1.0
            for t, v in active.items():
                if sid in self.tag_members[t]:
                    m *= v
            return m

        outputs = {}          # set -> [(placement, [member ids])]
        for set_id, e in self.sets.items():
            base = OrderedDict(e['original'])
            if legacy:
                base.update(e.get('current', {}))
            live = [s for s in e['structures']
                    if (not legacy or s.get('enabled', True)) and mult(s['structure']) > 0]
            groups = OrderedDict()
            for s in live:
                groups.setdefault(mult(s['structure']), []).append(s)
            ordered = sorted(groups.items(), key=lambda kv: -sum(x['weight'] for x in kv[1]))
            outs = []
            for m, mem in ordered:
                p, _ = scale_placement(base, m)
                outs.append((p, [x['structure'] for x in mem]))
            if not outs:
                outs = [(OrderedDict(base, frequency=0), [])]
            outputs[set_id] = outs
        set_density = {sid: grid_density(o[0][0]) for sid, o in outputs.items()}
        dens = {}
        for set_id, outs in outputs.items():
            for p, mem in outs:
                K = grid_density(p)
                if K is None:
                    continue
                K *= 1 - exclusion_loss(p, set_density)
                live = {m: (self.weight_in_set(m), 1.0) for m in mem if m not in self.blocked}
                dens.update(member_densities(K, live, self.pools))
        return dens, outputs

    def multiplier(self, sid):
        m = 1.0
        for t, d in self.tag_defs.items():
            v = d.get('multiplier')
            if isinstance(v, (int, float)) and v != 1 and sid in self.tag_members[t]:
                m *= v
        return m


def mean_density(dmap, areas):
    """Area-weighted mean density over a member's biomes."""
    if not dmap:
        return 0.0
    a = sum(areas[b] for b in dmap)
    return sum(d * areas[b] for b, d in dmap.items()) / a


def weighted_quantile(values, weights, q):
    order = np.argsort(values)
    v, w = np.asarray(values)[order], np.asarray(weights)[order]
    c = np.cumsum(w) / w.sum()
    return float(v[min(len(v) - 1, np.searchsorted(c, q))])


# ── the fit ──────────────────────────────────────────────────────────────────

def fit_group(ids, pools, targets, areas, lnK, fixed=None, iters=6000):
    """Solve log-weights x and log-frequencies y (<= 0) so that
         lnK + y_i + x_i - log(sum_{j valid in b} e^x_j)  ~  ln target_i(b)
    area-weighted per member, every member counted equally. fixed: {id: (weight, chance)} held."""
    fixed = fixed or {}
    ids = [i for i in ids if pools.get(i)]
    if not ids:
        return {}
    biomes = sorted({b for i in ids for b in pools[i]})
    col = {b: k for k, b in enumerate(biomes)}
    n, m = len(ids), len(biomes)
    mask = np.zeros((n, m), bool)
    lnD = np.zeros((n, m))
    W = np.zeros((n, m))
    for r, i in enumerate(ids):
        for b in pools[i]:
            c = col[b]
            mask[r, c] = True
            W[r, c] = areas[b]
            t = targets.get(i, {}).get(b)
            lnD[r, c] = math.log(t) if t and t > 0 else 0.0
    has_target = np.array([bool(targets.get(i)) for i in ids])
    W /= W.sum(1, keepdims=True)
    free = np.array([i not in fixed and has_target[r] for r, i in enumerate(ids)])
    x = np.where(free, (lnD * W).sum(1), 0.0)
    y = np.zeros(n)
    for r, i in enumerate(ids):
        if i in fixed:
            x[r] = math.log(fixed[i][0])
            y[r] = math.log(max(fixed[i][1], 1e-9))
    anchor = x[~free].mean() if (~free).any() else None
    if anchor is not None:
        # new members next to fitted ones: start at the group's typical weight (a member that shares no
        # biome keeps it, since its weight never matters)
        x = np.where(free, anchor, x)

    def lse(xv):
        big = np.where(mask, xv[:, None], -np.inf)
        mx = big.max(0)
        return mx + np.log(np.exp(big - mx).sum(0))

    lr, beta1, beta2 = 0.05, 0.9, 0.999
    mom, vel = np.zeros(n), np.zeros(n)
    for it in range(1, iters + 1):
        L = lse(x)
        base = lnK + x[:, None] - L[None, :]
        ystep = np.minimum(0.0, ((lnD - base) * W).sum(1))
        y = np.where(free, ystep, y)
        r = np.where(mask, base + y[:, None] - lnD, 0.0)
        P = np.where(mask, np.exp(x[:, None] - L[None, :]), 0.0)
        Wr = W * r
        g = 2 * Wr.sum(1) - 2 * (P * Wr.sum(0)[None, :]).sum(1)
        g = np.where(free, g, 0.0)
        mom = beta1 * mom + (1 - beta1) * g
        vel = beta2 * vel + (1 - beta2) * g * g
        x = x - lr * (mom / (1 - beta1 ** it)) / (np.sqrt(vel / (1 - beta2 ** it)) + 1e-9)
        if anchor is None:
            x = np.where(free, x - x[free].mean(), x)
    # integer weights (the set codec needs positive ints), then the frequencies again for them
    if anchor is None:
        scale = MEDIAN_WEIGHT / math.exp(float(np.median(x[free]))) if free.any() else 1.0
    else:
        scale = 1.0
    w_int = np.array([fixed[i][0] if i in fixed else max(1, js_round(math.exp(x[r]) * scale))
                      for r, i in enumerate(ids)], float)
    x = np.log(w_int)
    L = lse(x)
    base = lnK + x[:, None] - L[None, :]
    y = np.where(free, np.minimum(0.0, ((lnD - base) * W).sum(1)), y)
    out = {}
    for r, i in enumerate(ids):
        if i in fixed:
            continue
        c = math.exp(y[r]) if has_target[r] else 1.0
        out[i] = (int(w_int[r]), round(min(1.0, c), 4))
    return out


# ── building group entries ───────────────────────────────────────────────────

def group_grid(pack, name, spec, members, targets, set_density):
    """Placement for a new group, from its members' old densities."""
    if spec.get('grid_from'):
        src = pack.sets[spec['grid_from']]
        base = OrderedDict(src['original'])
        base.update(src.get('current', {}))
        p = OrderedDict([('spacing', base['spacing']), ('separation', base['separation']),
                         ('salt', base['salt'])])
        if base.get('spread_type', 'linear') != 'linear':
            p['spread_type'] = base['spread_type']
        return p
    T = defaultdict(float)
    for i in members:
        for b, d in targets.get(i, {}).items():
            T[b] += d
    if not T:
        return OrderedDict([('spacing', 32), ('separation', 8), ('salt', group_salt(name))])
    bs = sorted(T)
    K = weighted_quantile([T[b] for b in bs], [pack.areas[b] for b in bs], QUANTILE)
    ex = spec.get('exclusion')
    if ex:
        loss = min(0.9, (2 * ex[1] + 1) ** 2 * set_density.get(GROUP_SET_PREFIX + ex[0], 0))
        K /= (1 - loss)
    S = max(2, min(4096, math.floor(math.sqrt(1 / K))))
    # separation keeps the members' old feel: their old separation/spacing ratio, density-weighted
    num = den = 0.0
    for i in members:
        p = pack.legacy_placement.get(i)
        if not p or not p.get('spacing'):
            continue
        d = mean_density(targets.get(i, {}), pack.areas)
        num += d * p.get('separation', 0) / p['spacing']
        den += d
    ratio = num / den if den else 0.4
    sep = max(0, min(S - 1, js_round(S * ratio)))
    return OrderedDict([('spacing', S), ('separation', sep), ('salt', group_salt(name))])


def fit_standalone(pack, base, members, targets):
    """One grid for a formerly split standalone set: capacity = its most crowded biome's old total."""
    T = defaultdict(float)
    for i in members:
        for b, d in targets.get(i, {}).items():
            T[b] += d
    f = base.get('frequency', 1)
    K = max(T.values()) if T else f / base['spacing'] ** 2
    S = max(2, min(4096, math.floor(math.sqrt(f / K))))
    p = OrderedDict(base)
    p['separation'] = max(0, min(S - 1, js_round(base.get('separation', 0) * S / base['spacing'])))
    p['spacing'] = S
    return p, fit_group(members, pack.pools, targets, pack.areas, math.log(f / S ** 2))


def fit_members(pack, placement, exclusion_loss_value, members, targets, fixed=None):
    K = placement.get('frequency', 1) / placement['spacing'] ** 2 * (1 - exclusion_loss_value)
    return fit_group(members, pack.pools, targets, pack.areas, math.log(K), fixed)


# ── migrate ──────────────────────────────────────────────────────────────────

def migrate(pack, dry=False):
    """Old model -> structure_groups.json, once. Old model = per-set "current" edits, per-structure
    "enabled", tag multipliers (a product per structure; members of one set with different products
    were split into separate sets). Groups: fitted to each member's old density (see the module doc).
    Standalone sets: the set is scaled to its largest member multiplier and every other member keeps
    its own through frequency (m / largest), which is exact because the members keep sharing the grid
    with the same weights. Afterwards "current"/"enabled" leave structure_config.json and "multiplier"
    leaves structure_tags.json; the originals are in kubejs/archive/worldgen/pre_groups_2026-09-27/."""
    if GROUPS.exists() and not dry:
        sys.exit(f'{GROUPS.name} already exists; --migrate only runs once (delete it to redo from the archive).')
    if not any('current' in e for e in pack.sets.values()):
        sys.exit('structure_config.json has no "current" fields: nothing to migrate.')

    # 1. group/* tags
    for name, spec in GROUP_SPECS.items():
        tag = 'group/' + name
        if tag not in pack.tag_defs:
            pack.tag_defs[tag] = OrderedDict([('note', spec['note']), ('values', spec['values'])])
    pack.tag_members, errors = resolve_tags(pack.tag_defs, pack.all_ids)
    if errors:
        sys.exit('group tag errors:\n  ' + '\n  '.join(errors))
    owner = {}
    for name in pack.group_names():
        for sid in pack.group_members(name):
            if sid in owner:
                sys.exit(f'{sid} is in group/{owner[sid]} and group/{name}')
            owner[sid] = name

    legacy, outputs = pack.old_densities(legacy=True)
    pack.legacy_placement = {}
    for set_id, outs in outputs.items():
        for p, mem in outs:
            for m in mem:
                pack.legacy_placement[m] = p
    enabled = {s['structure']: s.get('enabled', True) for e in pack.sets.values() for s in e['structures']}

    # 2. groups, in an order where an exclusion's target comes first
    groups = OrderedDict()
    group_density = {}
    for name, spec in GROUP_SPECS.items():
        members = pack.group_members(name)
        live = [m for m in members if enabled.get(m, True) and pack.multiplier(m) > 0 and m not in pack.blocked and m not in pack.converted]
        placement = group_grid(pack, name, spec, live, legacy, group_density)
        loss = 0.0
        if spec.get('exclusion'):
            other, r = spec['exclusion']
            placement['exclusion_zone'] = OrderedDict([('other_set', GROUP_SET_PREFIX + other), ('chunk_count', r)])
            loss = min(1.0, (2 * r + 1) ** 2 * group_density.get(GROUP_SET_PREFIX + other, 0))
        group_density[GROUP_SET_PREFIX + name] = placement.get('frequency', 1) / placement['spacing'] ** 2
        fitted = fit_members(pack, placement, loss, live, legacy)
        structures = OrderedDict()
        for m in members:
            if m in fitted:
                w, c = fitted[m]
                structures[m] = OrderedDict([('weight', w), ('frequency', c)])
            else:
                structures[m] = OrderedDict([('weight', MEDIAN_WEIGHT), ('frequency', 1.0)])
                if not (enabled[m] and pack.multiplier(m) > 0):
                    structures[m]['enabled'] = False
        groups[name] = OrderedDict([('note', spec['note']), ('placement', placement), ('structures', structures)])

    # 3. standalone sets
    fully_grouped = {sid for sid, e in pack.sets.items()
                     if e['structures'] and all(s['structure'] in owner for s in e['structures'])}
    standalone = OrderedDict()
    retargets = []
    for set_id, e in pack.sets.items():
        rest = [s for s in e['structures'] if s['structure'] not in owner]
        if not rest:
            continue
        base = OrderedDict(e['original'])
        base.update(e.get('current', {}))
        live = [s for s in rest if s.get('enabled', True) and pack.multiplier(s['structure']) > 0]
        structures = OrderedDict()
        mults = {pack.multiplier(s['structure']) for s in live}
        if len(mults) > 1 and isinstance(base.get('spacing'), (int, float)):
            # the old model split this set by multiplier into separate grids, so its members didn't
            # compete where their biomes overlap; one grid can only match that with a fit (every
            # biome at full capacity, nothing capped: consistent spacing is a group goal, not this)
            p, fitted = fit_standalone(pack, base, [s['structure'] for s in live], legacy)
            for s in rest:
                sid = s['structure']
                if sid in fitted:
                    w, c = fitted[sid]
                    structures[sid] = OrderedDict([('weight', w), ('frequency', c)])
                elif s in live:
                    structures[sid] = OrderedDict([('frequency', 1.0)])
                else:
                    structures[sid] = OrderedDict([('frequency', 1.0), ('enabled', False)])
        else:
            if live:
                p, achieved = scale_placement(base, max(mults))
            else:
                p, achieved = base, 1.0
            for s in rest:
                sid = s['structure']
                if s in live:
                    structures[sid] = OrderedDict([('frequency', round(min(1.0, pack.multiplier(sid) / achieved), 4))])
                else:
                    structures[sid] = OrderedDict([('frequency', 1.0), ('enabled', False)])
        ez = p.get('exclusion_zone')
        if isinstance(ez, dict) and ez.get('other_set') in fully_grouped:
            old_set = ez['other_set']
            dests = defaultdict(int)
            for s in pack.sets[old_set]['structures']:
                dests[owner[s['structure']]] += 1
            dest = max(dests, key=dests.get)
            old_d = grid_density(outputs[old_set][0][0]) or 0
            new_d = group_density[GROUP_SET_PREFIX + dest]
            old_loss = min(1.0, (2 * ez['chunk_count'] + 1) ** 2 * old_d)
            r = max(1, min(16, js_round((math.sqrt(old_loss / new_d) - 1) / 2)))
            p = OrderedDict(p)
            p['exclusion_zone'] = OrderedDict([('other_set', GROUP_SET_PREFIX + dest), ('chunk_count', r)])
            retargets.append(f'{set_id}: exclusion_zone {old_set} r{ez["chunk_count"]} -> {GROUP_SET_PREFIX + dest} r{r} '
                             f'(keeps ~{old_loss:.0%} of its slots excluded)')
        changed = OrderedDict((k, v) for k, v in p.items() if e['original'].get(k) != v)
        standalone[set_id] = OrderedDict([('placement', changed), ('structures', structures)])

    doc = OrderedDict([('_readme', groups_readme()), ('groups', groups), ('standalone', standalone)])
    for r in retargets:
        print('  retargeted ' + r)
    if dry:
        return legacy, doc

    # 4. write, archive first
    ARCHIVE.mkdir(parents=True, exist_ok=True)
    for f in (CONFIG, TAGS):
        if not (ARCHIVE / f.name).exists():
            shutil.copy2(f, ARCHIVE / f.name)
    write_json(GROUPS, doc)
    for t in pack.tag_defs.values():
        t.pop('multiplier', None)
    pack.tags_doc['_readme'] = tags_readme()
    write_json(TAGS, pack.tags_doc)
    for e in pack.sets.values():
        e.pop('current', None)
        for s in e['structures']:
            s.pop('enabled', None)
    write_json(CONFIG, pack.config)
    print(f'migrated: {len(groups)} groups, {len(standalone)} standalone sets -> {GROUPS.relative_to(INSTANCE)}')
    return legacy, doc


# ── sync ─────────────────────────────────────────────────────────────────────

def sync(pack, doc, refit=()):
    """Adds what's missing, fitted to mod defaults; never touches existing values, except that every group
    in refit gets a new grid and all its members refitted to the rates the file gives them now."""
    defaults, outputs = pack.old_densities(legacy=False)
    pack.legacy_placement = {m: p for outs in outputs.values() for p, mem in outs for m in mem}
    added = []
    groups = doc.setdefault('groups', OrderedDict())
    owner = {}
    for name in pack.group_names():
        for sid in pack.group_members(name):
            owner[sid] = name
    group_density = {GROUP_SET_PREFIX + n: g['placement'].get('frequency', 1) / g['placement']['spacing'] ** 2
                     for n, g in groups.items() if 'placement' in g}
    # a structure that moves (into a group, or between groups) keeps the rate the file gives it now
    current = doc_densities(pack, doc)
    targets = dict(defaults)
    targets.update({m: d for m, d in current.items() if d})
    for name in refit:
        if groups.pop(name, None) is not None:
            added.append(f'refit {name}: new grid, every member refitted to its current rate')
    carried = {}   # "tuned" / "note" of a member that moves group go with it
    for name, g in groups.items():
        for sid in [m for m in g.get('structures', {}) if owner.get(m) != name]:
            carried[sid] = OrderedDict((k, g['structures'][sid][k]) for k in ('note', 'tuned') if k in g['structures'][sid])
            del g['structures'][sid]
            added.append(f'removed {name}: {sid} (' + (f'now in group/{owner[sid]})' if sid in owner else 'in no group now)'))
    for name in pack.group_names():
        members = pack.group_members(name)
        g = groups.get(name)
        if g is None:
            spec = GROUP_SPECS.get(name, {})
            live = [m for m in members if m not in pack.blocked and m not in pack.converted]
            placement = group_grid(pack, name, spec, live, targets, group_density)
            g = groups[name] = OrderedDict([('note', pack.tag_defs['group/' + name].get('note', '')),
                                            ('placement', placement), ('structures', OrderedDict())])
            group_density[GROUP_SET_PREFIX + name] = 1 / placement['spacing'] ** 2
            added.append(f'group {name} (spacing {placement["spacing"]}, separation {placement["separation"]})')
        st = g.setdefault('structures', OrderedDict())
        for m in [m for m in members if m not in st and m in pack.converted]:
            st[m] = OrderedDict([('weight', 1), ('frequency', 1.0), ('note', META_NOTE)])
            added.append(f'{name}: {m} (meta structure with an anchor: weight 1, its rate is its share in meta_structures.json)')
        new = [m for m in members if m not in st and m not in pack.blocked]
        if not new:
            continue
        fixed = {m: (v.get('weight', MEDIAN_WEIGHT), v.get('frequency', 1.0))
                 for m, v in st.items() if v.get('enabled', True) and m in members and m not in pack.converted}
        p = g['placement']
        ez = p.get('exclusion_zone')
        loss = 0.0
        if isinstance(ez, dict):
            loss = min(1.0, (2 * ez.get('chunk_count', 0) + 1) ** 2 * group_density.get(ez.get('other_set'), 0))
        fitted = fit_members(pack, p, loss, list(fixed) + new, targets, fixed)
        for m in new:
            w, c = fitted.get(m, (MEDIAN_WEIGHT, 1.0))
            st[m] = OrderedDict([('weight', w), ('frequency', c)])
            st[m].update(carried.get(m, {}))
            added.append(f'{name}: {m} (weight {w}, frequency {c})' + (' (kept its "tuned" note: check it still holds)' if 'tuned' in st[m] else ''))
    standalone = doc.setdefault('standalone', OrderedDict())
    for set_id in list(standalone):
        entry = standalone[set_id]
        for sid in [m for m in entry.get('structures', {}) if m in owner]:
            del entry['structures'][sid]
            added.append(f'removed standalone {set_id}: {sid} (now in group/{owner[sid]})')
        if set_id in pack.sets and all(s['structure'] in owner for s in pack.sets[set_id]['structures']):
            del standalone[set_id]
            added.append(f'removed standalone {set_id} (every member is in a group now)')
    for set_id, e in pack.sets.items():
        rest = [s['structure'] for s in e['structures'] if s['structure'] not in owner]
        if not rest:
            continue
        entry = standalone.get(set_id)
        if entry is None:
            entry = standalone[set_id] = OrderedDict([('placement', OrderedDict()), ('structures', OrderedDict())])
            added.append(f'standalone {set_id}')
        for sid in rest:
            if sid not in entry['structures']:
                entry['structures'][sid] = OrderedDict([('frequency', 1.0)])
                added.append(f'standalone {set_id}: {sid}')
    return added


# ── estimates report ─────────────────────────────────────────────────────────

META_NOTE = ('Meta structure with an anchor: never picked by this weight; it takes "share" (meta_structures.json) of the '
             'slots its anchors win here, so no member\'s rate changes. Keep the weight at 1.')


def apply_meta_anchors(pack, doc, dens):
    """A meta structure with an anchor takes "share" of the slots its anchors win in its group (patcherMod MetaSlots):
    per biome of its pool, share x the anchors' own densities there. Upper bound: a meta whose parts don't fit is the
    plain anchor. The anchors keep their densities (a meta's anchor is still that structure). A replacement ("replaces"
    in structure_groups.json) is converted the same way from the one member it names, but that member loses what the
    replacement takes (in the biomes both can stand in). Shares of one anchor stack, cut at 1 in id order as the game does."""
    used = defaultdict(float)
    lost = defaultdict(lambda: defaultdict(float))
    for mid in sorted(pack.converted):
        group = next((g for g in doc.get('groups', {}).values() if mid in g.get('structures', {})), None)
        if group is None or not group['structures'][mid].get('enabled', True):
            dens.pop(mid, None)
            continue
        names = {pack.replaces[mid]} if mid in pack.replaces else pack.meta_entries(pack.meta[mid]['anchor'])
        anchors = [a for a in sorted(names) if a in group['structures'] and a in dens and a != mid]
        out = defaultdict(float)
        for a in anchors:
            take = max(0.0, min(pack.converted[mid], 1.0 - used[a]))
            used[a] += take
            for b, d in dens[a].items():
                if b in pack.pools.get(mid, ()):
                    out[b] += take * d
                    if mid in pack.replaces:
                        lost[a][b] += take * d
        dens[mid] = dict(out)
    for a, by_biome in lost.items():
        dens[a] = {b: d - by_biome.get(b, 0.0) for b, d in dens[a].items()}
    return dens


def doc_densities(pack, doc):
    """What structure_groups.json gives each structure by its own entries, ignoring the (possibly just
    edited) group tags: group entries on their group grid, standalone sets with all their members."""
    plans, set_density = [], {}
    for name, g in doc.get('groups', {}).items():
        p = g.get('placement', {})
        if not p.get('spacing'):
            continue
        set_density[GROUP_SET_PREFIX + name] = p.get('frequency', 1) / p['spacing'] ** 2
        plans.append((p, {m: (v.get('weight', MEDIAN_WEIGHT), v.get('frequency', 1.0))
                          for m, v in g.get('structures', {}).items()
                          if v.get('enabled', True) and v.get('frequency', 1.0) > 0 and m not in pack.blocked
                          and m not in pack.converted}, g.get('biome_frequency')))
    for set_id, entry in doc.get('standalone', {}).items():
        if set_id not in pack.sets:
            continue
        p = OrderedDict(pack.sets[set_id]['original'])
        p.update(entry.get('placement', {}))
        set_density[set_id] = grid_density(p)
        live = {}
        for s in pack.sets[set_id]['structures']:
            v = entry.get('structures', {}).get(s['structure'])
            if v is None or s['structure'] in pack.blocked:
                continue
            if v.get('enabled', True) and v.get('frequency', 1.0) > 0:
                live[s['structure']] = (v.get('weight', s['weight']), v.get('frequency', 1.0))
        plans.append((p, live, None))
    dens = {}
    for p, live, bf in plans:
        K = grid_density(p)
        if K is None:
            continue
        d = member_densities(K * (1 - exclusion_loss(p, set_density)), live, pack.pools)
        if bf:
            f = pack.biome_factor(bf)
            d = {sid: {b: v * f(b) for b, v in m.items()} for sid, m in d.items()}
        dens.update(d)
    return apply_meta_anchors(pack, doc, dens)


def new_densities(pack, doc):
    """Densities the NEW model gives, straight from structure_groups.json."""
    owner = {}
    for name in pack.group_names():
        for sid in pack.group_members(name):
            owner[sid] = name
    set_density = {}
    plans = []
    for name, g in doc.get('groups', {}).items():
        p = g['placement']
        set_density[GROUP_SET_PREFIX + name] = p.get('frequency', 1) / p['spacing'] ** 2
        live = {m: (v.get('weight', MEDIAN_WEIGHT), v.get('frequency', 1.0))
                for m, v in g.get('structures', {}).items()
                if v.get('enabled', True) and v.get('frequency', 1.0) > 0 and m not in pack.blocked
                and owner.get(m) == name and m not in pack.converted}
        plans.append((p, live, g.get('biome_frequency')))
    for set_id, e in pack.sets.items():
        entry = doc.get('standalone', {}).get(set_id, {})
        rest = [s for s in e['structures'] if s['structure'] not in owner]
        if not rest:
            set_density[set_id] = 0
            continue
        p = OrderedDict(e['original'])
        p.update(entry.get('placement', {}))
        set_density[set_id] = grid_density(p)
        live = {}
        for s in rest:
            v = entry.get('structures', {}).get(s['structure'], {})
            if v.get('enabled', True) and s['structure'] not in pack.blocked:
                live[s['structure']] = (v.get('weight', s['weight']), v.get('frequency', 1.0))
        plans.append((p, live, None))
    dens = {}
    for p, live, bf in plans:
        K = grid_density(p)
        if K is None:
            continue
        K *= 1 - exclusion_loss(p, set_density)
        d = member_densities(K, live, pack.pools)
        if bf:   # biome_frequency: each member's frequency x the factor of the biome (patcherMod StructureFrequencies)
            f = pack.biome_factor(bf)
            d = {sid: {b: v * f(b) for b, v in m.items()} for sid, m in d.items()}
        dens.update(d)
    return apply_meta_anchors(pack, doc, dens)


def write_estimates(pack, doc, baseline, baseline_label, path, title='Structure group estimates'):
    """baseline None = compare against mod defaults (no Before column, no drift list)."""
    new = new_densities(pack, doc)
    defaults, _ = pack.old_densities(legacy=False)
    migration = baseline is not None
    if not migration:
        baseline = defaults
    lines = [f'# {title}', '',
             'Written by `python3 tools/gen_structure_groups.py`. Do not edit. Expected structures per area for every '
             'structure, from `structure_groups.json`, each structure\'s biome pool and the real biome areas in '
             '`biome_config.json`. "1 per N" means one structure per N x N chunks, averaged over the biomes it can '
             'spawn in (so a structure limited to rare biomes is still rare overall). The game itself writes '
             '`structure_tag_report.md`; this file is the offline estimate.', '',
             f'Compared against: **{baseline_label}**. Ratio = new / that; 1.00 = unchanged.', '']
    owner = {}
    for name in pack.group_names():
        for sid in pack.group_members(name):
            owner[sid] = name

    def row(sid, v=None):
        pool = pack.pools.get(sid, frozenset())
        n = mean_density(new.get(sid, {}), pack.areas)
        o = mean_density(baseline.get(sid, {}), pack.areas)
        dd = mean_density(defaults.get(sid, {}), pack.areas)
        ratio = f'{n / o:.2f}' if o > 0 else ('-' if n == 0 else 'new')
        flags = []
        if sid in pack.blocked:
            flags.append('blocked in code')
        if sid in pack.replaces:
            flags.append(f'replaces `{pack.replaces[sid]}` in {pack.converted[sid]:.0%} of its slots')
        if not pool:
            flags.append('no biome in this pack' if pack.pool_all.get(sid)
                         else 'empty biome tag: never spawns unless a mod fills it at runtime')
        cells = [f'`{sid}`']
        if v is not None:
            cells += [str(v.get('weight', '-')), str(v.get('frequency', 1.0)) + ('' if v.get('enabled', True) else ' (off)')]
        cells += [fmt_sp(dd)] + ([fmt_sp(o)] if migration else []) + [fmt_sp(n), ratio, ', '.join(flags)]
        return '| ' + ' | '.join(cells) + ' |'

    lines += ['## Groups', '']
    for name, g in doc.get('groups', {}).items():
        p = g['placement']
        K = p.get('frequency', 1) / p['spacing'] ** 2
        lines.append(f'### `{name}` -> `{GROUP_SET_PREFIX}{name}`')
        lines.append('')
        lines.append(f'{g.get("note", "")}')
        lines.append('')
        lines.append(f'Grid: spacing {p["spacing"]}, separation {p["separation"]} (members never start closer than '
                     f'{p["separation"] + 1} chunks = {(p["separation"] + 1) * 16} blocks apart). At most 1 structure per '
                     f'{p["spacing"]} x {p["spacing"]} chunks anywhere.'
                     + (f' Excluded within {p["exclusion_zone"]["chunk_count"]} chunks of `{p["exclusion_zone"]["other_set"]}` grid slots.'
                        if isinstance(p.get('exclusion_zone'), dict) else ''))
        # crowding: biomes where the old total exceeded the group's capacity
        T, Tn = defaultdict(float), defaultdict(float)
        for sid in g.get('structures', {}):
            for b, d in baseline.get(sid, {}).items():
                T[b] += d
            for b, d in new.get(sid, {}).items():
                Tn[b] += d
        capped = sorted((b for b in T if T[b] > K * 1.05), key=lambda b: -pack.areas[b] * T[b])
        if capped:
            share = sum(pack.areas[b] for b in capped) / max(1e-12, sum(pack.areas[b] for b in T))
            lines.append('')
            lines.append(f'Capped (old combined density above the group grid): {len(capped)} biome(s), '
                         f'{share:.0%} of the group\'s area. Largest: '
                         + ', '.join(f'`{b}` 1 per {fmt_sp(T[b])} -> {fmt_sp(Tn[b])}' for b in capped[:6]) + '.')
        lines.append('')
        lines += header_rows(migration)
        for sid, v in g.get('structures', {}).items():
            lines.append(row(sid, v))
        lines.append('')

    lines += ['## Standalone sets (changed ones only)', ''] + header_rows(migration)
    for set_id, entry in doc.get('standalone', {}).items():
        touched = entry.get('placement') or any(v.get('frequency', 1.0) != 1.0 or not v.get('enabled', True)
                                                for v in entry.get('structures', {}).values())
        if not touched:
            continue
        for sid, v in entry.get('structures', {}).items():
            vv = OrderedDict(v)
            vv.setdefault('weight', pack.weight_in_set(sid))
            lines.append(row(sid, vv))
    lines.append('')

    drifted = []
    if migration:
        for sid in pack.all_ids:
            n = mean_density(new.get(sid, {}), pack.areas)
            o = mean_density(baseline.get(sid, {}), pack.areas)
            if o > 0 and (n / o < 0.8 or n / o > 1.25):
                drifted.append((n / o, sid, owner.get(sid, 'standalone')))
        lines += ['## Changed by more than 20%', '']
        if drifted:
            for r, sid, where in sorted(drifted):
                lines.append(f'- `{sid}` ({where}): x{r:.2f}')
        else:
            lines.append('None.')
        lines.append('')
    path.write_text('\n'.join(lines) + '\n')
    return drifted


def header_rows(migration):
    cols = ['Structure', 'Weight', 'Frequency', 'Mod default 1 per'] + (['Before 1 per'] if migration else []) \
        + ['New 1 per', 'Ratio', 'Note']
    return ['| ' + ' | '.join(cols) + ' |', '|' + '---|' * len(cols)]


def migration_report(pack, doc):
    """Current structure_groups.json against the tuning it replaced (read from the archive)."""
    if not (ARCHIVE / CONFIG.name).exists():
        return None
    old = Pack(ARCHIVE / CONFIG.name, ARCHIVE / TAGS.name)
    # where a structure can spawn comes from the jars, not the config: judge both over today's pools
    # (the archived config predates Hopo moving both shipwrecks onto beaches, for one)
    old.pools, old.pool_all = pack.pools, pack.pool_all
    legacy, _ = old.old_densities(legacy=True)
    # structures the old config never knew (mods added after it was written) ran at their defaults
    defaults, _ = pack.old_densities(legacy=False)
    for sid in pack.all_ids:
        if sid not in old.set_of:
            legacy[sid] = defaults.get(sid, {})
    return write_estimates(pack, doc, legacy, 'the tuning before groups (archived 2026-09-27)', MIGRATION_REPORT,
                           'Structure groups vs the tuning they replaced')


# ── readmes ──────────────────────────────────────────────────────────────────

def groups_readme():
    return OrderedDict([
        ('what', 'Every structure\'s spawn rate, in one file. A GROUP is one shared structure_set '
                 '(creativeengineering:group/<name>) holding every structure in the structure tag group/<name> '
                 '(structure_tags.json): all members share its grid, so no two of them ever start closer than its '
                 'separation, whatever mod they come from. Every structure that is in no group keeps its own set '
                 'and is tuned under "standalone". Rejoin the world to apply (new chunks only); '
                 'structure_tag_report.md shows what the game applied.'),
        ('group placement', 'spacing: one grid slot per spacing x spacing chunks (the group\'s top density). '
                            'separation: minimum chunks between any two members. salt: which grid (leave it). '
                            'Optional: spread_type, frequency (chance per slot for the whole group), locate_offset, '
                            'exclusion_zone {other_set, chunk_count} (keep off another set\'s grid slots; it checks '
                            'slots, not real structures, so keep chunk_count small).'),
        ('per structure', 'weight: who wins a slot where several members can spawn (relative, whole numbers). '
                          'frequency: 0..1, chance that the winner really spawns; otherwise the slot stays empty, so '
                          'lowering one never makes another more common. enabled: false removes it. Where a '
                          'structure is the only member that fits the biome, it gets every slot x its frequency. '
                          'fit_base: written by --fit-rates for structures in a biome fit (biome_fits.json) = the '
                          'weight and frequency it had on its mod\'s own biome list; --fit-rates solves from it. '
                          'fit_keep: true lets --fit-rates raise the weight above fit_base when new neighbours crowd '
                          'the structure\'s biomes, so it keeps its density (the towers); others are capped instead; '
                          'fit_keep: false = --fit-rates never touches it (set by --strip-weights). '
                          'tuned: "<what, when, who decided>" marks a weight or frequency a person chose; '
                          '--strip-weights keeps it and sync carries it when the member moves group. '
                          'replaces + share: this member never wins a slot by its weight; it takes share (0..1) of the '
                          'slots the named member of the same group wins and keeps, where both can stand (the vanilla '
                          'swamp hut in YUNG\'s witch hut slots), so the two together are as common as the named one was. '
                          'Keep its weight at 1. CEConfig [worldgen.systems] structureReplacements turns them off.'),
        ('standalone', 'Keyed by the structure_set id in structure_config.json. placement: only the fields you '
                       'change (spacing, separation, frequency, count, locate_offset...; the rest come from '
                       '"original" in structure_config.json). structures: frequency / enabled / optional weight, '
                       'same meaning as in a group.'),
        ('moving a structure', 'Edit the group/* tags in structure_tags.json, then run python3 '
                               'tools/gen_structure_groups.py: it adds the new member with a weight and frequency '
                               'fitted to its mod default and leaves everything else alone. A tagged member with no '
                               'entry here still works (the game fills in a rough default and flags it).'),
        ('estimates', 'python3 tools/gen_structure_groups.py --check rewrites structure_group_estimates.md: '
                      'expected structures per area for every structure, from its real biome areas.'),
        ('biome rules', 'biome_rules.<name>: {biomes: [biome ids / #biome tags], allow: "#<structure tag>"}. Only '
                        'the tag\'s structures may start in those biomes; every other structure loses them from its '
                        'own biome list (patcherMod StructureBiomeRules, which also covers structures no config '
                        'knows yet). water keeps the open sea for ships, wrecks and ocean ruins.'),
    ])


def tags_readme():
    return OrderedDict([
        ('what', 'Named lists of structures. group/* tags decide which structures share a group grid '
                 '(structure_groups.json); every other tag is for grouping, reports and building those lists. '
                 'Tags have no multipliers any more (2026-09-27: every rate lives in structure_groups.json).'),
        ('values', 'Entries: "ns:id" (one structure), "ns:*" or "ns:prefix_*" (glob, * matches anything including /), '
                   '"#tag/name" (every member of another tag in this file), and a leading "!" on any of those to '
                   'exclude (exclusions win, whatever their position). Unknown ids and tags are errors (logged, listed '
                   'in the report, and NO structure changes are applied until fixed), so a typo never silently does nothing.'),
        ('groups', 'A structure may be in at most one group/* tag. After changing one, run python3 '
                   'tools/gen_structure_groups.py to give new members a fitted weight and frequency.'),
        ('families', 'mod/* = one tag per namespace (plus mod/yungs for YUNG\'s 6). type/* = what the structure is; a '
                     'structure can have more than one type. dungeon/* = dungeons split by location (surface / '
                     'underground) and origin (vanilla / better = YUNG\'s / modded). group/* = the shared grids. meta/* = the '
                     'structures a meta structure may use as its anchor or parts (meta_structures.json); each is also a '
                     'real structure tag creativeengineering:meta/<name>, added by structure_overrides.js. land and '
                     'land/* = biome fits (biome_fits.json): a structure in one spawns in that fit\'s biomes plus its '
                     'parents\' (land/oak: land/oak + land biomes) instead of its own list; each is also a real '
                     'structure tag creativeengineering:<name>.'),
    ])


# ── biome fits ───────────────────────────────────────────────────────────────

def surface_mean(dmap, pool, surface, areas):
    """Structures per chunk over the structure's surface biomes (area-weighted mean density)."""
    bs = [b for b in pool if b in surface]
    a = sum(areas[b] for b in bs)
    return sum(dmap.get(b, 0) * areas[b] for b in bs) / a if a else 0.0


KEEP_MAX = 64   # fit_keep: highest weight tried, x the base weight


def fit_rates(pack, doc, passes=4):
    """--fit-rates: every structure in a biome fit gets the weight and frequency that keep its mean density over its
    surface biomes what it was over its mod's own list (more biomes, same crowding).

    The weight decides how much the other members of its group move: a high weight with a low frequency wins slots in
    the new biomes and then vetoes them (empty), starving the members there; a low weight hands the slots it held in
    its old biomes to the members there. So a group member tries every weight from 1 to its base weight that can
    still reach the density at frequency <= 1, takes the frequency that hits it, and keeps the weight that moves the
    other members of its set least (sum of |log(new total / total before the fits)|). Standalone members (no weight in
    the file) only get a frequency. Members sharing biomes are solved together over a few passes.

    A member with fit_keep: true (the towers) that can't reach its density at its base weight, because the fits put
    heavier members into its biomes, gets the smallest weight above the base that does (up to KEEP_MAX x the base);
    the others in those biomes give up the slots. Without it the member is capped at its base weight.

    fit_base = {weight, frequency} on the mod's own list; every run starts from it (no compounding), and a structure
    that leaves every fit gets it back. A density out of reach at the base weight and frequency 1 is reported."""
    import copy

    def entries_of(d):
        out = {}
        for g in d.get('groups', {}).values():
            out.update({k: (v, True) for k, v in g.get('structures', {}).items()})
        for e in d.get('standalone', {}).values():
            out.update({k: (v, False) for k, v in e.get('structures', {}).items()})
        return out

    def base_of(v):
        b = v.get('fit_base')
        if isinstance(b, dict):
            return b
        out = OrderedDict()
        if 'weight' in v:
            out['weight'] = v['weight']
        out['frequency'] = b if isinstance(b, (int, float)) else v.get('frequency', 1.0)
        return out

    def put(v, weight, frequency):
        if weight is not None:
            v['weight'] = weight
        v['frequency'] = frequency

    entries = entries_of(doc)
    changed = []
    for sid, (v, _) in entries.items():
        if sid not in pack.fitted and 'fit_base' in v:
            b = base_of(v)
            put(v, b.get('weight'), b['frequency'])
            del v['fit_base']
            changed.append(f'fit rate {sid}: in no fit now, back to weight {v.get("weight", "-")}, frequency {v["frequency"]}')
    # replacements and metas take their rate from the slots they convert, not from a weight
    # fit_keep: false (stripped weights, --strip-weights) is never re-solved
    todo = [sid for sid in pack.fitted if sid in entries and entries[sid][0].get('enabled', True) and sid not in pack.converted
            and entries[sid][0].get('fit_keep') is not False]
    if not todo:
        return changed
    bases = {sid: base_of(entries[sid][0]) for sid in todo}
    own = Pack(fits=False)
    surface = {b for dim in read_json(BIOME_CONFIG)['dimensions'].values()
               for b, info in dim['biomes'].items() if info.get('layer', 'surface') == 'surface'}

    # targets: each structure's density on its own list, everything at its base values
    base_doc = copy.deepcopy(doc)
    base_entries = entries_of(base_doc)
    for sid in todo:
        put(base_entries[sid][0], bases[sid].get('weight'), bases[sid]['frequency'])
    before = new_densities(own, base_doc)
    target = {sid: surface_mean(before.get(sid, {}), own.pools.get(sid, ()), surface, pack.areas) for sid in todo}

    work = base_doc
    work_entries = base_entries
    group_of = {}
    for name, g in doc.get('groups', {}).items():
        for m in g.get('structures', {}):
            group_of[m] = [x for x in g['structures'] if x != m]

    def total(dens, pk, sid):
        return sum(dens.get(sid, {}).get(b, 0) * pk.areas[b] for b in pk.pools.get(sid, ()) if b in surface)

    before_total = {m: total(before, own, m) for ms in group_of.values() for m in ms}

    def disturbance(sid):
        dens = new_densities(pack, work)
        return sum(abs(math.log((total(dens, pack, m) + 1e-9) / (before_total[m] + 1e-9)))
                   for m in group_of.get(sid, ()) if before_total.get(m))

    def mean_at(sid, weight, frequency):
        v = work_entries[sid][0]
        put(v, weight, frequency)
        return surface_mean(new_densities(pack, work).get(sid, {}), pack.pools.get(sid, ()), surface, pack.areas)

    short = {}
    for _ in range(passes):
        for sid in todo:
            T = target[sid]
            v, in_group = work_entries[sid]
            wb = bases[sid].get('weight')
            if not T or not pack.pools.get(sid):
                continue
            if in_group and wb:
                cands = sorted({w for w in range(1, min(wb, 24) + 1)} | {wb} |
                               {max(1, js_round(wb * x / 24)) for x in range(1, 25)})
                feasible = [w for w in cands if mean_at(sid, w, 1.0) >= T]
                if not feasible and v.get('fit_keep'):
                    # fit_keep: new neighbours crowd its biomes; take the smallest weight above the base that
                    # still reaches the density (frequency stays near 1, so few slots are won and left empty)
                    w = wb
                    while mean_at(sid, w, 1.0) < T and w < wb * KEEP_MAX:
                        w = max(w + 1, js_round(w * 1.2))
                elif not feasible:
                    w = wb
                else:
                    scored = []
                    for c in feasible:
                        m = mean_at(sid, c, 1.0)
                        put(v, c, round6(min(1.0, T / m)))
                        scored.append((disturbance(sid), c))
                    w = min(scored)[1]
            else:
                w = None
            m = mean_at(sid, w, 1.0)
            want = T / m if m else 1.0
            short[sid] = want
            put(v, w, round6(min(1.0, want)))

    capped = sorted((1 / short[sid], sid) for sid in todo if short.get(sid, 0) > 1.01)
    if capped:
        print(f'{len(capped)} fitted structure(s) capped (frequency 1 at the base weight; their biomes are full):')
        for share, sid in capped:
            print(f'  {sid}: {share:.0%} of its old density')
    for sid in todo:
        v = entries[sid][0]
        nv = work_entries[sid][0]
        old = (v.get('weight'), v.get('frequency', 1.0))
        new = (nv.get('weight'), nv['frequency'])
        v['fit_base'] = bases[sid]
        if old != new:
            put(v, new[0], new[1])
            changed.append(f'fit rate {sid}: weight {old[0]} -> {new[0]}, frequency {old[1]} -> {new[1]} '
                           f'(base {bases[sid].get("weight", "-")} / {bases[sid]["frequency"]})'
                           + (f'; capped, reaches {1 / short[sid]:.0%} of its old density' if short.get(sid, 0) > 1 else ''))
    return changed


def strip_weights(pack, doc, only=()):
    """--strip-weights: weights nobody chose go back to MEDIAN_WEIGHT (user 2026-10-19: "strip all structure weights
    unless an explicit increase or decrease note"). Skipped: members with "tuned", metas and replacements (weight 1 by
    design). Frequencies stay (they hold the migration's rarity; a later pass may reset them after a census).
    Returns (changed lines, [(sid, group, old weight, new weight, frequency)])."""
    changed, rows = [], []
    for name, g in doc.get('groups', {}).items():
        if only and name not in only:
            continue
        for sid, v in g.get('structures', {}).items():
            if 'tuned' in v or sid in pack.converted or 'replaces' in v:
                continue
            old = v.get('weight', MEDIAN_WEIGHT)
            touched = old != MEDIAN_WEIGHT or 'fit_base' in v or v.get('fit_keep') is not False
            if not touched:
                continue
            v['weight'] = MEDIAN_WEIGHT
            v.pop('fit_base', None)
            v['fit_keep'] = False
            rows.append((sid, name, old, MEDIAN_WEIGHT, v.get('frequency', 1.0)))
            if old != MEDIAN_WEIGHT:
                changed.append(f'strip {name}: {sid} weight {old} -> {MEDIAN_WEIGHT} (frequency {v.get("frequency", 1.0)} kept)')
    if not only:
        for set_id, e in doc.get('standalone', {}).items():
            for sid, v in e.get('structures', {}).items():
                if 'tuned' in v or 'weight' not in v:
                    continue
                old = v.pop('weight')
                v.pop('fit_base', None)
                v['fit_keep'] = False
                mod_w = next((s['weight'] for s in pack.sets.get(set_id, {}).get('structures', []) if s['structure'] == sid), '-')
                rows.append((sid, 'standalone ' + set_id, old, mod_w, v.get('frequency', 1.0)))
                changed.append(f'strip standalone {set_id}: {sid} weight {old} -> mod default {mod_w}')
    return changed, rows


# ── main ─────────────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[1])
    ap.add_argument('--migrate', action='store_true')
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--dry-run', metavar='ESTIMATES_MD', help='with --migrate: write nothing but this estimates file')
    ap.add_argument('--worldgen-dir', help='use the configs in this folder instead (tests)')
    ap.add_argument('--refit', nargs='+', metavar='GROUP',
                    help='rebuild these groups\' grids and refit every member to its current rate (after adding members '
                         'much denser than the grid, e.g. meteorites into sky)')
    ap.add_argument('--fit-rates', action='store_true',
                    help='after editing biome_fits.json or the fit structure tags: rescale each fitted structure\'s frequency '
                         'so its density per area of its (new) biomes stays what it was over its mod\'s own list')
    ap.add_argument('--strip-weights', nargs='*', metavar='GROUP',
                    help='weights nobody chose (no "tuned", not a meta or replacement) back to MEDIAN_WEIGHT, frequencies '
                         'kept; no GROUP = every group and the standalone overrides. Estimates compare to the file before')
    ap.add_argument('--migration-report', action='store_true',
                    help='also rewrite structure_group_migration.md (current file vs the archived pre-groups tuning)')
    args = ap.parse_args()
    if args.worldgen_dir:
        global WG, CONFIG, TAGS, GROUPS, ESTIMATES, MIGRATION_REPORT
        WG = Path(args.worldgen_dir).resolve()
        CONFIG, TAGS, GROUPS = WG / 'structure_config.json', WG / 'structure_tags.json', WG / 'structure_groups.json'
        ESTIMATES, MIGRATION_REPORT = WG / 'structure_group_estimates.md', WG / 'structure_group_migration.md'
    pack = Pack()
    if args.migrate:
        legacy, doc = migrate(pack, dry=bool(args.dry_run))
        if args.dry_run:
            out = Path(args.dry_run).resolve()
            json.dump(doc, open(out.with_suffix('.json'), 'w'), indent=2)
            drifted = write_estimates(pack, doc, legacy, 'the tuning before groups', out)
            print(f'dry run -> {out} ({len(drifted)} structure(s) changed by more than 20%)')
            return
        pack = Pack()
        doc = read_json(GROUPS)
    else:
        if not GROUPS.exists():
            sys.exit(f'{GROUPS.name} is missing: run with --migrate (first time) or restore it.')
        doc = read_json(GROUPS)
        if args.strip_weights is not None:
            import copy
            before_doc = copy.deepcopy(doc)
            before = new_densities(pack, before_doc)
            changed, rows = strip_weights(pack, doc, tuple(args.strip_weights))
            after = new_densities(pack, doc)
            print(f'stripped {len(rows)} member(s), {len(changed)} weight(s) changed:')
            print(f'{"structure":60} {"where":28} {"weight":>12} {"freq":>9} {"rate ratio":>10}')
            out = []
            for sid in sorted(set(before) | set(after)):
                o = mean_density(before.get(sid, {}), pack.areas)
                n = mean_density(after.get(sid, {}), pack.areas)
                if o > 0 and abs(n / o - 1) > 0.005 or (o == 0) != (n == 0):
                    out.append((n / o if o else float('inf'), sid))
            by = {r[0]: r for r in rows}
            for ratio, sid in sorted(out, key=lambda t: -abs(math.log(t[0])) if 0 < t[0] < float('inf') else -99):
                r = by.get(sid)
                w = f'{r[2]} -> {r[3]}' if r else '(unchanged)'
                print(f'{sid:60} {(r[1] if r else ""):28} {w:>12} {(r[4] if r else ""):>9} {ratio:>10.2f}')
            write_json(GROUPS, doc)
            write_estimates(pack, doc, before, 'structure_groups.json before --strip-weights', ESTIMATES,
                            title='Structure group estimates (after --strip-weights)')
            print(f'estimates against the file before -> {ESTIMATES} (run --check to compare to mod defaults again)')
            return
        if not args.check:
            added = sync(pack, doc, args.refit or ())
            if args.fit_rates:
                added += fit_rates(pack, doc)
            if added:
                write_json(GROUPS, doc)
                print(f'changed {len(added)}:')
                for a in added:
                    print('  ' + a)
            else:
                print('structure_groups.json already covers every structure')
    write_estimates(pack, doc, None, 'mod defaults', ESTIMATES)
    print(f'estimates -> {ESTIMATES}')
    if args.migrate or args.migration_report:
        drifted = migration_report(pack, doc)
        if drifted is not None:
            print(f'migration comparison -> {MIGRATION_REPORT} ({len(drifted)} structure(s) changed by more than 20%)')


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""
Turns the analyzer's numbered proposals + the user's decisions into:
  kubejs/generation_scripts/worldgen/biome_merges.json       approved additive tag merges (source of truth)
  kubejs/generation_scripts/worldgen/biome_merge_suggestions.md   annotated review report

kubejs/server_scripts/worldgen/biome_merges.js applies biome_merges.json on every tag load.

Re-running REGENERATES biome_merges.json from the constants below, overwriting hand edits to it (put
lasting changes in the constants instead). Safe after mod changes: decisions are keyed by
(theme, target mod), not by proposal number; proposals for removed mods are skipped and new ones are
reported as undecided without being applied.

Decisions live in the constants below. Global rules:
  - skylands biomes are never added anywhere (structures already in them are only LISTED)
  - cave biomes are never added to non-cave-native structures (withheld + marked)
"""
import json
import re
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import analyze_structure_biome_merges as A

WORLDGEN = A.WORLDGEN_DIR
OUT_MERGES = WORLDGEN / 'biome_merges.json'
OUT_MD = WORLDGEN / 'biome_merge_suggestions.md'
TERRALITH_JAR = A.MODS_DIR / 'Terralith_1.19.3_v2.3.12.jar'

# Decisions are keyed by (theme category, target mod). The analyzer numbers proposals in sorted
# order, so the numbers shift whenever a structure mod is added or removed: they're display-only.
# A proposal whose key isn't listed here is reported as NEW and not applied until it's added.
# Dropped 2026-09-26 with the mod: ('badlands', 'minecolonies'), ('desert', 'minecolonies').
APPROVED = [
    ('badlands', 'towns_and_towers'), ('beach', 'towns_and_towers'), ('desert', 'betterdeserttemples'),
    ('desert', 'minecraft'), ('desert', 'philipsruins'), ('desert', 'towns_and_towers'), ('end', 'philipsruins'),
    ('forest', 'minecraft'), ('forest', 'towns_and_towers'), ('jungle', 'betterjungletemples'),
    ('jungle', 'minecraft'), ('jungle', 'themissingvillages'), ('jungle', 'towns_and_towers'),
    ('mushroom', 'towns_and_towers'), ('plains', 'towns_and_towers'), ('savanna', 'minecraft'),
    ('savanna', 'towns_and_towers'), ('snowy', 'minecraft'), ('snowy', 'towns_and_towers'),
    ('swamp', 'betterwitchhuts'), ('swamp', 'themissingvillages'), ('swamp', 'towns_and_towers'),
    ('taiga', 'towns_and_towers'),
]
END = ('end', 'philipsruins')
MANSION = ('forest', 'minecraft')
TT_FOREST, TT_PLAINS, TT_SNOWY = ('forest', 'towns_and_towers'), ('plains', 'towns_and_towers'), ('snowy', 'towns_and_towers')
HELD = {END: 'held for a per-structure breakout (End matrix below)'}
VARIANT_MATCHED = {TT_FOREST, TT_PLAINS, TT_SNOWY}   # additions chosen per biome-variant structure, not blanket
DARK_FOREST_ONLY = {MANSION}                          # woodland mansion: dark-forest-like biomes only
EXTRA_EXCLUDE = {('taiga', 'towns_and_towers'): {'terralith:bryce_canyon'}}   # hot biome in a taiga list
MODIFIED_EXTRA = {('mushroom', 'towns_and_towers'), ('taiga', 'towns_and_towers')}   # hand-edited, reported as modified

# Evidence-based picks (trunk-log / vegetation analysis of the Terralith biome jsons).
DARK_FOREST_LIKE = {'terralith:sakura_valley'}          # dark oak canopy trunks; sister of sakura_grove, which Terralith already gives mansions
BIRCH_LIKE = {'terralith:birch_taiga', 'terralith:white_cliffs'}   # birch-dominant trunks
GENERIC_FOREST_LIKE = {'terralith:temperate_highlands'}            # oak/birch/dark-oak mix, temperate
SUNFLOWER_LIKE = set()   # only skylands_summer has sunflowers (excluded) -> nothing qualifies

CAVE_NATIVE_VANILLA = {'minecraft:mineshaft', 'minecraft:mineshaft_mesa', 'minecraft:stronghold', 'minecraft:ancient_city'}
CAVE_STEPS = {'underground_structures', 'strongholds', 'underground_decoration'}


# ---------------------------------------------------------------------------- Terralith biome info
_tz = zipfile.ZipFile(TERRALITH_JAR)
_tnames = set(_tz.namelist())


def _load(kind, fid):
    ns, _, p = fid.partition(':')
    if ns != 'terralith':
        return None
    n = f'data/terralith/worldgen/{kind}/{p}.json'
    if n not in _tnames:
        return None
    txt = re.sub(r',\s*([}\]])', r'\1', _tz.read(n).decode('utf-8', 'replace'))
    try:
        return json.loads(txt)
    except Exception:
        return None


def _walk(obj, out, seen):
    if isinstance(obj, dict):
        if isinstance(obj.get('Name'), str):
            out.add(obj['Name'].replace('minecraft:', ''))
        for v in obj.values():
            _walk(v, out, seen)
    elif isinstance(obj, list):
        for v in obj:
            _walk(v, out, seen)
    elif isinstance(obj, str):
        for kind in ('placed_feature', 'configured_feature'):
            if (kind, obj) not in seen:
                d = _load(kind, obj)
                if d is not None:
                    seen.add((kind, obj))
                    _walk(d, out, seen)


def biome_info(biome):
    """'temp 0.7 / rain 0.8 rain; trunks: birch,oak; flowers: ...' for a terralith biome, else ''."""
    ns, _, path = biome.partition(':')
    d = _load('biome', biome)
    if d is None:
        return ''
    blocks, seen = set(), set()
    for step in d.get('features', []):
        for f in step:
            _walk(f, blocks, seen)
    trunks = sorted(b.replace('_log', '') for b in blocks if b.endswith('_log') and not b.startswith('stripped'))
    flowers = sorted(b for b in blocks if b in {
        'sunflower', 'lilac', 'peony', 'rose_bush', 'cornflower', 'lily_of_the_valley', 'allium', 'azure_bluet',
        'oxeye_daisy', 'blue_orchid', 'poppy', 'dandelion'} or b.endswith('_tulip'))
    return (f"temp {d.get('temperature')}, downfall {d.get('downfall')}, {d.get('precipitation')}"
            f"; trunks: {', '.join(trunks) or '-'}; flowers: {', '.join(flowers) or '-'}")


# ---------------------------------------------------------------------------- main build
def structure_steps():
    steps = {}
    for jar in sorted(A.MODS_DIR.glob('*.jar')):
        try:
            z = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            m = re.match(r'^data/([^/]+)/worldgen/structure/(.+)\.json$', n)
            if m:
                try:
                    steps[f'{m.group(1)}:{m.group(2)}'] = json.loads(z.read(n)).get('step')
                except Exception:
                    pass
    return steps


def main():
    ctx = A.compute()
    raw, cache, cats = ctx['raw_tags'], ctx['cache'], ctx['category_biomes']
    proposals = {p['id']: p for p in ctx['proposals']}
    key_of = {n: (p['category'], p['target_mod']) for n, p in proposals.items()}
    num = {k: n for n, k in key_of.items()}
    for k in APPROVED:
        if k not in num:
            print(f'decision {k[0]} -> {k[1]} has no proposal any more (mod removed?), skipped')
    approved = sorted(num[k] for k in APPROVED if k in num)
    undecided = sorted(n for n, k in key_of.items() if k not in APPROVED)
    for n in undecided:
        print(f'NEW proposal {n} ({key_of[n][0]} -> {key_of[n][1]}) is undecided: not applied (add it to APPROVED)')
    nums = lambda keys: {num[k] for k in keys if k in num}
    held = {num[k]: v for k, v in HELD.items() if k in num}
    variant_matched, dark_forest_only = nums(VARIANT_MATCHED), nums(DARK_FOREST_ONLY)
    extra_exclude = {num[k]: v for k, v in EXTRA_EXCLUDE.items() if k in num}
    n_end, n_mansion = num.get(END), num.get(MANSION)

    R = lambda *ts: set().union(*[A.resolve_tag(t, raw, cache) for t in ts])
    modded = lambda s: {b for b in s if not b.startswith('minecraft:')}

    all_biomes = {v for vals in raw.values() for v in vals if isinstance(v, str) and not v.startswith('#')}
    skylands = {b for b in all_biomes if 'skylands' in b}
    cave = ({b for b in all_biomes if b.startswith('terralith:cave/')} | R('c:caves', 'forge:is_underground')
            | {'minecraft:dripstone_caves', 'minecraft:lush_caves', 'minecraft:deep_dark', 'quark:glimmering_weald'})

    # pools + tag users
    fields, steps = ctx['field_by_struct'], structure_steps()
    pools = {}
    for set_id, e in ctx['cfg']['structure_sets'].items():
        for s in e['structures']:
            if s['structure'] not in pools:
                pool, tags = A.structure_pool(s['structure'], fields.get(s['structure']), raw, cache)
                pools[s['structure']] = (pool or frozenset(), tags, set_id)
    tag_users = defaultdict(set)
    for sid, f in fields.items():
        for v in ([f] if isinstance(f, str) else (f or [])):
            if isinstance(v, str) and v.startswith('#'):
                tag_users[v[1:]].add(sid)
    for sid, (pool, tags, _) in pools.items():
        for t in tags:
            tag_users[t].add(sid)

    def cave_native(sid):
        return (sid in CAVE_NATIVE_VANILLA or steps.get(sid) in CAVE_STEPS
                or '/underground' in sid or ':underground/' in sid)

    # variant biome sets ------------------------------------------------------------------------
    snowy = modded(R('c:snowy', 'forge:is_snowy')) - skylands - cave
    grove = snowy & R('c:mountain_slope') & R('c:tree_coniferous')
    slopes = (snowy & R('c:mountain_slope')) - grove
    beach = snowy & R('c:beach', 'c:stony_shores')
    plains = snowy & R('c:snowy_plains', 'forge:is_plains')
    taiga = (snowy & R('c:tree_coniferous')) - grove - plains
    snowy_variant = {'grove': grove, 'snowy_slopes': slopes, 'snowy_beach': beach,
                     'snowy_plains': plains, 'snowy_taiga': taiga}
    flower = modded(R('c:flower_forests')) - skylands - cave

    def variant_adds(sid, proposal):
        path = sid.split(':')[1]
        if proposal == num.get(TT_SNOWY):
            for key, biomes in snowy_variant.items():
                if path.endswith(key):
                    return set(biomes)
        if proposal == num.get(TT_FOREST):
            if path.endswith('birch_forest'):
                return set(BIRCH_LIKE)
            if path.endswith('flower_forest'):
                return set(flower)
            return set(GENERIC_FOREST_LIKE)
        if proposal == num.get(TT_PLAINS):
            return set(SUNFLOWER_LIKE)
        return None

    # per-structure additions -----------------------------------------------------------------
    merges = defaultdict(lambda: {'add': set(), 'proposals': set(), 'structures': set()})
    removed_log = defaultdict(lambda: {'skylands': set(), 'cave': set(), 'other': set()})   # proposal -> reasons
    unapplied = []
    per_struct_final = {}
    for ps in ctx['per_structure']:
        n = ps['proposal']
        pool = pools[ps['structure']][0]
        if n in held or n in undecided:
            continue
        missing = set(ps['missing'])
        if n in variant_matched:
            wanted = variant_adds(ps['structure'], n) - pool
        elif n in dark_forest_only:
            wanted = DARK_FOREST_LIKE - pool
        else:
            wanted = set(missing)
        wanted -= extra_exclude.get(n, set())
        sky = wanted & skylands
        cv = set() if cave_native(ps['structure']) else (wanted & cave)
        removed_log[n]['skylands'] |= (missing & skylands)
        removed_log[n]['cave'] |= (missing & cave)
        removed_log[n]['other'] |= (missing & extra_exclude.get(n, set()))
        wanted -= sky | cv
        per_struct_final[ps['structure']] = wanted
        if not ps['target_tags']:
            unapplied.append(ps['structure'])
            continue
        for t in ps['target_tags']:
            if wanted:
                m = merges[t]
                m['add'] |= wanted
                m['proposals'].add(n)
                m['structures'].add(ps['structure'])

    tags_out = {}
    for t, m in sorted(merges.items()):
        tags_out[t] = {
            'add': sorted(m['add']),
            'proposals': sorted(m['proposals']),
            'structures': sorted(m['structures']),
            'also_affects': sorted(tag_users[t] - m['structures']),
        }
    OUT_MERGES.write_text(json.dumps({
        '_readme': ('Approved additive biome-tag merges. Edit "add" (biome ids) per tag, then /reload: '
                    'kubejs/server_scripts/worldgen/biome_merges.js adds them to each tag on every tag load '
                    '(additive, the mod\'s own contents stay; unknown biome ids are skipped with a warning). '
                    '"proposals" / "structures" / "also_affects" are read-only notes; "also_affects" = other '
                    'structures that reference the same tag and therefore also gain these biomes. '
                    'tools/build_biome_merges.py would regenerate this file from its constants, overwriting edits.'),
        'tags': tags_out}, indent=2))

    # ----------------------------------------------------------------------------- report
    L = []
    w = L.append
    executed = [n for n in approved if n not in held]
    modified = sorted(variant_matched | dark_forest_only | nums(MODIFIED_EXTRA))
    w('# Structure biome merges: status + review')
    w('')
    w(f'{len(executed)} proposals executed ({len(tags_out)} additive tag files), {len(held)} held, {len(undecided)} new/undecided. '
      'Approved state lives in `biome_merges.json`; `tools/build_biome_merges.py` regenerates this file.')
    w('')
    w('Global rules applied: **skylands biomes are never added; cave biomes are never added to non-cave-native structures.**')
    w('')
    w('## Status by proposal')
    w('')
    w('| # | theme -> target | status | notes |')
    w('|---|---|---|---|')
    tag_variant_blanket = {}
    for n in sorted(approved + undecided):
        p = proposals[n]
        rl = removed_log[n]
        note = []
        if rl['skylands']:
            note.append('skylands removed')
        if rl['cave']:
            note.append('cave biomes withheld: ' + ', '.join(sorted(rl['cave'])))
        if rl['other']:
            note.append('removed: ' + ', '.join(sorted(rl['other'])))
        empty = sorted(ps['structure'] for ps in ctx['per_structure']
                       if ps['proposal'] == n and n in executed and not per_struct_final.get(ps['structure']))
        if empty:
            note.append(f"{len(empty)} of {len(proposals[n]['structures'])} structures got no additions (no fitting biome)")
        if n in undecided:
            status = 'NEW (undecided, not applied)'
        elif n in held:
            status = 'HELD'
        elif n in modified:
            status = 'EXECUTED (modified)'
        else:
            status = 'executed'
        w(f"| {n} | {p['category']} -> {p['target_mod']} | {status} | {'; '.join(note)} |")
    w('')

    w('## Applied tag merges')
    w('')
    for t, m in tags_out.items():
        pl = 'proposal' if len(m['proposals']) == 1 else 'proposals'
        w(f"- **{t}** ({pl} {', '.join(map(str, m['proposals']))}): {', '.join(m['add'])}")
        if m['also_affects']:
            w(f"  - also affects (shares this tag): {', '.join(m['also_affects'])}")
    w('')
    nothing = [sid for n in sorted(variant_matched) for sid in [ps['structure'] for ps in ctx['per_structure'] if ps['proposal'] == n]
               if not per_struct_final.get(sid)]
    if nothing:
        w('Variant-matched structures that received NO additions (no fitting biome):')
        for sid in sorted(nothing):
            w(f'- {sid}')
        w('')

    # --- variant-matched summary
    w(f"## Variant-matched sets (proposals {', '.join(map(str, sorted(variant_matched)))})")
    w('')
    w('Each biome-variant structure only gets biomes resembling its own variant.')
    w('')
    w(f"- birch forest structures: {', '.join(sorted(BIRCH_LIKE))}  (birch-dominant trunks)")
    w(f"- flower forest structures: {', '.join(sorted(flower))}  (Terralith's own c:flower_forests, minus skylands)")
    w(f"- plain forest structures: {', '.join(sorted(GENERIC_FOREST_LIKE))}  (oak/birch/dark-oak temperate mix)")
    w(f"- sunflower plains structures: none. No Terralith biome has sunflowers except skylands_summer (excluded). "
      f"Original candidates rejected as not sunflower-plains-like: alpine_highlands, highlands, rocky_shrubland")
    for key, biomes in snowy_variant.items():
        w(f"- {key} structures: {', '.join(sorted(biomes)) or '(none)'}")
    assigned = set().union(*snowy_variant.values())
    w(f"- snowy biomes NOT assigned to any T&T variant (second review): {', '.join(sorted(snowy - assigned))}")
    w('')

    # --- mansion review
    if n_mansion is not None:
        p10 = proposals[n_mansion]
        w(f'## Proposal {n_mansion}: woodland mansion (second review)')
        w('')
        w(f"Applied: {', '.join(sorted(DARK_FOREST_LIKE))} (dark oak canopy trunks, sister of sakura_grove which Terralith already gives mansions).")
        w('')
        w(f"Remaining biomes from the original {len(p10['biomes'])}, for second review:")
        w('')
        for b in p10['biomes']:
            if b in DARK_FOREST_LIKE:
                continue
            tag = ' [SKYLANDS - removed]' if b in skylands else ''
            w(f"- {b}{tag}: {biome_info(b) or 'n/a'}")
        w('')

    # --- proposal 9 breakout
    w(f'## Proposal {n_end}: End structures breakout (HELD)')
    w('')
    with zipfile.ZipFile(A.MODS_DIR / 'Nullscape_1.19.3_v1.2.2.jar') as nz:
        null_b = sorted('nullscape:' + n.split('/')[-1][:-5] for n in nz.namelist()
                        if re.match(r'^data/nullscape/worldgen/biome/.+\.json$', n))
    end_biomes = sorted(set(R('minecraft:is_end')) | set(null_b))
    w(f"Proposal {n_end} as generated only touches 2 structures (philipsruins end_ruins, end_gate_fortress, via tag "
      f"`philipsruins:has_structure/end_ruins_biomes`, currently end_highlands + end_midlands). The End has "
      f"{len(end_biomes)} biomes ({len(null_b)} Nullscape + {len(end_biomes) - len(null_b)} vanilla), so here is the full "
      f"structure x biome matrix ('X' = generates there now, 'p{n_end}' = proposal {n_end} would add):")
    w('')
    end_structs = sorted(sid for sid, (pool, _, _) in pools.items() if pool & set(end_biomes))
    short = [b.split(':')[1] for b in end_biomes]
    w('| structure | tag | ' + ' | '.join(short) + ' |')
    w('|---|---|' + '---|' * len(short))
    p9_structs = {ps['structure']: set(ps['missing']) for ps in ctx['per_structure'] if ps['proposal'] == n_end}
    for sid in end_structs:
        pool, tags, _ = pools[sid]
        cells = []
        for b in end_biomes:
            if b in pool:
                cells.append('X')
            elif b in p9_structs.get(sid, ()):
                cells.append(f'p{n_end}')
            else:
                cells.append('')
        w(f"| {sid} | {', '.join(tags) or 'literal'} | " + ' | '.join(cells) + ' |')
    w('')

    # --- skylands
    w('## Skylands biomes: structures that currently generate in them')
    w('')
    w(f"Skylands biomes: {', '.join(sorted(skylands))}. Removed from every add list; nothing below is changed, only listed.")
    w('')
    for sid, (pool, tags, _) in sorted(pools.items()):
        hit = sorted(pool & skylands)
        if hit:
            w(f"- **{sid}** ({len(hit)}/{len(skylands)} skylands biomes) via {', '.join(tags) or 'literal list'}")
    w('')

    # --- cave marks
    w('## Cave biomes')
    w('')
    w('Cave-native = underground/stronghold structure step, or vanilla mineshaft/stronghold/ancient_city, or Terralith `underground/*` '
      '(those declare step surface_structures but are cave structures by design, so they are treated as native).')
    w('')
    w('### Already generating in cave biomes (non-cave-native)')
    w('')
    found = False
    for sid, (pool, tags, _) in sorted(pools.items()):
        hit = sorted(pool & cave)
        if hit and not cave_native(sid):
            found = True
            w(f"- [CAVE] **{sid}**: {', '.join(hit)} via {', '.join(tags) or 'literal list'}")
    if not found:
        w('- none')
    w('')
    w('### Cave biomes in the suggestions (withheld, not applied)')
    w('')
    any_cave = False
    for n in approved:
        c = removed_log[n]['cave']
        if c:
            any_cave = True
            w(f"- [CAVE] proposal {n} ({proposals[n]['category']} -> {proposals[n]['target_mod']}): {', '.join(sorted(c))}")
    if not any_cave:
        w('- none')
    w('')

    # --- variant marks
    w('## Variant structures vs broader biome definitions')
    w('')
    w(f"### In the suggestions (blanket across sibling variants, like proposal {num.get(TT_FOREST)}/{num.get(TT_PLAINS)})")
    w('')
    for n in approved:
        items = [ps for ps in ctx['per_structure'] if ps['proposal'] == n]
        by_adds = defaultdict(lambda: defaultdict(list))
        for ps in items:
            variant = frozenset(b for b in pools[ps['structure']][0] if b.startswith('minecraft:'))
            by_adds[frozenset(ps['missing'])][variant].append(ps['structure'])
        blanket = [(adds, vs) for adds, vs in by_adds.items() if len(vs) >= 2]
        if blanket and n not in variant_matched:
            w(f"- [VARIANT-BROAD] proposal {n} ({proposals[n]['category']} -> {proposals[n]['target_mod']}, "
              f"{'executed' if n not in held else 'held'}): "
              + '; '.join(f"{len(vs)} distinct vanilla variants receive the identical {len(adds)}-biome list -> "
                          + ' | '.join(f"{'/'.join(sorted(v)) or 'n/a'}: {', '.join(sorted(sts))}" for v, sts in vs.items())
                          for adds, vs in blanket))
    w('')
    w('### Already present (variant structure whose pool includes off-theme modded biomes)')
    w('')
    for sid, (pool, tags, _) in sorted(pools.items()):
        if sid in A.SKIP_STRUCTURES or cave_native(sid):
            continue
        vanilla = {b for b in pool if b.startswith('minecraft:') and any(b in bs for bs in cats.values())}
        theme = {c for c, bs in cats.items() if vanilla and len(vanilla & bs) / len(vanilla) >= 0.6}
        if not theme:
            continue
        off = sorted(b for b in modded(pool)
                     if any(b in bs for bs in cats.values()) and not any(b in cats[c] for c in theme)
                     and b.split(':')[0] != sid.split(':')[0])
        if off:
            w(f"- [VARIANT-BROAD] **{sid}** (theme: {', '.join(sorted(theme))}): also generates in {', '.join(off)}")
    w('')

    # --- raw original proposals
    w('## Original proposal lists (as first generated, before decisions)')
    w('')
    for n in sorted(approved + undecided):
        p = proposals[n]
        w(f"### {n}. {p['category']} ({', '.join(p['biome_mods'])}) -> {p['target_mod']}")
        w('')
        w(f"Add: {', '.join(p['biomes'])}")
        w('')
        w('Structures: ' + ', '.join(f"{s['structure']} (+{len(s['missing'])})" for s in p['structures']))
        w('')
    OUT_MD.write_text('\n'.join(L))
    print(f'{len(tags_out)} tag merges -> {OUT_MERGES.name}; report -> {OUT_MD.name}')
    if unapplied:
        print('literal-list structures skipped (need structure override):', unapplied)


if __name__ == '__main__':
    main()

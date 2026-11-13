#!/usr/bin/env python3
"""Phase manifest checker and resource filter for the Create: Power Grid port (dev-only).

feature_writeup/plan_power_grid.md section 1 ("Phase manifest", "Recipe gate"). Reads
development/backportedPowerGrid/phases.json and the upstream resources in development/backportedPowerGrid/src/main/resources.

Modes:
  python3 tools/pg_phase_check.py
      Report: per phase, the recipes it enables, and every problem (exit 1 on a problem).
  python3 tools/pg_phase_check.py --phase N --out DIR
      Build mode (run by the backportedPowerGrid Gradle build): checks the manifest, then writes the resources that
      ship at port phase N into DIR. Files of a later phase are left out; tag files lose their entries for unported
      ids. Exit 1 on a problem, which fails the build.
  python3 tools/pg_phase_check.py --fix-recipes
      One-off recipe JSON fixes for 1.19.2 (idempotent, rewrites src/main/resources in place): drop `category` and
      `show_notification`; ingredient `create:copper_sheet` -> tag `forge:plates/copper`; ingredient `create:cardboard`
      -> tag `forge:cardboard` (D13, H6); a `create:cardboard` byproduct (no such item on 0.5.1) is dropped.

Phase of a file:
  - blockstates/<id>.json, models/block/<id>.json, models/item/<id>.json (or models/item/<id>/...), loot_tables/blocks/<id>.json:
    the block or item <id>; data/powergrid/powergrid/wire_types/<id>.json: the item <id>.
  - recipes: the highest phase among the output, every powergrid ingredient and the recipe type (overridable in
    `recipes`). A recipe whose ingredient or type is in a later phase than its output is a problem unless `recipes`
    lists it (the override acknowledges that the output is uncraftable until that phase).
  - `resources` globs (the longest matching glob wins).
  - anything else: phase 0 (ships always: lang, textures, sounds, particles, models not named after an id).
Tags under data/*/tags/{blocks,items,entity_types,fluids}: entries naming an unported powergrid id are removed.
Phases are numbers: a sub-step of a phase is a decimal (P9a to P9d are 9.1 to 9.4, added 2026-11-13).

Client assets check (build mode, after the copy; added 2026-11-13 after the P3 to P5 client crash on the missing
particles/zap.json): every block, item and particle the code registers at this phase (read from the ported
collections in src/main/java: ModdedBlocks, ModdedItems, ModdedParticles, ModdedFluids when present) must have its client
assets in the output: a blockstate per block, an item model per item and per block with an item, a lang key, and per
particle type a particles/<name>.json (1.19.2's ParticleEngine fails the resource load without one) whose
textures exist. A missing asset is a problem (fails the build).
"""
import argparse
import fnmatch
import json
import os
import re
import shutil
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MOD = os.path.join(ROOT, 'development', 'backportedPowerGrid')
NS = 'powergrid'
NEVER = 99
ID_RE = re.compile(r'"#?powergrid:([a-z0-9_/.]+)"')
TAG_KINDS = {'blocks': 'blocks', 'items': 'items', 'entity_types': 'entities', 'fluids': 'fluids'}


def load_manifest(path):
    with open(path) as f:
        m = json.load(f)
    for key in ('blocks', 'items', 'entities', 'fluids', 'menus', 'recipe_types', 'recipes', 'resources'):
        m.setdefault(key, {})
    return m


def rel_files(res):
    out = []
    for d, _, fs in os.walk(res):
        for f in fs:
            out.append(os.path.relpath(os.path.join(d, f), res).replace(os.sep, '/'))
    return sorted(out)


class Manifest:
    def __init__(self, m):
        self.m = m
        # an item id: standalone item or a block's item
        self.item_phase = dict(m['blocks'])
        self.item_phase.update(m['items'])
        self.fluid_phase = dict(m['fluids'])

    def id_phase(self, ident):
        """Phase of a powergrid item, block or fluid id (None if it names none of them)."""
        for table in (self.item_phase, self.fluid_phase, self.m['entities']):
            if ident in table:
                return table[ident]
        return None

    def glob_phase(self, path):
        best = None
        for glob, phase in self.m['resources'].items():
            if fnmatch.fnmatchcase(path, glob) and (best is None or len(glob) > len(best[0])):
                best = (glob, phase)
        return best[1] if best else None

    def file_phase(self, path):
        g = self.glob_phase(path)
        if g is not None:
            return g
        parts = path.split('/')
        name = parts[-1].rsplit('.', 1)[0]
        if path.startswith(f'assets/{NS}/blockstates/'):
            return self.m['blocks'].get(name, 0)
        if path.startswith(f'assets/{NS}/models/block/') and len(parts) == 5:
            return self.m['blocks'].get(name, 0)
        if path.startswith(f'assets/{NS}/models/item/'):
            key = parts[4].rsplit('.', 1)[0]
            return self.item_phase.get(key, 0)
        if path.startswith(f'data/{NS}/loot_tables/blocks/') and len(parts) == 5:
            return self.m['blocks'].get(name, 0)
        if path.startswith(f'data/{NS}/powergrid/wire_types/'):
            return self.item_phase.get(name, 0)
        return 0


def named_ids(node, key=None):
    """powergrid ids a recipe names as items, fluids or block ids (not tags, not recipe types)."""
    if isinstance(node, dict):
        for k, v in node.items():
            if k in ('tag', 'type'):
                continue
            yield from named_ids(v, k)
    elif isinstance(node, list):
        for v in node:
            yield from named_ids(v, key)
    elif isinstance(node, str) and node.startswith(NS + ':') and key in ('item', 'id', 'fluid', 'block', 'result', 'output'):
        yield node.split(':', 1)[1]


def recipe_info(man, path, recipe):
    """(output phase, phase of the latest ingredient or type, computed phase, unknown powergrid ids)."""
    rtype = recipe.get('type', '')
    out_ids = []
    res = recipe.get('result') or recipe.get('results') or recipe.get('output')
    if isinstance(res, dict):
        res = [res]
    if isinstance(res, str):
        res = [{'item': res}]
    for r in res or []:
        item = r.get('item') or r.get('id') or r.get('fluid') or ''
        if item.startswith(NS + ':'):
            out_ids.append(item.split(':', 1)[1])
            break  # the first result is the product; the rest are byproducts
    unknown, late = [], []
    ing_phase = man.m['recipe_types'].get(rtype, 0) if rtype.startswith(NS + ':') else 0
    if ing_phase:
        late.append((ing_phase, rtype))
    if rtype.startswith(NS + ':') and rtype not in man.m['recipe_types']:
        unknown.append('type ' + rtype)
    for ident in sorted(set(named_ids(recipe))):
        p = man.id_phase(ident)
        if p is None:
            unknown.append(ident)
            continue
        if ident not in out_ids:
            ing_phase = max(ing_phase, p)
            late.append((p, ident))
    out_phase = max([man.id_phase(i) or 0 for i in out_ids], default=0)
    late = sorted(i for p, i in late if p > out_phase)
    return out_phase, ing_phase, max(out_phase, ing_phase), unknown, (late if out_ids else [])


def analyse(man, res):
    """Returns (file -> phase, recipe report list, problems)."""
    phases, report, problems = {}, [], []
    for path in rel_files(res):
        phase = man.file_phase(path)
        if path.startswith(f'data/{NS}/recipes/') and path.endswith('.json') and man.glob_phase(path) is None:
            key = path[len(f'data/{NS}/recipes/'):-5]
            with open(os.path.join(res, path)) as f:
                recipe = json.load(f)
            out_p, ing_p, phase, unknown, late = recipe_info(man, path, recipe)
            for u in unknown:
                problems.append(f'{path}: unknown powergrid id {u} (add it to phases.json)')
            if key in man.m['recipes']:
                phase = man.m['recipes'][key]
                if phase < max(out_p, ing_p):
                    problems.append(f'{path}: override phase {phase} is below its output or ingredients ({max(out_p, ing_p)})')
            elif late:
                problems.append(f'{path}: output is phase {out_p} but {", ".join(late)} is phase {ing_p}; '
                                f'list it under "recipes" in phases.json to accept that')
            report.append((phase, key))
        if path.startswith('data/') and '/tags/' in path and path.endswith('.json'):
            kind = TAG_KINDS.get(path.split('/')[3]) if path.count('/') >= 4 else None
            if kind:
                with open(os.path.join(res, path)) as f:
                    values = json.load(f).get('values', [])
                table = man.item_phase if kind == 'items' else man.m[kind]
                for v in values:
                    ident = v if isinstance(v, str) else v.get('id', '')
                    if ident.startswith(NS + ':') and ident.split(':', 1)[1] not in table:
                        problems.append(f'{path}: unknown powergrid id {ident} (add it to phases.json {kind})')
        phases[path] = phase
    for kind in ('blocks', 'items', 'entities', 'fluids', 'menus', 'recipe_types', 'recipes', 'resources'):
        for k, v in man.m[kind].items():
            if isinstance(v, bool) or not isinstance(v, (int, float)) or v < 0:
                problems.append(f'phases.json {kind}.{k}: phase must be a non-negative number')
    for k in man.m['items']:
        if k in man.m['blocks']:
            problems.append(f'phases.json: {k} is in both blocks and items (a block item follows its block)')
    return phases, report, problems


def filter_tag(man, path, data, phase):
    kind = TAG_KINDS.get(path.split('/')[3]) if path.count('/') >= 4 else None
    if kind is None or 'values' not in data:
        return data, 0
    table = man.item_phase if kind == 'items' else man.m[kind]
    kept, dropped = [], 0
    for v in data['values']:
        ident = v if isinstance(v, str) else v.get('id', '')
        if ident.startswith(NS + ':'):
            p = table.get(ident.split(':', 1)[1])
            if p is not None and p > phase:
                dropped += 1
                continue
        kept.append(v)
    out = dict(data)
    out['values'] = kept
    return out, dropped


JAVA = os.path.join(MOD, 'src', 'main', 'java', 'org', 'patryk3211', 'powergrid', 'collections')
# blocks registered without lang on purpose (never shown by name): none so far
NO_LANG_BLOCKS = set()


def registered_ids():
    """Ids the ported collections register (the code is the truth for what loads; phases.json gates the code)."""
    blocks, items, particles, fluids = {}, set(), set(), set()
    p = os.path.join(JAVA, 'ModdedBlocks.java')
    if os.path.isfile(p):
        src = open(p).read()
        for m in re.finditer(r'REGISTRATE\.block\(id\("([a-z0-9_]+)"\)', src):
            end = src.find('.register();', m.end())
            chunk = src[m.end():end if end >= 0 else len(src)]
            blocks[m.group(1)] = ('simpleItem()' in chunk) or ('.item(' in chunk)
    p = os.path.join(JAVA, 'ModdedItems.java')
    if os.path.isfile(p):
        items = set(re.findall(r'REGISTRATE\.item\(id\("([a-z0-9_]+)"\)', open(p).read()))
    p = os.path.join(JAVA, 'ModdedParticles.java')
    if os.path.isfile(p):
        particles = set(re.findall(r'\bregister\("([a-z0-9_]+)"', open(p).read()))
    p = os.path.join(JAVA, 'ModdedFluids.java')
    if os.path.isfile(p):
        # Since P9c: REGISTRATE.fluid(PortPhase.require("fluid", "<id>"), asResource("<still>"), asResource("<flow>"), ...)
        # registers the fluid, its liquid block <id> and (with .bucket()) the item <id>_bucket.
        src = open(p).read()
        for m in re.finditer(r'REGISTRATE\.fluid\(PortPhase\.require\("fluid", "([a-z0-9_]+)"\),\s*'
                             r'PowerGrid\.asResource\("([a-z0-9_/]+)"\),\s*PowerGrid\.asResource\("([a-z0-9_/]+)"\)', src):
            end = src.find('.register();', m.end())
            chunk = src[m.end():end if end >= 0 else len(src)]
            fluids.add((m.group(1), m.group(2), m.group(3), '.bucket()' in chunk))
    return blocks, items, particles, fluids


def check_assets(out):
    problems = []
    blocks, items, particles, fluids = registered_ids()
    a = os.path.join(out, 'assets', NS)
    lang_path = os.path.join(a, 'lang', 'en_us.json')
    lang = json.load(open(lang_path)) if os.path.isfile(lang_path) else {}
    if not lang:
        problems.append('assets: no assets/powergrid/lang/en_us.json in the output')

    def has(rel):
        return os.path.isfile(os.path.join(a, rel))

    def item_model(i):
        return has(f'models/item/{i}.json') or os.path.isdir(os.path.join(a, 'models', 'item', i))

    for b, with_item in sorted(blocks.items()):
        if not has(f'blockstates/{b}.json'):
            problems.append(f'assets: block {b} has no blockstates/{b}.json')
        if with_item and not item_model(b):
            problems.append(f'assets: block {b} has an item but no models/item/{b}.json')
        if b not in NO_LANG_BLOCKS and f'block.{NS}.{b}' not in lang:
            problems.append(f'assets: block {b} has no lang key block.{NS}.{b}')
    for i in sorted(items):
        if not item_model(i):
            problems.append(f'assets: item {i} has no models/item/{i}.json')
        if f'item.{NS}.{i}' not in lang:
            problems.append(f'assets: item {i} has no lang key item.{NS}.{i}')
    for pt in sorted(particles):
        rel = f'particles/{pt}.json'
        if not has(rel):
            problems.append(f'assets: particle {pt} has no {rel} (1.19.2 fails the client resource load)')
            continue
        desc = json.load(open(os.path.join(a, rel)))
        for t in desc.get('textures', []):
            ns, path = t.split(':', 1) if ':' in t else ('minecraft', t)
            if ns == NS and not has(f'textures/particle/{path}.png'):
                problems.append(f'assets: particle {pt} texture {t} has no textures/particle/{path}.png')
    for f, still, flow, bucket in sorted(fluids):
        for tex in (still, flow):
            if not has(f'textures/{tex}.png'):
                problems.append(f'assets: fluid {f} texture {NS}:{tex} has no textures/{tex}.png')
        if f'fluid.{NS}.{f}' not in lang:
            problems.append(f'assets: fluid {f} has no lang key fluid.{NS}.{f}')
        if not has(f'blockstates/{f}.json'):
            problems.append(f'assets: fluid block {f} has no blockstates/{f}.json')
        if f'block.{NS}.{f}' not in lang:
            problems.append(f'assets: fluid block {f} has no lang key block.{NS}.{f}')
        if bucket:
            if not item_model(f + '_bucket'):
                problems.append(f'assets: bucket {f}_bucket has no models/item/{f}_bucket.json')
            if f'item.{NS}.{f}_bucket' not in lang:
                problems.append(f'assets: bucket {f}_bucket has no lang key item.{NS}.{f}_bucket')
    if not problems:
        print(f'pg_phase_check: assets ok for {len(blocks)} blocks, {len(items)} items, {len(particles)} particle types, '
              f'{len(fluids)} fluids (textures, block, bucket)')
    return problems


def build(man, res, out, phase):
    phases, _, problems = analyse(man, res)
    if problems:
        return problems
    if os.path.isdir(out):
        shutil.rmtree(out)
    shipped = left = rewritten = 0
    for path, p in phases.items():
        if p > phase:
            left += 1
            continue
        src = os.path.join(res, path)
        dst = os.path.join(out, path)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        if path.startswith('data/') and '/tags/' in path and path.endswith('.json'):
            with open(src) as f:
                data = json.load(f)
            data, dropped = filter_tag(man, path, data, phase)
            if dropped:
                rewritten += 1
                with open(dst, 'w') as f:
                    json.dump(data, f, indent=2)
                    f.write('\n')
                shipped += 1
                continue
        shutil.copyfile(src, dst)
        shipped += 1
    print(f'pg_phase_check: phase {phase}: {shipped} files ship ({rewritten} tags filtered), {left} left out')
    return check_assets(out)


def fix_recipes(res):
    changed = 0
    rdir = os.path.join(res, 'data', NS, 'recipes')

    def fix(node, in_results):
        if isinstance(node, dict):
            if not in_results and node.get('item') == 'create:copper_sheet' and len(node) == 1:
                return {'tag': 'forge:plates/copper'}
            if not in_results and node.get('item') == 'create:cardboard' and len(node) == 1:
                return {'tag': 'forge:cardboard'}
            return {k: fix(v, in_results or k in ('result', 'results')) for k, v in node.items()
                    if k not in ('category', 'show_notification')}
        if isinstance(node, list):
            return [fix(v, in_results) for v in node
                    if not (in_results and isinstance(v, dict) and v.get('item') == 'create:cardboard')]
        return node

    for path in rel_files(rdir):
        full = os.path.join(rdir, path)
        with open(full) as f:
            data = json.load(f)
        new = fix(data, False)
        if new != data:
            with open(full, 'w') as f:
                json.dump(new, f, indent=2)
                f.write('\n')
            changed += 1
    print(f'pg_phase_check: fixed {changed} recipe files')


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--manifest', default=os.path.join(MOD, 'phases.json'))
    ap.add_argument('--resources', default=os.path.join(MOD, 'src', 'main', 'resources'))
    ap.add_argument('--phase', type=float)
    ap.add_argument('--out')
    ap.add_argument('--fix-recipes', action='store_true')
    a = ap.parse_args()
    if a.fix_recipes:
        fix_recipes(a.resources)
        return 0
    man = Manifest(load_manifest(a.manifest))
    if a.out is not None:
        if a.phase is None:
            ap.error('--out needs --phase')
        problems = build(man, a.resources, a.out, a.phase)
    else:
        phases, report, problems = analyse(man, a.resources)
        by_phase = {}
        for p, key in report:
            by_phase.setdefault(p, []).append(key)
        for p in sorted(by_phase):
            print(f'phase {p}: {len(by_phase[p])} recipes')
            for key in sorted(by_phase[p]):
                print(f'  {key}')
        counts = {}
        for p in phases.values():
            counts[p] = counts.get(p, 0) + 1
        print('files per phase: ' + ', '.join(f'{p}: {n}' for p, n in sorted(counts.items())))
    for line in problems:
        print('PROBLEM ' + line, file=sys.stderr)
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())

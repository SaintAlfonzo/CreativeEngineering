#!/usr/bin/env python3
"""Output root of a generator (creativedelight framework #7, creative_delight.md 28).

A generator that writes member rows (cans, food types, rules for a Creative Delight member) reads its output root from
its own entry in tools/generators.json instead of hard-coding the pack folder:

    "outRoot": "kubejs/data/creativedelight/kubejs/<modid>"

Missing means the generator's own default (the pack folder, unchanged). Allowed roots are the CD trees the jar build
copies (creativeDelight/build.gradle scriptTrees + dataTrees); anything else is refused, so a typo never scatters
files. The generator's "outputs" stay instance-relative paths (GeneratorRunner hashes them from the instance root),
so they name the full path under outRoot. /cdevgen itself is unchanged: the runner ignores this field.

Files written into a CD tree start with header(): the jar ships them like hand-written scripts, and the header tells a
reader not to edit them by hand. A run after a jar build is not in that jar; build_deploy's
freshness check covers the trees and refuses the stale jar.

    from gen_out import out_root, header
    root = out_root('can_types', INSTANCE / 'kubejs/generation_scripts/food')

Member scripts (framework #7, owner 2026-11-01): a generator whose data the game reads writes it as one `.js` per
member (item namespace) under its outRoot, `<outRoot>/<namespace>/<name>.js`, headed `// requires: <namespace>` and
calling `global.CD.genPut(name, namespace, data)` (cd_core.js). Readers take the merged data with
`global.CD.genMerge(name)`; no reader opens a file by instance path. The JSON in kubejs/generation_scripts/food stays the
generators' own record (tools read it; food_types.json and can_servings.json also take hand edits), so a hand edit is
followed by a generator run, and each generator's --check reports member scripts that no longer match.

    from gen_out import split_by_namespace, write_member_scripts
    changed, stale = write_member_scripts('placeable_catalog', 'placeable', parts, DEFAULT_ROOT, 'placeable tags')

Manifest entries for hand-run scripts that need an outRoot (gen_can_types.py) sit in the top-level "handRun" list,
which /cdevgen does not read.

    python3 tools/gen_out.py                 list every generator's root
    python3 tools/gen_out.py --check         exit 1 when an outRoot is not allowed or an output lies outside it
"""
import argparse
import json
import sys
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
MANIFEST = INSTANCE / 'tools/generators.json'

# The trees creativeDelight/build.gradle copies into the jar (scriptTrees + dataTrees).
CD_TREES = [
    'kubejs/data/creativedelight/kubejs',
    'kubejs/assets/creativedelight/kubejs',
    'kubejs/data/creativedelight/worldgen',
    'kubejs/data/creativedelight/cup_conversion',
    'development/creativeDelight/src/main/resources/kubejs_startup',   # the jar's startup scripts (src, not copied)
]
DATA_ROOT = 'kubejs/data/creativedelight/kubejs'
STARTUP_ROOT = 'development/creativeDelight/src/main/resources/kubejs_startup'
STARTUP_FOLDER = {'creativeengineering': 'creativedelight'}
# Namespaces that are always loaded: their member scripts carry no requires header.
NO_REQUIRES = ('minecraft', 'creativeengineering', 'creativedelight', 'forge')
# Item namespaces whose jar has a different mod id. `// requires:` checks mod ids, so a header naming the namespace
# makes KubeJS skip the file (guard_check reports it as 'requires names no loaded jar').
NS_MOD_ID = {'cozys-improved-cats': 'cozys_improved_cats'}


def requires_mod(ns):
    """Mod id for a `// requires:` header of item namespace ns."""
    return NS_MOD_ID.get(ns, ns)
# Member scripts run before every hand-written CD script (cd_core.js is 1000, cd_food.js 999).
MEMBER_PRIORITY = 900


def _entries():
    m = json.loads(MANIFEST.read_text(encoding='utf-8'))
    return m['generators'] + [dict(g, handRun=True) for g in m.get('handRun', [])]


def _entry(gen_id):
    for g in _entries():
        if g['id'] == gen_id:
            return g
    raise SystemExit(f'gen_out: no generator {gen_id!r} in tools/{MANIFEST.name}')


def allowed(rel):
    """True when rel (instance-relative, '/' separated) is a CD tree or a folder inside one."""
    rel = rel.strip('/')
    return any(rel == t or rel.startswith(t + '/') for t in CD_TREES) and '..' not in rel.split('/')


def out_root(gen_id, default):
    """The generator's output folder: its manifest outRoot (made absolute), else default."""
    rel = _entry(gen_id).get('outRoot')
    if rel is None:
        return Path(default)
    if not allowed(rel):
        raise SystemExit(f'gen_out: {gen_id}.outRoot {rel!r} is not inside a CD tree ({", ".join(CD_TREES)})')
    return INSTANCE / rel.strip('/')


def header(gen_id, comment='//'):
    """First line of a generated file in a CD tree. JSON has no comments: write a "_generated" key instead."""
    g = _entry(gen_id)
    how = 'run by hand' if g.get('handRun') else f'/cdevgen run {gen_id}'
    return f'{comment} Generated by {g["script"]} ({how}); do not edit, rerun the generator.\n'


def problems():
    """An outRoot outside the CD trees, or an output inside a CD tree but not under the entry's outRoot (a generator
    may also write pack files: item_dict.js, its JSON record, a writeup)."""
    out = []
    for g in _entries():
        rel = g.get('outRoot')
        if rel is not None and not allowed(rel):
            out.append(f'{g["id"]}: outRoot {rel!r} is not inside a CD tree')
            continue
        root = rel.strip('/') if rel is not None else None
        for o in g.get('outputs', []):
            o = o.strip('/')
            if not allowed(o):
                continue
            if root is None:
                out.append(f'{g["id"]}: output {o!r} is in a CD tree but the entry has no outRoot')
            elif not (o == root or o.startswith(root + '/')):
                out.append(f'{g["id"]}: output {o!r} is outside outRoot {rel!r}')
    return out


# ---------------------------------------------------------------- member scripts

def namespace_of(key, key_val):
    """Item namespace of an item_dict key; tags, missing keys and empty values go to creativeengineering (always on)."""
    v = key_val.get(key)
    if not v or v.startswith('#') or ':' not in v:
        return 'creativeengineering'
    return v.split(':', 1)[0]


def split_lists(lists, key_val, all_names_in=None):
    """{name: [item_dict keys]} -> {namespace: {name: [keys]}}, order kept. all_names_in: a namespace whose part lists
    every name (empty lists too), so a reader still sees each name when only that part loads."""
    parts = {}
    if all_names_in:
        parts[all_names_in] = {n: [] for n in lists}
    for n, keys in lists.items():
        for k in keys:
            parts.setdefault(namespace_of(k, key_val), {}).setdefault(n, []).append(k)
    return parts


def member_text(gen_id, name, ns, data, what):
    lines = [header(gen_id).rstrip('\n'), f'// priority: {MEMBER_PRIORITY}']
    if ns not in NO_REQUIRES:
        lines.append(f'// requires: {requires_mod(ns)}')
    lines.append(f'// {what}, {ns} part. Readers merge every part with global.CD.genMerge(\'{name}\') (creative_delight.md 28.2).')
    body = json.dumps(data, indent=1, ensure_ascii=False)
    lines.append(f"if (global.CD) global.CD.genPut('{name}', '{ns}', {body})")
    return '\n'.join(lines) + '\n'


def write_member_scripts(gen_id, name, parts, default_root, what, check=False):
    """parts: namespace -> JSON data. Writes <root>/<ns>/<name>.js for each part and deletes this generator's
    <name>.js in folders with no part. Returns (changed or new paths, stale paths); writes nothing when check."""
    root = out_root(gen_id, default_root)
    mark = header(gen_id).split(';')[0]
    # CD's own startup folder is creativedelight/ (plan_cb_polish.md 4), also for its creativeengineering: items
    folder = (lambda ns: STARTUP_FOLDER.get(ns, ns)) if root == INSTANCE / STARTUP_ROOT else (lambda ns: ns)
    want = {root / folder(ns) / f'{name}.js': member_text(gen_id, name, ns, parts[ns], what) for ns in sorted(parts)}
    changed = [p for p, t in want.items() if not p.exists() or p.read_text(encoding='utf-8') != t]
    stale = [p for p in sorted(root.glob(f'*/{name}.js'))
             if p not in want and p.read_text(encoding='utf-8').startswith(mark)]
    if not check:
        for p in changed:
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_text(want[p], encoding='utf-8')
        for p in stale:
            p.unlink()
    return changed, stale


def report_member_scripts(name, changed, stale, check):
    """One line for the generator's output; True when something differs."""
    if not changed and not stale:
        print(f'{name}: member scripts up to date')
        return False
    verb = 'would change' if check else 'wrote'
    rel = lambda p: str(p.relative_to(INSTANCE)) if p.is_relative_to(INSTANCE) else str(p)
    print(f'{name}: {verb} {len(changed)} member script(s)' + (f', {"would delete" if check else "deleted"} {len(stale)} stale' if stale else ''))
    for p in changed + stale:
        print('  ' + rel(p))
    return True


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--check', action='store_true', help='exit 1 on a bad outRoot or an output outside it')
    args = ap.parse_args()
    if args.check:
        bad = problems()
        for line in bad:
            print('! ' + line)
        print(f'gen_out: {len(bad)} problem(s)')
        return 1 if bad else 0
    for g in _entries():
        print(f'{g["id"]:28} {g.get("outRoot", "(default: pack folder)")}')
    return 0


if __name__ == '__main__':
    sys.exit(main())

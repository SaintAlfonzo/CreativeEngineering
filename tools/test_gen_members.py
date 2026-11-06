#!/usr/bin/env python3
"""Offline check of the generator member scripts (creativedelight framework #7, creative_delight.md 28.2, CDT-14).

Runs every generated <mod>/{food_types,placeable,mortar_catalog}.js (data/creativedelight/kubejs) and kubejs_startup/<mod>/
{can_types,can_servings}.js through CD's cd_core.js and the five readers in the pack's own Rhino fork
(tools/gen_members_test/RhinoRun.java), then checks against the generators' JSON records:
  1. global.CD.genMerge(name) holds exactly the record (same entries; list order inside a type may differ)
  2. the readers add the same tags, create and modify the same cans and make the same mortar crafts the record gives
Every mod counts as loaded (Platform.isLoaded stub), so the requires headers do not drop anything here.
    python3 tools/test_gen_members.py
Needs the ATLauncher library folder (Rhino's gson). No game needed.
"""
import glob, json, os, subprocess, sys, tempfile
from pathlib import Path
ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import gen_out, pack_paths  # noqa: E402

GEN = ROOT / 'kubejs/generation_scripts/food'
DATA = ROOT / gen_out.DATA_ROOT
START = ROOT / gen_out.STARTUP_ROOT
CD = 'kubejs/data/creativedelight/kubejs/creativeengineering/'


def rel(p):
    return str(Path(p).relative_to(ROOT))


def run():
    lib = Path.home() / '.local/share/atlauncher/libraries'
    rhino = sorted(glob.glob(str(ROOT / 'mods/rhino-forge-*.jar')))[0]
    gson = lib / 'com/google/code/gson/gson/2.8.9/gson-2.8.9.jar'
    cp = f'{rhino}:{gson}'
    t = tempfile.mkdtemp()
    subprocess.run(['javac', '-proc:none', '-cp', cp, '-d', t, str(ROOT / 'tools/gen_members_test/RhinoRun.java')], check=True)
    members = sorted(glob.glob(str(START / '*/can_types.js')) + glob.glob(str(START / '*/can_servings.js'))
                     + glob.glob(str(DATA / '*/food_types.js')) + glob.glob(str(DATA / '*/placeable.js'))
                     + glob.glob(str(DATA / '*/mortar_catalog.js')))
    files = (['development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js', gen_out.STARTUP_ROOT + '/creativedelight/cd_core.js']
             + [rel(p) for p in members]
             + [gen_out.STARTUP_ROOT + '/creativeengineering/cans.js', CD + 'food_type_tags.js', CD + 'placeable_tags.js',
                CD + 'can_tags.js', CD + 'mortar.js'])
    out = subprocess.run(['java', '-cp', f'{t}:{cp}', 'RhinoRun', str(ROOT)] + files, capture_output=True, text=True)
    if out.returncode != 0 or not out.stdout.strip():
        sys.exit('RhinoRun failed: ' + out.stdout[-2000:] + out.stderr[-2000:])
    return len(members), json.loads(out.stdout)


def num(x):
    """Rhino and Python print some numbers differently (5 vs 5.0): compare through a JSON round trip of floats."""
    return json.loads(json.dumps(x), parse_int=float)


def main():
    n_members, r = run()
    if 'loadError' in r:
        sys.exit('load error: ' + r['loadError'])
    bad = []
    _, kv, _ = pack_paths.parse_item_dict()
    item = lambda k: kv.get(k) if kv.get(k) and not kv[k].startswith('#') else None
    m = r['merged']
    cans = json.loads((GEN / 'can_types.json').read_text())['cans']
    serv = json.loads((GEN / 'can_servings.json').read_text())['servings']
    types = json.loads((GEN / 'food_types.json').read_text())
    place = {k: v for k, v in json.loads((GEN / 'placeable.json').read_text()).items() if k != '_readme'}
    mortar = json.loads((GEN / 'mortar_catalog.json').read_text())['recipes']

    # 1. merged data = record
    if num(m['can_types'].get('cans')) != num(cans):
        bad.append('can_types differs from can_types.json')
    if num(m['can_servings'].get('servings')) != num(serv):
        bad.append('can_servings differs from can_servings.json')
    ft = m['food_types']
    if set(ft.get('types', {})) != set(types['types']) or any(sorted(ft['types'][t]) != sorted(v) for t, v in types['types'].items()):
        bad.append('food_types.types differs from food_types.json')
    if ft.get('canned') != types['canned']:
        bad.append('food_types.canned differs from food_types.json')
    if {k: sorted(v) for k, v in m['placeable'].items()} != {k: sorted(v) for k, v in place.items()}:
        bad.append('placeable differs from placeable.json')
    if num(sorted(m['mortar_catalog'].get('recipes', []), key=lambda x: x['id'])) != num(sorted(mortar, key=lambda x: x['id'])):
        bad.append('mortar_catalog differs from mortar_catalog.json')

    # 2. what the readers do = what the record gives
    want = {}
    def add(tag, ids):
        want.setdefault(tag, set()).update(i for i in ids if i)
    for t, keys in types['types'].items():
        add(kv['t_food_' + t.replace('/', '_')], [item(k) for k in keys])
    add(kv['t_canned_source'], [item(k) for srcs in types['canned'].values() for k in srcs])
    names = {'placeable': 't_placeable', 'placeable/sneak': 't_placeable_sneak', 'placeable_on_plate': 't_placeable_on_plate',
             'placeable_on_small_plate': 't_placeable_on_small_plate', 'placeable_on_coaster': 't_placeable_on_coaster'}
    for name, key in names.items():
        add(kv[key], [item(k) for k in place.get(name, [])])
    add(kv['t_bnc_placeable_on_coaster'], [kv['t_placeable_on_coaster']])
    for k, n in serv.items():
        if item(k):
            add(kv['t_can_servings'] + '/' + str(n), [item(k)])
    got = {t: set(v) for t, v in r['tags'].items()}
    for t in sorted(set(want) | set(got)):
        if want.get(t, set()) != got.get(t, set()):
            bad.append(f'tag {t}: {len(got.get(t, ()))} items, the record gives {len(want.get(t, ()))}')
    created = {cid: c for cid, c in r['created'].items()}
    exp_new = {kv[k]: c for k, c in cans.items() if not c.get('existing') and kv.get(k)}
    if set(created) != set(exp_new):
        bad.append(f'cans created {len(created)}, the record gives {len(exp_new)}')
    for cid, c in exp_new.items():
        g = created.get(cid, {})
        if g.get('name') != c['name'] or (c.get('food') is not False and num([g.get('food', {}).get('hunger'), g.get('food', {}).get('modifier')]) != num([c['hunger'], c['modifier']])):
            bad.append(f'can {cid}: {g} vs record {c["name"]} {c.get("hunger")} {c.get("modifier")}')
            break
    exp_mod = sorted(kv[k] for k, c in cans.items() if c.get('existing') and kv.get(k))
    if sorted(r['modified']) != exp_mod:
        bad.append(f'cans modified {len(r["modified"])}, the record gives {len(exp_mod)}')
    if sorted(x['id'] for x in r['mortar']) != sorted(x['id'] for x in mortar):
        bad.append(f'mortar crafts {len(r["mortar"])}, the record gives {len(mortar)}')
    errors = [x for x in r['log'] if x.startswith('ERROR')]
    bad += errors

    print(f'{n_members} member scripts loaded; {len(created)} cans created, {len(r["modified"])} modified, '
          f'{sum(len(v) for v in got.values())} tag entries in {len(got)} tags, {len(r["mortar"])} mortar crafts')
    for w in [x for x in r['log'] if x.startswith('WARN')][:10]:
        print('  ' + w[:300])
    for b in bad:
        print('! ' + b)
    print('gen member tests ' + ('FAILED' if bad else 'PASSED'))
    return 1 if bad else 0


if __name__ == '__main__':
    sys.exit(main())

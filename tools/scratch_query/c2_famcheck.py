#!/usr/bin/env python3
"""Offline checks of the generated food catalogs for r1113 chunk 2 (SQ-LGM-13, -14, SQ-BBQ2-07). Reads the instance's
kubejs/generation_scripts/food/*.json (what the last /cdevgen wrote) and kubejs/exported/items_by_mod/.

    c2_famcheck.py families ID [ID ...]   family paths holding each item id (food_families.json)
    c2_famcheck.py lmplaceable            every displaydelight:lm_* block's food: is it in a placeable list
    c2_famcheck.py skewers                cooked skewers (item ids ending in _skewer / _stick, not raw_) and their
                                          families; can_types entries whose name or sources mention a skewer
    c2_famcheck.py softfluids JAR         SQ-PJL-05: soft fluid container capacities in the Vintage member nested in JAR
Prints the generated files' modification times first, so a stale catalog is visible.
"""
import json
import os
import re
import sys
import time

INST = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
FOOD = os.path.join(INST, 'kubejs', 'generation_scripts', 'food')
ITEMS = os.path.join(INST, 'kubejs', 'exported', 'items_by_mod')


def load(name):
    p = os.path.join(FOOD, name)
    print('file %s %s' % (name, time.strftime('%Y-%m-%d %H:%M', time.localtime(os.path.getmtime(p)))))
    with open(p) as f:
        return json.load(f)


def families_of(fam, item):
    return [k for k, v in fam.items() if isinstance(v, list) and item in v]


def all_items():
    out = []
    for fn in os.listdir(ITEMS):
        if fn.endswith('.json'):
            with open(os.path.join(ITEMS, fn)) as f:
                d = json.load(f)
            out.extend(d.get('items', []) if isinstance(d, dict) else d)
    return out


def main(a):
    if not a:
        print(__doc__)
        return 2
    if a[0] == 'families':
        fam = load('food_families.json')['families']
        for i in a[1:]:
            print('%s: %s' % (i, ', '.join(families_of(fam, i)) or 'NONE'))
    elif a[0] == 'lmplaceable':
        pl = load('placeable.json')
        keys = {k: set(v) for k, v in pl.items() if isinstance(v, list) and not k.startswith('_')}
        lm = sorted(i for i in all_items() if i.startswith('displaydelight:lm_'))
        miss = 0
        for i in lm:
            base = i.split(':', 1)[1][3:]
            tags = [k for k, v in keys.items() if base in v or ('largemeals_' + base) in v]
            if not tags:
                miss += 1
            print('%s: %s' % (i, ', '.join(tags) or 'NOT PLACEABLE'))
        print('lm blocks %d, not placeable %d' % (len(lm), miss))
    elif a[0] == 'skewers':
        fam = load('food_families.json')['families']
        cans = load('can_types.json')['cans']
        sk = sorted(i for i in all_items() if re.search(r'(skewer|barbecue_stick)$', i) and ':raw_' not in i
                    and 'incomplete' not in i)
        n = 0
        for i in sk:
            f = families_of(fam, i)
            n += 'skewer' in f
            print('%s: %s' % (i, ', '.join(f) or 'NONE'))
        print('cooked skewers %d, in family skewer %d' % (len(sk), n))
        hits = [k for k, v in cans.items() if re.search('skewer|barbecue_stick', k + json.dumps(v.get('sources', [])))]
        print('skewer cans %d %s' % (len(hits), ' '.join(hits)))
    elif a[0] == 'softfluids':
        # SQ-PJL-05: capacity of every data/vintagedelight/moonlight/soft_fluids/*.json in the Vintage member nested in
        # the Creative Delight jar a[1] (e.g. srv/mods/creativedelight-1.1.0.jar)
        import io
        import zipfile
        outer = zipfile.ZipFile(a[1])
        inner = [n for n in outer.namelist() if re.match(r'META-INF/jarjar/backportedVintage-.*\.jar$', n)]
        if not inner:
            print('no nested Vintage jar in ' + a[1])
            return 1
        z = zipfile.ZipFile(io.BytesIO(outer.read(inner[0])))
        caps = {}
        for n in z.namelist():
            if re.match(r'data/vintagedelight/moonlight/soft_fluids/.*\.json$', n):
                d = json.loads(z.read(n))
                for c in d.get('containers', []) if isinstance(d, dict) else []:
                    caps.setdefault(str(c.get('capacity')), []).append(n.rsplit('/', 1)[1] + ':' + str(c.get('empty', '')))
                if isinstance(d, dict) and 'containers' not in d:
                    caps.setdefault('no-containers', []).append(n.rsplit('/', 1)[1])
        print('nested %s' % inner[0])
        for k, v in sorted(caps.items()):
            print('capacity %s: %d container(s) %s' % (k, len(v), ' '.join(v[:12])))
    else:
        print('unknown mode ' + a[0])
        return 2
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))

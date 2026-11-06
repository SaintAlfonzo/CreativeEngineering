#!/usr/bin/env python3
"""Foods with a block model of their own on Brewin' and Chewin' coasters -> the Creative Brewing jar's
assets/creativebrewing/brewinandchewin/coaster/ (was the KubeJS resource pack's assets/creativeengineering/ up to
2026-11-04, plan_cb_publish.md M3; a CB rebuild ships them, and the port skips a file whose item is not registered).

The Brewin' port's coaster draws an item with its own small model when a coaster JSON names one
(assets/<ns>/brewinandchewin/coaster/*.json, any namespace), and with [coaster] taggedItemsOnly on it takes only
the items in #brewinandchewin:placeable_on_coaster, which the port fills from its own 96 files. An item with no
coaster JSON would lie flat as its item sprite, so only items with a real block model get a file here:

  * Display Delight: every food the port of 1.7.0 registers a display block for (linked by block name,
    tools/display_delight_jar.py), pointing at that block's default-state model (facing north, no wooden leg).
    Which block, best first: the food set down on its own (placed), then on a Small Food Plate, then on a Food Plate.
  * Other mods' placed drinks and jars (OWN_BLOCKS): the edible block items whose block class is listed there
    (Vinery's wine bottles, Vintage Delight's mason jars), pointing at their own block's default-state model.
  * BORROW: a food whose item sprite is the same picture as a modelled food's (tools: feature_writeup/
    coaster_model_candidates.md) takes that food's coaster models, optionally with the enchantment glint.

Block models are full-size blocks (a DD plate is 14 px across), so each entry gets a "scale" that brings its widest
side down to TARGET_PX (about the size of the port's own bowls) and its height to MAX_HEIGHT_PX.

placeable_catalog.py reads these files too: their items join #creativeengineering:placeable_on_coaster, and
data/creativedelight/kubejs/creativeengineering/placeable_tags.js puts that tag into
#brewinandchewin:placeable_on_coaster. So run this first. Items not in the dumps (kubejs/exported/food/placeable.json,
items_by_mod/) are left out until /dumpall has seen them.

    python3 tools/gen_coaster_models.py           write the files
    python3 tools/gen_coaster_models.py --check   exit 1 if any file is missing, stale or left over
"""
import argparse
import json
import sys
import zipfile

import lang_index
import placeable_catalog as pc

OUT = lang_index.INSTANCE / 'development/creativeBrewing/src/main/resources/assets/creativebrewing/brewinandchewin/coaster'
# Widest side of a block model on a coaster, in pixels; the port's bowls are 12, its tankards 7.
TARGET_PX = 10.0
# Tallest a model may stand; Vinery's medium bottles are 18.
MAX_HEIGHT_PX = 16.0
KIND_ORDER = ('placed', 'small_plate', 'plate')

# Block class (simple name) of an edible block item -> the jars its art is read from, first hit wins. Vinery's art
# lives in the donor jars the port reads at runtime (vinery_upstream/, mod_sides.md), under the same model ids.
OWN_BLOCKS = {
    'WineBottleBlock': ('vinery_upstream/letsdo-vinery-fabric-1.5.5.jar', 'vinery_upstream/letsdo-vinery-forge-1.3.10.jar'),
    'MasonJarBlock': ('mods/backportedVintage-*.jar',),
}

# Item -> (modelled item whose coaster models it takes, add the glint). Only foods whose item sprite is the same
# picture as the reference's (coaster_model_candidates.md, score 1.00, same colours).
BORROW = {
    'farmersrespite:long_apple_cider': ('farmersdelight:apple_cider', False),
    'farmersrespite:strong_apple_cider': ('farmersdelight:apple_cider', False),
    'create_central_kitchen:pumpkin_pie_slice': ('delightful:pumpkin_pie_slice', False),
    'goldenfood:enchanted_golden_carrot': ('minecraft:golden_carrot', True),
}
# Farmer's Respite's long_ and strong_ drinks draw the base drink's sprite (its item models name the same texture).
BORROW.update({'farmersrespite:%s_%s' % (v, d): ('farmersrespite:' + d, False)
               for v in ('long', 'strong')
               for d in ('black_tea', 'green_tea', 'yellow_tea', 'purulent_tea', 'rose_hip_tea', 'dandelion_tea',
                         'coffee', 'gamblers_tea')})
GLINT = [{'type': 'brewinandchewin:glint'}]


class Assets:
    """The assets/<ns>/ json files of one or more jars, the first jar's copy winning."""

    def __init__(self, ns, jars):
        self.ns, self.files = ns, {}
        prefix = 'assets/%s/' % ns
        for jar in reversed(jars):
            with zipfile.ZipFile(jar) as z:
                for name in z.namelist():
                    if name.startswith(prefix) and name.endswith('.json') and '/lang/' not in name:
                        try:
                            self.files[name[len(prefix):-len('.json')]] = json.loads(z.read(name))
                        except ValueError:
                            pass

    def model(self, model_id):
        if not model_id or ':' not in model_id or model_id.split(':', 1)[0] != self.ns:
            return None
        return self.files.get('models/' + model_id.split(':', 1)[1])

    def bounds(self, model_id, depth=0):
        """(min xyz, max xyz) of a model's elements, following parents in this namespace; None when it has none."""
        m = self.model(model_id)
        if m is None or depth > 8:
            return None
        elements = m.get('elements')
        if not elements:
            return self.bounds(m.get('parent'), depth + 1)
        lo = [min(e['from'][a] for e in elements) for a in range(3)]
        hi = [max(e['to'][a] for e in elements) for a in range(3)]
        return lo, hi


DEFAULT_VALUES = {'north', '1', '0', 'false', 'none', 'bottom'}


def is_default(key, value):
    if key == 'facing':
        return value == 'north'
    return value in DEFAULT_VALUES


def when_holds(when):
    """Whether a multipart 'when' holds for the default state (facing north, every flag false, counts at 1)."""
    if not when:
        return True
    if 'OR' in when:
        return any(when_holds(w) for w in when['OR'])
    if 'AND' in when:
        return all(when_holds(w) for w in when['AND'])
    return all(any(is_default(k, v) for v in str(vals).split('|')) for k, vals in when.items())


def default_models(blockstate):
    """Models of the default state (facing north, no support leg, one of a stack), unrotated; [] when none."""
    if not blockstate:
        return []
    for key, variant in blockstate.get('variants', {}).items():
        props = dict(p.split('=') for p in key.split(',') if p)
        if all(is_default(k, v) for k, v in props.items()):
            v = variant[0] if isinstance(variant, list) else variant
            return [v['model']] if not v.get('y') else []
    models = []
    for part in blockstate.get('multipart', []):
        apply = part['apply'][0] if isinstance(part['apply'], list) else part['apply']
        if when_holds(part.get('when')) and not apply.get('y'):
            models.append(apply['model'])
    return models


def scale_for(bounds):
    if bounds is None:
        return 1.0
    lo, hi = bounds
    widest = max(hi[0] - lo[0], hi[2] - lo[2])
    height = hi[1] - lo[1]
    scale = min(1.0, TARGET_PX / widest if widest else 1.0, MAX_HEIGHT_PX / height if height else 1.0)
    return 1.0 if scale >= 1.0 else round(scale, 2)


def merged_bounds(assets, models):
    boxes = [b for b in (assets.bounds(m) for m in models) if b]
    if not boxes:
        return None
    return ([min(b[0][a] for b in boxes) for a in range(3)], [max(b[1][a] for b in boxes) for a in range(3)])


def entry_for(item_id, assets, block):
    models = default_models(assets.files.get('blockstates/' + block))
    models = [m for m in models if assets.model(m) is not None]
    if not models:
        return None
    entry = {'item': item_id, 'models': models[0] if len(models) == 1 else [{'model': m} for m in models]}
    scale = scale_for(merged_bounds(assets, models))
    if scale != 1.0:
        entry['scale'] = scale
    return entry


def file_name(item_id):
    return item_id.replace(':', '__') + '.json'


def jars_for(patterns):
    out = []
    for pattern in patterns:
        folder, _, name = pattern.rpartition('/')
        # mods/ jars may be nested in a bundle (Creative Delight members)
        found = (lang_index.pack_paths.mod_jars(name, lang_index.INSTANCE / folder) if folder == 'mods'
                 else sorted(lang_index.INSTANCE.glob(pattern)))
        if found:
            out.append(found[-1])
    return out


def build():
    _source, rows, _dd, _blocks = pc.load()
    present = set(rows) | pc.installed_ids()
    ported = {json.loads(p.read_text())['item']: json.loads(p.read_text()) for p in pc.COASTER_DIR.glob('*.json')}
    if not ported:
        print('WARNING no port coaster models in %s' % pc.COASTER_DIR)
    out, problems, counts = {}, [], {'dd': 0, 'own': 0, 'borrow': 0}

    def wanted(item_id):
        return item_id in present and item_id not in ported and not pc.excluded(item_id)

    # Display Delight, every block the port registers
    dd_jar = pc.dd_jar()
    dd = Assets('displaydelight', [dd_jar])
    links, _tags = pc.dd_from_jar()
    for item_id in sorted(links):
        if not wanted(item_id):
            continue
        kind = next(k for k in KIND_ORDER if k in links[item_id])
        block = links[item_id][kind].split(':', 1)[1]
        entry = entry_for(item_id, dd, block)
        if entry is None:
            problems.append('%s: no default model for displaydelight:%s' % (item_id, block))
            continue
        out[file_name(item_id)] = entry
        counts['dd'] += 1

    # Other mods' placed drinks and jars
    by_class = {}
    for item_id, r in sorted(rows.items()):
        if not (r.get('edible') and r.get('blockItem') and r.get('block')) or not wanted(item_id):
            continue
        cls = next((pc.simple(c) for c in r.get('blockClasses', []) if pc.simple(c) in OWN_BLOCKS), None)
        if cls:
            by_class.setdefault(cls, []).append((item_id, r['block']))
    for cls, items in sorted(by_class.items()):
        jars = jars_for(OWN_BLOCKS[cls])
        if not jars:
            problems.append('%s: none of %s found' % (cls, ', '.join(OWN_BLOCKS[cls])))
            continue
        assets = {}
        for item_id, block in items:
            ns, path = block.split(':', 1)
            if ns not in assets:
                assets[ns] = Assets(ns, jars)
            entry = entry_for(item_id, assets[ns], path)
            if entry is None:
                problems.append('%s: no default model for %s in %s' % (item_id, block, ', '.join(j.name for j in jars)))
                continue
            out[file_name(item_id)] = entry
            counts['own'] += 1

    # Same picture as a modelled food
    for item_id, (ref, glint) in sorted(BORROW.items()):
        if not wanted(item_id):
            continue
        source = ported.get(ref) or out.get(file_name(ref))
        if source is None:
            problems.append('%s: %s has no coaster model to borrow' % (item_id, ref))
            continue
        models = source['models']
        models = [{'model': models}] if isinstance(models, str) else [dict(m) for m in models]
        if glint:
            models = models + [dict(m, texture_modifiers=GLINT) for m in models if 'texture_modifiers' not in m]
        entry = {'item': item_id, 'models': models[0]['model'] if len(models) == 1 and len(models[0]) == 1 else models}
        if 'scale' in source:
            entry['scale'] = source['scale']
        out[file_name(item_id)] = entry
        counts['borrow'] += 1
    return str(dd_jar).rsplit("/", 1)[-1], out, problems, counts


def render(entry):
    return json.dumps(entry, indent=2) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    jar, out, problems, counts = build()
    existing = {p.name for p in OUT.glob('*.json')} if OUT.exists() else set()
    stale = [n for n, e in out.items() if n not in existing or (OUT / n).read_text() != render(e)]
    extra = sorted(existing - set(out))
    for p in problems:
        print('WARNING ' + p)
    if args.check:
        if stale or extra:
            print('%d coaster file(s) missing or out of date, %d left over; run python3 tools/gen_coaster_models.py'
                  % (len(stale), len(extra)))
            return 1
        print('up to date (%d coaster files)' % len(out))
        return 0
    OUT.mkdir(parents=True, exist_ok=True)
    for n in stale:
        (OUT / n).write_text(render(out[n]))
    for n in extra:
        (OUT / n).unlink()
        print('  removed %s' % n)
    print('%s: %d coaster files (%d Display Delight, %d own block models, %d borrowed); wrote %d, removed %d under %s'
          % (jar, len(out), counts['dd'], counts['own'], counts['borrow'], len(stale), len(extra),
             OUT.relative_to(lang_index.INSTANCE)))
    return 0


if __name__ == '__main__':
    sys.exit(main())

#!/usr/bin/env python3
"""Display Delight foods on Brewin' and Chewin' coasters -> kubejs/assets/creativeengineering/brewinandchewin/coaster/.

The Brewin' port's coaster draws an item with its own small model when a coaster JSON names one
(assets/<ns>/brewinandchewin/coaster/*.json, any namespace), and with [coaster] taggedItemsOnly on it takes only
the items in #brewinandchewin:placeable_on_coaster, which the port fills from its own 96 files. Display Delight
already has a placed model for each food it links, so this writes a coaster JSON for every DD-linked food the
port has no model for, pointing at that DD block's default-state model (facing=north, stacks=1).

Which DD block, best first: the food set down on its own (placed), then on a Small Food Plate, then on a Food
Plate. DD's models are full-size blocks (a plate is 14 px across), so each entry gets a "scale" that brings its
widest side down to TARGET_PX, about the size of the port's own bowls.

placeable_catalog.py reads these files too: their items join #creativeengineering:placeable_on_coaster, and
server_scripts/tags/placeabletags.js puts that tag into #brewinandchewin:placeable_on_coaster. So run this first.

DD links come from /dumpplaceable (fallback: /dumpfood + the DD jar), the same way placeable_catalog.py reads them.

    python3 tools/gen_coaster_models.py           write the files
    python3 tools/gen_coaster_models.py --check   exit 1 if any file is missing, stale or left over
"""
import argparse
import json
import sys

import gen_display_delight_models as ddm
import lang_index
import placeable_catalog as pc

OUT = lang_index.INSTANCE / 'kubejs/assets/creativeengineering/brewinandchewin/coaster'
# Widest side of a DD model on a coaster, in pixels; the port's bowls are 12, its tankards 7.
TARGET_PX = 10.0
KIND_ORDER = ('placed', 'small_plate', 'plate')


def model_bounds(files, model, depth=0):
    """(min xyz, max xyz) of a DD model's elements, following displaydelight parents; None when it has none."""
    if not model or not model.startswith('displaydelight:') or depth > 8:
        return None
    m = files.get('models/' + model.split(':', 1)[1])
    if m is None:
        return None
    elements = m.get('elements')
    if not elements:
        return model_bounds(files, m.get('parent'), depth + 1)
    lo = [min(e['from'][a] for e in elements) for a in range(3)]
    hi = [max(e['to'][a] for e in elements) for a in range(3)]
    return lo, hi


def scale_for(bounds):
    if bounds is None:
        return 1.0
    lo, hi = bounds
    widest = max(hi[0] - lo[0], hi[2] - lo[2])
    return 1.0 if widest <= TARGET_PX else round(TARGET_PX / widest, 2)


def file_name(item_id):
    return item_id.replace(':', '__') + '.json'


def build():
    jar, files = ddm.read_jar()
    _source, _rows, dd, _blocks = pc.load()
    ported = {json.loads(p.read_text())['item'] for p in pc.COASTER_DIR.glob('*.json')}
    if not ported:
        print('WARNING no port coaster models in %s' % pc.COASTER_DIR)
    out, problems, skipped = {}, [], []
    items = sorted(set().union(*(set(dd[k]) for k in KIND_ORDER)))
    for item_id in items:
        if item_id in ported:
            skipped.append(item_id)
            continue
        kind = next(k for k in KIND_ORDER if item_id in dd[k])
        block = dd[kind][item_id].split(':', 1)[1]
        blockstate = files.get('blockstates/' + block)
        model = ddm.default_model(blockstate) if blockstate else None
        if model is None or 'models/' + model.split(':', 1)[1] not in files:
            problems.append('%s: no default model for displaydelight:%s' % (item_id, block))
            continue
        entry = {'item': item_id, 'models': model}
        scale = scale_for(model_bounds(files, model))
        if scale != 1.0:
            entry['scale'] = scale
        out[file_name(item_id)] = entry
    return jar, out, problems, skipped


def render(entry):
    return json.dumps(entry, indent=2) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    jar, out, problems, skipped = build()
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
    print('%s: %d Display Delight foods get a coaster model (%d already have the port\'s); wrote %d, removed %d under %s'
          % (jar, len(out), len(skipped), len(stale), len(extra), OUT.relative_to(lang_index.INSTANCE)))
    return 0


if __name__ == '__main__':
    sys.exit(main())

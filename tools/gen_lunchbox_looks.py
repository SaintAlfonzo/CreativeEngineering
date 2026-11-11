#!/usr/bin/env python3
"""Lunchbox looks: dyeable textures and the item models -> kubejs/assets/solapplepie/.

Spice of Life's three lunchboxes plus the pack's Netherite Lunchbox (patcherMod lunchbox/) each show one of four
looks: lunchbag (paper, the jar's own texture), copper, iron, netherite (source art in
kubejs/assets/solapplepie/textures/items/). Their own looks: Lunchbag = lunchbag, Lunchbox (Copper Lunchbox in game)
= copper, Golden Lunchbox (Iron Lunchbox) = iron, Netherite Lunchbox = netherite. Spice of Life's own lunchbox.png and
golden_lunchbox.png stay untouched (the camera bags use them).

For each look this writes
  textures/items/lunchbox_dye/<look>_base.png     the base, greyscale, for the dye tint (layer 0)
  textures/items/lunchbox_dye/<look>_overlay.png  outline and latches as drawn (layer 1, never tinted)
  models/item/lunchbox_look/<look>.json, <look>_dyed.json
and one item model per box with the overrides for its skins and dye. The outline is the mask in
iron_lunchbox_outline.png (netherite_lunchbox_outline.png must match it); the latches are LATCH colours; the
lunchbag has no outline file, so its darkest browns (every channel under BAG_OUTLINE_MAX) are its outline.

Override numbers: the item property creativedelight:lunchbox_skin is the position in SKINS + 1, which must
match Lunchboxes.SKINS in patcherMod. Only append to both.

    python3 tools/gen_lunchbox_looks.py           write the files
    python3 tools/gen_lunchbox_looks.py --check   exit 1 if any file is missing or stale
"""
import argparse
import glob
import io
import json
import os
import sys
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, 'kubejs', 'assets', 'solapplepie')
TEX = os.path.join(ASSETS, 'textures', 'items')

# look -> source texture (a file in TEX, or None = the lunchbag from the Spice of Life jar)
LOOKS = {
    'lunchbag': None,
    'copper': 'copper_lunchbox.png',
    'iron': 'iron_lunchbox.png',
    'netherite': 'netherite_lunchbox.png',
}
LOOK_TEXTURE = {
    'lunchbag': 'solapplepie:items/lunchbag',
    'copper': 'solapplepie:items/copper_lunchbox',
    'iron': 'solapplepie:items/iron_lunchbox',
    'netherite': 'solapplepie:items/netherite_lunchbox',
}
LATCH = {
    'copper': {(250, 214, 74)},
    'iron': {(250, 214, 74)},
    'netherite': {(32, 197, 181)},
}
BAG_OUTLINE_MAX = 70
# greyscale range of the dyeable base (light, like vanilla's leather armour, so a dye reads true)
BASE_LO, BASE_HI = 110, 240

# box item -> (tier, own look). Tier order = Lunchboxes.TIERS.
BOXES = {
    'lunchbag': (0, 'lunchbag'),
    'lunchbox': (1, 'copper'),
    'golden_lunchbox': (2, 'iron'),
    'netherite_lunchbox': (3, 'netherite'),
}
# Lunchboxes.SKINS, same order: (item id, look name or None, model when not a look)
SKINS = [
    ('solapplepie:lunchbag', 'lunchbag', None),
    ('solapplepie:lunchbox', 'copper', None),
    ('solapplepie:golden_lunchbox', 'iron', None),
    ('fantasyfurniture:decorations/berry_basket_empty', None, 'fantasyfurniture:item/decorations/berry_basket_empty'),
    ('fantasyfurniture:decorations/berry_basket_blueberry', None, 'fantasyfurniture:item/decorations/berry_basket_blueberry'),
    ('fantasyfurniture:decorations/berry_basket_strawberry', None, 'fantasyfurniture:item/decorations/berry_basket_strawberry'),
    ('fantasyfurniture:decorations/berry_basket_sweetberry', None, 'fantasyfurniture:item/decorations/berry_basket_sweetberry'),
]
SKIN_KEY = 'creativedelight:lunchbox_skin'
DYED_KEY = 'creativedelight:lunchbox_dyed'


def load_lunchbag():
    jars = sorted(glob.glob(os.path.join(ROOT, 'mods', 'solapplepie-*.jar')))
    if not jars:
        sys.exit('no mods/solapplepie-*.jar')
    with zipfile.ZipFile(jars[-1]) as z:
        return Image.open(io.BytesIO(z.read('assets/solapplepie/textures/items/lunchbag.png'))).convert('RGBA')


def mask_of(path):
    im = Image.open(path).convert('RGBA')
    px = im.load()
    return {(x, y) for x in range(im.width) for y in range(im.height) if px[x, y][3] > 0}


def split(look, im, outline):
    """(base greyscale, overlay) images for one look."""
    px = im.load()
    w, h = im.size
    overlay = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    base = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    opx, bpx = overlay.load(), base.load()
    latch = LATCH.get(look, set())
    base_pts = []
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3] == 0:
                continue
            if look == 'lunchbag':
                is_over = max(p[:3]) < BAG_OUTLINE_MAX
            else:
                is_over = (x, y) in outline or p[:3] in latch
            if is_over:
                opx[x, y] = p
            else:
                base_pts.append((x, y, 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2], p[3]))
    lo = min(b[2] for b in base_pts)
    hi = max(b[2] for b in base_pts)
    for x, y, lum, a in base_pts:
        t = 0.0 if hi == lo else (lum - lo) / (hi - lo)
        g = round(BASE_LO + t * (BASE_HI - BASE_LO))
        bpx[x, y] = (g, g, g, a)
    return base, overlay


def png_bytes(im):
    buf = io.BytesIO()
    im.save(buf, 'PNG')
    return buf.getvalue()


def json_bytes(obj):
    return (json.dumps(obj, indent=2) + '\n').encode()


def look_model(look):
    return 'solapplepie:item/lunchbox_look/' + look


def item_model(tier, own):
    overrides = [{'predicate': {DYED_KEY: 1}, 'model': look_model(own) + '_dyed'}]
    for n, (sid, look, model) in enumerate(SKINS, start=1):
        if look is not None:
            skin_tier = next(t for t, l in BOXES.values() if l == look)
            if skin_tier >= tier:
                continue
            overrides.append({'predicate': {SKIN_KEY: n}, 'model': look_model(look)})
            overrides.append({'predicate': {SKIN_KEY: n, DYED_KEY: 1}, 'model': look_model(look) + '_dyed'})
        else:
            overrides.append({'predicate': {SKIN_KEY: n}, 'model': model})
    return {'parent': look_model(own), 'overrides': overrides}


def outputs():
    outline = mask_of(os.path.join(TEX, 'iron_lunchbox_outline.png'))
    if mask_of(os.path.join(TEX, 'netherite_lunchbox_outline.png')) != outline:
        sys.exit('iron and netherite outline masks differ; split() expects one shared mask')
    files = {}
    for look, src in LOOKS.items():
        im = load_lunchbag() if src is None else Image.open(os.path.join(TEX, src)).convert('RGBA')
        base, overlay = split(look, im, outline)
        files[os.path.join(TEX, 'lunchbox_dye', look + '_base.png')] = png_bytes(base)
        files[os.path.join(TEX, 'lunchbox_dye', look + '_overlay.png')] = png_bytes(overlay)
        mdir = os.path.join(ASSETS, 'models', 'item', 'lunchbox_look')
        files[os.path.join(mdir, look + '.json')] = json_bytes(
            {'parent': 'minecraft:item/generated', 'textures': {'layer0': LOOK_TEXTURE[look]}})
        files[os.path.join(mdir, look + '_dyed.json')] = json_bytes({
            'parent': 'minecraft:item/generated',
            'textures': {'layer0': 'solapplepie:items/lunchbox_dye/%s_base' % look,
                         'layer1': 'solapplepie:items/lunchbox_dye/%s_overlay' % look}})
    for box, (tier, own) in BOXES.items():
        files[os.path.join(ASSETS, 'models', 'item', box + '.json')] = json_bytes(item_model(tier, own))
    return files


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    stale = []
    for path, data in outputs().items():
        old = open(path, 'rb').read() if os.path.exists(path) else None
        if old == data:
            continue
        rel = os.path.relpath(path, ROOT)
        if args.check:
            stale.append(rel)
            continue
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, 'wb') as f:
            f.write(data)
        print('wrote', rel)
    if stale:
        print('stale or missing:\n  ' + '\n  '.join(stale))
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())

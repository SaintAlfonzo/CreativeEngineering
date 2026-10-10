#!/usr/bin/env python3
"""Spawn egg colours for each moobloom variant, from its coat (feature_writeup/mooblooms.md 4.4).

For every data/<ns>/moobloom_variants/<path>.json in src/main/resources with a coat at
assets/<ns>/textures/entity/moobloom/<path>.png:
  base  = the commonest opaque colour of the coat
  (pixels that are identical on every coat, like the udder and eyes, are skipped)
  spots = the commonest colour that is clearly different from the base (RGB distance >= MIN_DISTANCE)
and writes them as "egg_colors": ["#base", "#spots"] into the variant JSON (other keys untouched).

    python3 tools/egg_colors.py           # write
    python3 tools/egg_colors.py --check   # print only, exit 1 if a file would change
"""
import json
import sys
from collections import Counter
from pathlib import Path

from PIL import Image

MIN_DISTANCE = 60

RES = Path(__file__).resolve().parent.parent / 'src' / 'main' / 'resources'


def distance(a, b):
    return sum((x - y) ** 2 for x, y in zip(a, b)) ** 0.5


def shared_pixels(images):
    """Pixel indices that are the same on every coat (udder, eyes, horns, hooves): not part of any coat's look."""
    data = [list(img.getdata()) for img in images]
    if len(data) < 2 or len({len(d) for d in data}) != 1:
        return set()
    return {i for i in range(len(data[0])) if all(d[i] == data[0][i] for d in data[1:])}


def egg_colors(img, shared=frozenset()):
    counts = Counter(p[:3] for i, p in enumerate(img.getdata()) if p[3] >= 128 and i not in shared)
    ranked = [c for c, _ in counts.most_common()]
    if not ranked:
        raise ValueError('no opaque pixels')
    base = ranked[0]
    spots = next((c for c in ranked[1:] if distance(c, base) >= MIN_DISTANCE), ranked[1] if len(ranked) > 1 else base)
    return ['#%02X%02X%02X' % base, '#%02X%02X%02X' % spots]


def main():
    check = '--check' in sys.argv
    changed = 0
    coats = {}
    for variant in sorted(RES.glob('data/*/moobloom_variants/**/*.json')):
        ns = variant.relative_to(RES).parts[1]
        path = variant.relative_to(RES / 'data' / ns / 'moobloom_variants').with_suffix('')
        png = RES / 'assets' / ns / 'textures' / 'entity' / 'moobloom' / path.with_suffix('.png')
        if not png.is_file():
            print(f'{ns}:{path.as_posix()}: no coat at {png.relative_to(RES)}, skipped')
            continue
        coats[variant] = (f'{ns}:{path.as_posix()}', Image.open(png).convert('RGBA'))
    shared = shared_pixels([img for _, img in coats.values()])
    for variant, (name, img) in coats.items():
        colors = egg_colors(img, shared)
        data = json.loads(variant.read_text())
        mark = '' if data.get('egg_colors') == colors else '  (changed)'
        print(f'{name}: {colors[0]} {colors[1]}{mark}')
        if mark:
            changed += 1
            if not check:
                data['egg_colors'] = colors
                variant.write_text(json.dumps(data, indent=2) + '\n')
    if check and changed:
        sys.exit(1)


if __name__ == '__main__':
    main()

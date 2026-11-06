#!/usr/bin/env python3
# Jam line art from colour samples (CreativeEngineering2, TODO dishes/jams #8, textures/food #9). Own work, GPLv3.
#
# Every jar sprite, jar interior, jam block, jello block and bowl of jello is one colour sample palette-swapped
# (textureswapper/palette_swap.py, brightness rank) onto a fixed base, so a new colour gives the whole set:
#   jar item       recoloured Fruits Delight item/jelly_content, under Fruits Delight item/jam_bottle
#   jar interior   recoloured textureswapper/mason_jar/interior_jam_base (jams) or interior_jar_base (smooth fills)
#   jam block      recoloured Fruits Delight block/jelly (Fruits Delight's jam block and jam fluid texture; tiles)
#   jello block    recoloured Fruits Delight block/jello (Fruits Delight's jello block and jello fluid texture)
#   bowl of jello  recoloured textureswapper/mason_jar/jello.png over bowl.png
# Fruits Delight is All Rights Reserved: its textures are read from the jar in mods/ at run time, never copied whole.
#
#   python3 development/creativeDelight/members/vintage/tools/gen_jam_art.py --apply      writes kubejs/assets/mason_jar_new/
#   python3 development/creativeDelight/members/vintage/tools/gen_jam_art.py --preview OUT.png [--source fruit]
# then gen_pack_jams.py, port_resources.py, build :creativeDelight:.
import argparse
import glob
import io
import os
import sys
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
INSTANCE = os.path.abspath(os.path.join(HERE, '../../../../..'))
sys.path.insert(0, os.path.join(INSTANCE, 'textureswapper'))
from palette_swap import get_colors_by_brightness, lerp_palette, palette_swap  # noqa: E402

MJ = os.path.join(INSTANCE, 'textureswapper/mason_jar')
OUT = os.path.join(INSTANCE, 'kubejs/assets/mason_jar_new')

# art stem (gen_pack_jams.py JAMS / RETEXTURE naming), colour sample in new_color_jam or new_color_jar, fruit sample
# in fruit/jello (None: no fruit colour), interior base. Jams make all five pieces; smooth jars only jar + interior.
JAMS = [
    ('ancient_fruit', 'new_color_jam/ancient_fruit', 'ancient_fruit'),
    ('banana', 'new_color_jam/banana', 'banana'),
    ('cherry', 'new_color_jam/cherry_jam', 'cherry'),
    ('green_grape', 'new_color_jam/green_grape_jam', 'white_grape'),
    ('mulberry', 'new_color_jam/mulberry', 'mulberry'),
    ('pepper', 'new_color_jam/pepper_jam', 'pickled_pepper'),
    ('raspberry', 'new_color_jam/raspberry_jam', 'raspberry'),
    ('red_grape', 'new_color_jam/red_grape_jam', 'red_grape'),
    ('salmonberry', 'new_color_jam/salmonberry_jam', 'salmonberries'),
    ('strawberries', 'new_color_jam/strawberries', 'strawberries'),
]
# jams that keep their older colour sample under --source fruit (owner 2026-11-03: pepper)
KEEP_CURRENT = {'pepper'}
# smooth jars: art name in new_jar, interior name in new_mason_jar_interior, colour sample
JARS = [
    ('honey_jar', 'honey_jar_interior_texture', 'honey'),
]


def fd_textures():
    jars = sorted(glob.glob(os.path.join(INSTANCE, 'mods/fruitsdelight-*.jar')))
    if not jars:
        raise SystemExit('Fruits Delight jar not in mods/')
    out = {}
    with zipfile.ZipFile(jars[-1]) as z:
        for key, path in (('jelly_content', 'item/jelly_content'), ('jam_bottle', 'item/jam_bottle'),
                          ('jam_swirl', 'block/jelly'), ('jello', 'block/jello')):
            out[key] = np.array(Image.open(io.BytesIO(z.read(f'assets/fruitsdelight/textures/{path}.png'))).convert('RGBA'))
    return out


FD = None
BASE = {}


def bases():
    global FD
    if FD is None:
        FD = fd_textures()
        for n in ('interior_jam_base', 'interior_jar_base', 'jello', 'bowl'):
            BASE[n] = np.array(Image.open(os.path.join(MJ, n + '.png')).convert('RGBA'))


def palette_of(colour_png, drop_darkest=0):
    """Brightness-ordered palette of a colour sample, semi-transparent pixels left out (they would come out as
    see-through pixels in the recoloured texture: white_grape.png has some)."""
    arr = np.array(Image.open(colour_png).convert('RGBA'))
    arr[arr[:, :, 3] < 255] = 0
    pal = list(reversed(get_colors_by_brightness(arr)))  # darkest first
    return pal[drop_darkest:] if 0 < drop_darkest < len(pal) else pal


def swap(base, colour_png, interpolate=False, drop_darkest=0):
    return palette_swap(base, palette_of(colour_png, drop_darkest), interpolate)


def even_swap(base, colour_png, drop_darkest=0):
    """Forced even gradient: the base's distinct brightness levels, by rank, spaced evenly along the whole palette (blended),
    whatever the palette's size (palette_swap's interpolate only blends when the palette is smaller than the base)."""
    pal = palette_of(colour_png, drop_darkest)
    levels = sorted(set(int(c[0]) + int(c[1]) + int(c[2]) for c in get_colors_by_brightness(base)))
    rank = dict((b, i) for i, b in enumerate(levels))
    out = base.copy()
    for c in get_colors_by_brightness(base):
        mask = (base[:, :, :4] == np.array(c)).all(axis=2)
        t = rank[int(c[0]) + int(c[1]) + int(c[2])] / float(len(levels) - 1) if len(levels) > 1 else 0.5
        out[mask] = lerp_palette(pal, t)
    return out


def under(top, bottom):
    im = Image.fromarray(bottom.copy())
    im.alpha_composite(Image.fromarray(top))
    return im


def jam_set(colour_png, ip=False):
    bases()
    return {
        'jar': under(FD['jam_bottle'], swap(FD['jelly_content'], colour_png, ip)),
        'interior': Image.fromarray(swap(BASE['interior_jam_base'], colour_png, ip)),
        'jam_block': Image.fromarray(swap(FD['jam_swirl'], colour_png, ip)),
        'jello_block': Image.fromarray(swap(FD['jello'], colour_png, ip)),
        'bowl': under(swap(BASE['jello'], colour_png, ip), BASE['bowl']),
    }


def jar_set(colour_png, ip=False):
    bases()
    return {
        'jar': under(FD['jam_bottle'], swap(FD['jelly_content'], colour_png, ip)),
        'interior': Image.fromarray(swap(BASE['interior_jar_base'], colour_png, ip)),
    }


def sample(rel):
    return os.path.join(MJ, rel + '.png')


def save(im, sub, name):
    path = os.path.join(OUT, sub, name + '.png')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)


# Owner picks 2026-11-03 (apply): jar, jam block, jello block and bowl are interpolated for these jams; pepper is left
# alone (its art is the committed original: jar, interior, jam, jello, bowl); the interiors listed in
# INTERIOR_DROP_DARKEST are swapped without the sample's darkest colour; the other interiors are the plain swap.
INTERPOLATE = {'ancient_fruit', 'banana', 'raspberry', 'green_grape', 'mulberry', 'red_grape', 'salmonberry',
               'strawberries', 'pepper'}
# SKIP: art kept as it is on disk. Pepper: committed originals, except its jam and jello block (plain swap of the older
# sample). Accepted by the owner 2026-11-03 (frozen): ancient fruit, banana, cherry, mulberry, raspberry, strawberries.
SKIP = {'pepper', 'ancient_fruit', 'banana', 'cherry', 'mulberry', 'raspberry', 'salmonberry', 'strawberries'}
INTERIOR_DROP_DARKEST = {'banana', 'cherry', 'green_grape', 'mulberry', 'raspberry', 'salmonberry'}

# FROZEN: pieces of a jam kept as they are on disk (red grape's blocks and bowl were accepted with the first six)
FROZEN = {'red_grape': {'jam_block', 'jello_block', 'bowl'},
          'pepper': {'interior', 'jello_block', 'bowl'}}  # pepper: only the jar and jam block come from the colour sample

# Accepted 2026-11-03: these (otherwise frozen) jams get their interior from the forced-gradient swap, darkest colour dropped
GRADIENT_INTERIORS = {'banana', 'raspberry', 'mulberry'}
# Accepted 2026-11-03: jar interior from even_swap (forced even gradient), with this many darkest colours dropped
EVEN_INTERIORS = {'red_grape': 0, 'green_grape': 1, 'cherry': 1, 'salmonberry': 2}

# Pieces made with even_swap (forced even gradient) instead of the plain swap, which skips non-monotonic palette entries
# (pepper's dark reds are darker than its browns' brightness rank): owner 2026-11-04
EVEN_PIECES = {'pepper': {'jar', 'jam_block'}}

PIECES = ('jar', 'interior', 'jam_block', 'jello_block', 'bowl', 'jars')


def apply(source, pieces, only=None):
    n = 0
    for stem, colour, fruit in JAMS:
        if only and stem not in only:
            continue
        if stem in GRADIENT_INTERIORS and 'interior' in pieces:
            bases()
            src = sample(colour) if stem in KEEP_CURRENT else sample(f'fruit/jello/{fruit}')
            save(Image.fromarray(swap(BASE['interior_jam_base'], src, True, 1)), 'new_mason_jar_interior',
                 f'{stem}_jam_interior_texture')
            n += 1
        if stem in EVEN_INTERIORS and 'interior' in pieces:
            bases()
            src = sample(colour) if stem in KEEP_CURRENT else sample(f'fruit/jello/{fruit}')
            save(Image.fromarray(even_swap(BASE['interior_jam_base'], src, EVEN_INTERIORS[stem])),
                 'new_mason_jar_interior', f'{stem}_jam_interior_texture')
            n += 1
        if stem in SKIP:
            continue
        src = sample(f'fruit/jello/{fruit}') if source == 'fruit' and stem not in KEEP_CURRENT else sample(colour)
        s = jam_set(src, stem in INTERPOLATE)
        if stem in EVEN_PIECES:
            bases()
            if 'jar' in EVEN_PIECES[stem]:
                s['jar'] = under(FD['jam_bottle'], even_swap(FD['jelly_content'], src))
            if 'jam_block' in EVEN_PIECES[stem]:
                s['jam_block'] = Image.fromarray(even_swap(FD['jam_swirl'], src))
        s['interior'] = Image.fromarray(swap(BASE['interior_jam_base'], src, False,
                                             1 if stem in INTERIOR_DROP_DARKEST else 0))
        for piece, sub, name in (('jar', 'new_jar', f'{stem}_jam'),
                                 ('interior', 'new_mason_jar_interior', f'{stem}_jam_interior_texture'),
                                 ('jam_block', 'new_jam_block', f'{stem}_jam'),
                                 ('jello_block', 'new_jello_block', f'{stem}_jello'),
                                 ('bowl', 'new_jello_bowl', f'{stem}_jello_bowl')):
            if piece == 'interior' and (stem in EVEN_INTERIORS or stem in GRADIENT_INTERIORS):
                continue
            if piece in pieces and piece not in FROZEN.get(stem, ()):
                save(s[piece], sub, name)
                n += 1
    if 'jars' in pieces:
        for art, interior, colour in JARS:
            s = jar_set(sample(colour))
            save(s['jar'], 'new_jar', art)
            save(s['interior'], 'new_mason_jar_interior', interior)
            n += 2
    print(f'{n} files ({source} colours) written to {OUT}')


def preview(path):
    """Per fruit: the set from today's colour sample, then the set from the fruit/jello sample. Blocks are shown
    tiled 2x2 so a seam shows."""
    z, pad, label_w = 5, 6, 150
    cols = ['jar', 'interior', 'jam_block', 'jello_block', 'bowl']
    cell = 16 * z
    block_cell = 32 * z
    widths = [block_cell if c.endswith('block') else cell for c in cols]
    set_w = sum(widths) + pad * len(cols)
    rows = [(stem, sample(colour), sample(f'fruit/jello/{fruit}')) for stem, colour, fruit in JAMS]
    jar_rows = [(art, sample(colour)) for art, _, colour in JARS]
    head = 40
    W = label_w + 2 * set_w + 3 * pad + 20
    H = head + len(rows) * (block_cell + pad) + 50 + len(jar_rows) * (block_cell + pad) + pad
    sheet = Image.new('RGBA', (W, H), (48, 48, 52, 255))
    d = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 15)
        bold = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 17)
    except OSError:
        font = bold = ImageFont.load_default()
    x0, x1 = label_w, label_w + set_w + 2 * pad + 20
    d.text((x0, 6), 'Current colour sample (blocks fixed)', fill='white', font=bold)
    d.text((x1, 6), 'New fruit/jello colour sample', fill=(255, 220, 120), font=bold)
    d.text((x0, 24), '   jar      interior        jam block                 jello block              bowl',
           fill=(170, 170, 170), font=font)
    d.text((x1, 24), '   jar      interior        jam block                 jello block              bowl',
           fill=(170, 170, 170), font=font)
    d.line([(x1 - 12, 0), (x1 - 12, H)], fill=(90, 90, 96), width=2)

    def draw_set(s, x, y):
        for c, w in zip(cols, widths):
            if c in s:
                im = s[c]
                if c.endswith('block'):
                    t = Image.new('RGBA', (32, 32))
                    for dx in (0, 16):
                        for dy in (0, 16):
                            t.paste(im, (dx, dy))
                    im = t
                im = im.resize((im.width * z, im.height * z), Image.NEAREST)
                sheet.alpha_composite(im, (x, y + (block_cell - im.height) // 2))
            x += w + pad

    y = head
    for name, cur, new in rows:
        d.text((8, y + block_cell // 2 - 8), name.replace('_', ' '), fill='white', font=font)
        if cur:
            draw_set(jam_set(cur), x0, y)
        else:
            d.text((x0, y + block_cell // 2 - 8), 'Fruits Delight tinted art today', fill=(150, 150, 150), font=font)
        draw_set(jam_set(new), x1, y)
        y += block_cell + pad
    y += 20
    d.text((8, y), 'Smooth jars (new sample)', fill='white', font=bold)
    y += 30
    for name, src in jar_rows:
        d.text((8, y + block_cell // 2 - 8), name.replace('_', ' '), fill='white', font=font)
        draw_set(jar_set(src), x0, y)
        y += block_cell + pad
    sheet.save(path)
    print('preview:', path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--apply', action='store_true', help='write the art into kubejs/assets/mason_jar_new/')
    ap.add_argument('--source', choices=('current', 'fruit'), default='fruit',
                    help='colour samples for --apply: fruit/jello (fruit, owner pick 2026-11-03) or the older new_color_jam (current)')
    ap.add_argument('--pieces', default=','.join(PIECES), help='comma list of ' + ', '.join(PIECES))
    ap.add_argument('--only', help='comma list of jam stems (default all)')
    ap.add_argument('--preview', metavar='PNG')
    a = ap.parse_args()
    if a.preview:
        preview(a.preview)
    if a.apply:
        apply(a.source, a.pieces.split(','), a.only.split(',') if a.only else None)
    if not (a.preview or a.apply):
        ap.print_help()


if __name__ == '__main__':
    main()

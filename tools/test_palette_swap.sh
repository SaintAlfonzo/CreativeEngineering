#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Offline checks for textureswapper/palette_swap.py (TODO textures/food #10, the three fixed faults). Synthetic in-memory
# images and temp files only; no pack texture is read or written.
#   1. legacy parity: no flag, and --interpolate with a palette smaller than the source, equal the pre-fix mapping
#      (frozen copy below) on random images and palettes
#   2. even rank spacing (--even): bunched source levels spread over the whole palette, one palette step per rank
#   3. --interpolate with a palette larger than the source: pixel-weighted run means, not the legacy map
#   4. non-monotonic palette (--even): a dark-red run among browns shows up; the legacy map skips it
#   5. CLI: single and bulk runs with --even / --interpolate write outputs; the legacy CLI output is unchanged
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
cd "$ROOT"

python3 - "$T" <<'EOF'
import os, subprocess, sys
import numpy as np
from PIL import Image
sys.path.insert(0, 'textureswapper')
import palette_swap as ps

T = sys.argv[1]
fails = []


def check(name, ok, detail=''):
	print(('PASS  ' if ok else 'FAIL  ') + name + (('  ' + detail) if detail and not ok else ''))
	if not ok:
		fails.append(name)


def legacy(src_arr, palette_colors, interpolate=False):
	# palette_swap() as of 2026-11-21, frozen for the parity check
	src_colors = ps.get_colors_by_brightness(src_arr)
	n_palette = len(palette_colors)
	br = lambda c: int(c[0]) + int(c[1]) + int(c[2])
	sb = [br(c) for c in src_colors]
	lo, hi = min(sb), max(sb)
	blend = interpolate and 1 < n_palette < len(src_colors)
	levels = sorted(set(sb))
	rank = dict((b, i) for i, b in enumerate(levels))

	def m(b):
		if blend:
			t = rank[b] / float(len(levels) - 1) if len(levels) > 1 else 0.5
			return ps.lerp_palette(palette_colors, t)
		t = (b - lo) / float(hi - lo) if hi != lo else 0.5
		return palette_colors[max(0, min(n_palette - 1, int(round(t * (n_palette - 1)))))]
	out = src_arr.copy()
	for c in src_colors:
		out[(src_arr == np.array(c)).all(axis=2)] = m(br(c))
	return out


def image(colors, size=8, transparent=True):
	# one pixel per color, row-major, the rest transparent (or the first color repeated)
	arr = np.zeros((size, size, 4), np.uint8)
	flat = arr.reshape(-1, 4)
	for i, c in enumerate(colors):
		flat[i] = c
	if not transparent:
		flat[len(colors):] = colors[0]
	return arr


rng = np.random.default_rng(1121)

# 1. legacy parity
bad = 0
for trial in range(300):
	k = int(rng.integers(1, 30))
	src = image([tuple(rng.integers(0, 256, 3).tolist()) + (255,) for _ in range(k)], 8)
	src[rng.random((8, 8)) < 0.2] = 0
	if not len(ps.get_colors_by_brightness(src)):
		continue
	pal = [tuple(rng.integers(0, 256, 3).tolist()) + (255,) for _ in range(int(rng.integers(1, 30)))]
	pal = list(dict.fromkeys(pal))
	n_src = len(ps.get_colors_by_brightness(src))
	if not np.array_equal(ps.palette_swap(src, pal), legacy(src, pal)):
		bad += 1
	if len(pal) < n_src and not np.array_equal(ps.palette_swap(src, pal, True), legacy(src, pal, True)):
		bad += 1
check('1 legacy parity (default and --interpolate with a smaller palette, 300 random cases)', bad == 0, str(bad))

# 2. even rank spacing: 5 source levels bunched at the dark end, 9-color gray palette, darkest first
src_levels = [10, 14, 18, 22, 250]
src = image([(v, v, v, 255) for v in src_levels])
pal = [(g, g, g, 255) for g in range(0, 256, 32)]          # 8 colors
legacy_out = sorted(set(map(tuple, ps.palette_swap(src, pal).reshape(-1, 4)[:5].tolist())))
even_out = ps.palette_swap(src, pal, even=True).reshape(-1, 4)[:5].tolist()
check('2a legacy map bunches (fewer distinct outputs than levels)', len(legacy_out) < 5, str(legacy_out))
grays = [c[0] for c in even_out]
check('2b --even gives one distinct, ascending palette color per level', grays == sorted(set(grays)) and len(grays) == 5,
	  str(grays))
check('2c --even spans the palette end to end', grays[0] == pal[0][0] and grays[-1] == pal[-1][0], str(grays))
pal3 = [(0, 0, 0, 255), (128, 128, 128, 255), (255, 255, 255, 255)]
g3 = [c[0] for c in ps.palette_swap(src, pal3, even=True).reshape(-1, 4)[:5].tolist()]
check('2d --even, palette smaller than the levels: rank steps 0,0/1,1,1/2,2', g3 == [0, 0, 128, 255, 255] or
	  g3 == [0, 128, 128, 128, 255] or g3 == [0, 0, 128, 128, 255], str(g3))

# 3. --interpolate with a larger palette: 3 source levels, 6-color palette -> 3 runs of 2, weighted by pixel count
src = image([(20, 20, 20, 255), (120, 120, 120, 255), (220, 220, 220, 255)])
pal = [(0, 0, 0, 255), (30, 30, 30, 255), (90, 90, 90, 255), (120, 120, 120, 255), (200, 200, 200, 255),
	   (240, 240, 240, 255)]
counts = {pal[0]: 1, pal[1]: 3, pal[2]: 1, pal[3]: 1, pal[4]: 1, pal[5]: 1}
got = [c[0] for c in ps.palette_swap(src, pal, interpolate=True, palette_counts=counts).reshape(-1, 4)[:3].tolist()]
want = [int(round((0 * 1 + 30 * 3) / 4.0)), 105, 220]
check('3a --interpolate, larger palette: run means (pixel weighted)', got == want, '%s != %s' % (got, want))
check('3b differs from the legacy map (which ignored --interpolate here)',
	  not np.array_equal(ps.palette_swap(src, pal, True, palette_counts=counts), legacy(src, pal, True)))

pal3 = pal[:1] + pal[3:4] + pal[5:]
got = [c[0] for c in ps.palette_swap(src, pal3, interpolate=True).reshape(-1, 4)[:3].tolist()]
check('3c --interpolate, palette as large as the source: palette colors in rank order', got == [0, 120, 240], str(got))

# 4. non-monotonic palette: dark reds (most-used) sit between a black outline and browns in brightness order
reds = [(90, 10, 10, 255), (110, 15, 12, 255), (125, 18, 14, 255)]
browns = [(70, 50, 30, 255), (100, 70, 40, 255), (120, 85, 50, 255), (140, 100, 60, 255), (160, 115, 70, 255),
		  (180, 130, 80, 255), (200, 150, 95, 255)]
pal_img = np.zeros((4, 16, 4), np.uint8)
cells = [(0, 0, 0, 255)] * 2 + [c for c in reds for _ in range(6)] + browns
for i, c in enumerate(cells):
	pal_img.reshape(-1, 4)[i] = c
_, pal, counts = None, list(reversed(ps.get_colors_by_brightness(pal_img))), ps.color_counts(pal_img)
src = image([(v, v, v, 255) for v in (5, 60, 120, 180)])
is_red = lambda c: c[0] > 2 * c[1] + 20
old = ps.palette_swap(src, pal).reshape(-1, 4)[:4].tolist()
new = ps.palette_swap(src, pal, even=True, palette_counts=counts).reshape(-1, 4)[:4].tolist()
check('4a legacy map skips the dark reds (fixture premise)', not any(is_red(c) for c in old), str(old))
check('4b --even keeps a dark red (most-used color of its run)', any(is_red(c) for c in new), str(new))
check('4c --even output colors are real palette colors', all(tuple(c) in pal for c in new), str(new))

# 5. CLI on temp files
src_p, pal_p = os.path.join(T, 'src.png'), os.path.join(T, 'pal.png')
Image.fromarray(image([(v, v, v, 255) for v in (10, 14, 18, 22, 250)]), 'RGBA').save(src_p)
Image.fromarray(pal_img, 'RGBA').save(pal_p)
script = os.path.join('textureswapper', 'palette_swap.py')
ok = True
for flags, ref in (([], ps.palette_swap), (['--even'], None), (['--interpolate'], None), (['--even', '--invert'], None)):
	out_p = os.path.join(T, 'out_%s.png' % '_'.join(f.strip('-') for f in flags))
	r = subprocess.run([sys.executable, script, src_p, pal_p, out_p] + flags, capture_output=True, text=True)
	if r.returncode or not os.path.exists(out_p):
		ok = False
		print(r.stderr[-400:])
		continue
	if not flags:
		_, p, = ps.load_palette_colors(pal_p, False)
		ok = ok and np.array_equal(np.array(Image.open(out_p).convert('RGBA')),
								   legacy(np.array(Image.open(src_p).convert('RGBA')), p))
check('5a CLI single runs (plain output equals the legacy map)', ok)
bulk = os.path.join(T, 'bulk')
os.makedirs(os.path.join(bulk, 'in'))
Image.fromarray(pal_img, 'RGBA').save(os.path.join(bulk, 'in', 'pepper.png'))
r = subprocess.run([sys.executable, os.path.abspath(script), src_p, '--bulk', '--even', '--interpolate'], cwd=bulk,
				   capture_output=True, text=True)
check('5b CLI bulk run with --even --interpolate', r.returncode == 0 and os.path.exists(os.path.join(bulk, 'out', 'pepper.png')),
	  r.stdout[-300:] + r.stderr[-300:])

print('palette_swap: %d failed' % len(fails))
sys.exit(1 if fails else 0)
EOF

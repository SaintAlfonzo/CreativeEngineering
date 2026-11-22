# SPDX-License-Identifier: GPL-3.0-only
"""Swap engine modes and knobs (plan_texture_tools_gui.md section 3, P1).

S1 default SwapOptions (and mode even, interpolate always) equal the frozen legacy palette_swap on every fixture
S2 extract_palette defaults equal load_palette_colors; alpha threshold, exclusion with tolerance, HSV shift
S3 per-mode properties: rank, even, role, nearest, ramp; interpolate off / smaller / always, amount, spaces
S4 knobs: source keep, preserve, alpha source, stretch, dither; transparent pixels untouched in every mode
S5 CLI: new flags run, --mode even == --even byte for byte, cli_args round trip, bad values exit 1

Synthetic arrays (seeded) plus the top-level textureswapper/*.png sprites read only; outputs in a temp dir.
Usage: python3 texturetools/tests/test_swap_modes.py [-v]
"""
import contextlib
import glob
import importlib.util
import io
import itertools
import os
import shutil
import sys
import tempfile

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)
VERBOSE = '-v' in sys.argv

from texturetools.cli import palette_swap as cli  # noqa: E402
from texturetools.core import palette as P  # noqa: E402
from texturetools.core import swap as S  # noqa: E402
from texturetools.core.swap import SwapOptions  # noqa: E402

results = {}


def check(section, ok, what):
	r = results.setdefault(section, [0, 0])
	r[0] += 1
	if not ok:
		r[1] += 1
		print('FAIL  {0}  {1}'.format(section, what))
	elif VERBOSE:
		print('ok    {0}  {1}'.format(section, what))


def legacy():
	spec = importlib.util.spec_from_file_location('legacy_ps', os.path.join(ROOT, 'texturetools', 'tests', 'legacy',
																			 'palette_swap.py'))
	m = importlib.util.module_from_spec(spec)
	spec.loader.exec_module(m)
	return m


def sprite(rng, colors, size=16, holes=0.15, partial=0.05):
	arr = np.zeros((size, size, 4), np.uint8)
	idx = rng.integers(0, len(colors), (size, size))
	for i, c in enumerate(colors):
		arr[idx == i, :3] = c
	arr[:, :, 3] = 255
	r = rng.random((size, size))
	arr[r < holes, 3] = 0
	arr[(r >= holes) & (r < holes + partial), 3] = 128
	return arr


def rand_colors(rng, k):
	return [tuple(int(v) for v in rng.integers(0, 256, 3)) for _ in range(k)]


def colors_of(arr):
	px = arr.reshape(-1, 4)
	return set(map(tuple, px[px[:, 3] > 0].tolist()))


def fixtures():
	rng = np.random.default_rng(1122)
	srcs, pals = {}, {}
	ramp = np.zeros((16, 16, 4), np.uint8)
	for i in range(8):
		ramp[i * 2:i * 2 + 2, :, :3] = 20 + i * 30
		ramp[i * 2:i * 2 + 2, :, 3] = 255
	srcs['ramp8'] = ramp
	for k in (3, 6, 12):
		srcs['rand%d' % k] = sprite(rng, rand_colors(rng, k))
	for k in (1, 2, 4, 9, 20):
		pals['p%d' % k] = sprite(rng, rand_colors(rng, k), holes=0.1, partial=0.1)
	real = sorted(glob.glob(os.path.join(ROOT, 'textureswapper', '*.png')))[:5]
	for p in real:
		a = np.array(Image.open(p).convert('RGBA'))
		srcs['r_' + os.path.basename(p)] = a
		pals['r_' + os.path.basename(p)] = a
	return srcs, pals


def outcome(fn, *a, **k):
	try:
		return fn(*a, **k), None
	except ValueError as e:
		return None, str(e)


def same(x, y):
	if x[1] or y[1]:
		return x[1] == y[1]
	return x[0].dtype == y[0].dtype and np.array_equal(x[0], y[0])


# ---------------------------------------------------------------- S1, S2

def s1_s2(srcs, pals, tmp):
	old = legacy()
	for pname, parr in pals.items():
		path = os.path.join(tmp, 'pal_' + pname + '.png')
		Image.fromarray(parr, 'RGBA').save(path)
		for invert, drops, dd, dl in itertools.product((False, True), ([], ['brown', 'white']), (0, 1), (0, 2)):
			_, lp, lc = old.load_palette_colors(path, invert, drops, dd, dl, with_counts=True)
			ep, ec = P.extract_palette(parr, invert, drops, dd, dl)
			check('S2 extract', lp == ep and lc == ec, 'extract_palette == load_palette_colors %s %s' % (pname, invert))
			for sname, sarr in srcs.items():
				for mode, interp in (('rank', 'off'), ('even', 'off'), ('rank', 'always'), ('even', 'always')):
					o = SwapOptions(mode=mode, interpolate=interp, invert=invert, drops=drops, drop_darkest=dd,
									drop_lightest=dl)
					x = outcome(old.palette_swap, sarr, lp, interp == 'always', mode == 'even', lc)
					y = outcome(S.swap_image, sarr, parr, o)
					check('S1 legacy', same(x, y), 'swap_image %s <- %s %s %s inv=%s drop=%s %d %d'
						  % (sname, pname, mode, interp, invert, drops, dd, dl))
	# alpha threshold
	pal = np.zeros((2, 3, 4), np.uint8)
	pal[0, 0] = (10, 10, 10, 255)
	pal[0, 1] = (200, 0, 0, 128)
	pal[0, 2] = (250, 250, 250, 255)
	pal[1, 0] = (90, 90, 90, 254)
	c1, _ = P.extract_palette(pal, alpha_min=1)
	c255, _ = P.extract_palette(pal, alpha_min=255)
	check('S2 alpha', len(c1) == 4 and len(c255) == 2 and all(c[3] == 255 for c in c255), 'palette_alpha 1 vs 255')
	# exclusion with tolerance
	cols = [(10, 10, 10), (12, 9, 14), (40, 40, 40), (200, 200, 200)]
	parr = np.array([[c + (255,) for c in cols]], np.uint8)
	e0, _ = P.extract_palette(parr, exclude=['#0a0a0a'])
	e4, _ = P.extract_palette(parr, exclude=['#0a0a0a'], exclude_tol=4)
	eall, _ = P.extract_palette(parr, exclude=['#000000'], exclude_tol=255)
	check('S2 exclude', len(e0) == 3 and len(e4) == 2 and len(eall) == 4, 'exclude tol 0 / 4 / all (kept when empty)')
	# HSV shift: 360 is identity, 120 turns red into green, re-sorted by brightness
	red = np.array([[(200, 0, 0, 255), (100, 0, 0, 255)]], np.uint8)
	h360, _ = P.extract_palette(red, hsv_shift=(360, 0, 0))
	h120, _ = P.extract_palette(red, hsv_shift=(120, 0, 0))
	check('S2 hsv', h360 == P.extract_palette(red)[0], 'hue +360 identity')
	check('S2 hsv', h120 == [(0, 100, 0, 255), (0, 200, 0, 255)], 'hue +120 red -> green %s' % (h120,))
	vdown, cnt = P.extract_palette(red, hsv_shift=(0, 0, -1))
	check('S2 hsv', vdown == [(0, 0, 0, 255)] and cnt[(0, 0, 0, 255)] == 2, 'value -1 merges colors and counts')
	check('S2 color', P.parse_color('#abc') == (170, 187, 204) and P.parse_color('1,2,3') == (1, 2, 3)
		  and P.parse_color('#0a0b0c') == (10, 11, 12), 'parse_color forms')


# ---------------------------------------------------------------- S3 modes

def lum(c):
	return float(S.rgb_to_lab(np.array(c[:3], float))[0])


def s3(srcs, pals):
	modes = S.MODES
	for sname, sarr in srcs.items():
		for pname, parr in pals.items():
			pal_colors = set(P.extract_palette(parr)[0])
			for mode in modes:
				o = SwapOptions(mode=mode)
				out = S.swap_image(sarr, parr, o)
				tr = sarr[:, :, 3] == 0
				check('S3 base', np.array_equal(out[tr], sarr[tr]), 'transparent untouched %s %s %s' % (mode, sname, pname))
				check('S3 base', colors_of(out) <= pal_colors, 'off: only palette colors %s %s %s' % (mode, sname, pname))
				# smaller: blends only when the palette has fewer colors than the source
				n_src = len(colors_of(sarr))
				sm = S.swap_image(sarr, parr, SwapOptions(mode=mode, interpolate='smaller'))
				ref = 'always' if 1 < len(pal_colors) < n_src else 'off'
				if mode != 'role' and (mode in ('rank', 'even') or ref == 'off'):   # role: per family pair
					rr = S.swap_image(sarr, parr, SwapOptions(mode=mode if mode != 'rank' or ref == 'off' else 'even',
															   interpolate=ref) if mode in ('rank', 'even') else
									  SwapOptions(mode=mode))
					check('S3 smaller', np.array_equal(sm, rr), 'smaller == %s %s %s %s' % (ref, mode, sname, pname))
				for space in S.SPACES:
					a0 = S.swap_image(sarr, parr, SwapOptions(mode=mode, interpolate='always', interp_amount=0,
															   interp_space=space))
					off = S.swap_image(sarr, parr, SwapOptions(mode=mode, interpolate='off'))
					if mode in ('rank',):
						off = S.swap_image(sarr, parr, SwapOptions(mode='even'))   # interpolate implies even ranks
					check('S3 amount', np.array_equal(a0, off), 'amount 0 == pick %s %s %s %s' % (mode, space, sname, pname))
	# even: a 3-level source on a 9-color monotonic gray palette takes the first, middle and last runs
	grays = [(v, v, v) for v in range(20, 255, 28)]
	pal = np.array([[g + (255,) for g in grays]], np.uint8)
	src = np.array([[(10, 10, 10, 255), (100, 100, 100, 255), (101, 101, 101, 255)]], np.uint8)
	ev = S.swap_image(src, pal, SwapOptions(mode='even'))
	check('S3 even', [tuple(p) for p in ev[0, :, :3]] == [grays[0], grays[4], grays[8]], 'even spreads 3 levels %s'
		  % ([tuple(p) for p in ev[0]],))
	rk = S.swap_image(src, pal, SwapOptions())
	check('S3 rank', [tuple(p) for p in rk[0, :, :3]] == [grays[0], grays[8], grays[8]], 'rank bunches by value')
	# nearest: a palette holding the source colors exactly returns the source
	rng = np.random.default_rng(5)
	s = sprite(rng, rand_colors(rng, 6), partial=0)
	near = S.swap_image(s, s, SwapOptions(mode='nearest'))
	check('S3 nearest', np.array_equal(near, s), 'nearest onto itself is identity')
	# ramp: output lightness follows source lightness (non-decreasing), invert reverses it
	shuffled = [grays[i] for i in (4, 0, 7, 2, 8, 1, 5, 3, 6)]
	pal_s = np.array([[g + (255,) for g in shuffled]], np.uint8)
	src8 = srcs['ramp8']
	for inv in (False, True):
		out = S.swap_image(src8, pal_s, SwapOptions(mode='ramp', stretch=True, invert=inv))
		ls = [lum(tuple(out[i * 2, 0])) for i in range(8)]
		mono = all(b >= a - 1e-6 for a, b in zip(ls, ls[1:])) if not inv else all(b <= a + 1e-6 for a, b in zip(ls, ls[1:]))
		check('S3 ramp', mono and abs(ls[0] - ls[-1]) > 50, 'ramp monotonic invert=%s %s' % (inv, [round(v) for v in ls]))
	blend = S.swap_image(src8, pal_s[:, :3], SwapOptions(mode='ramp', stretch=True, interpolate='always',
														   interp_space='lab'))
	check('S3 ramp', len(colors_of(blend)) == 8, 'ramp blend gives 8 shades from 3 palette colors')
	# role: gray body + red accent onto gray + blue palette -> body on grays, accent on blues
	body = [(60, 60, 60), (110, 110, 110), (170, 170, 170)]
	accent = [(150, 20, 20), (220, 40, 40)]
	src = np.array([[c + (255,) for c in body + accent] * 2], np.uint8)
	pgrays = [(40, 40, 45), (90, 90, 95), (140, 140, 145), (200, 200, 205)]
	pblues = [(20, 30, 140), (40, 60, 200), (90, 120, 240)]
	ppal = np.array([[c + (255,) for c in pgrays + pblues]], np.uint8)
	ro = S.swap_image(src, ppal, SwapOptions(mode='role'))
	got = [tuple(int(v) for v in p[:3]) for p in ro[0, :5]]
	check('S3 role', all(g in pgrays for g in got[:3]) and all(g in pblues for g in got[3:]),
		  'role body->grays accent->blues %s' % (got,))
	check('S3 role', got[0] != got[2] and got[3] != got[4], 'role keeps lightness order inside families')
	one = sprite(np.random.default_rng(3), [(120, 30, 30), (160, 40, 40), (200, 60, 60)], partial=0)
	check('S3 role', np.array_equal(S.swap_image(one, ppal, SwapOptions(mode='role')),
									S.swap_image(one, ppal, SwapOptions(mode='even'))), 'one-family source == even')
	fams = S.hue_families([(200, 0, 0, 255), (0, 200, 0, 255), (0, 0, 200, 255), (128, 128, 128, 255)])
	check('S3 role', [f[0] for f in fams] == ['gray', 'hue', 'hue', 'hue'], 'hue_families split at gaps')
	# lab round trip
	rgb = np.random.default_rng(9).integers(0, 256, (500, 3)).astype(float)
	err = np.abs(S.lab_to_rgb(S.rgb_to_lab(rgb)) - rgb).max()
	check('S3 lab', err < 0.01, 'lab round trip max err %.4f' % err)
	check('S3 mix', S.mix((0, 0, 0, 255), (255, 255, 255, 255), 0.5, 'hsv')[:3] == (128, 128, 128)
		  and S.mix((255, 0, 0, 255), (0, 0, 255, 255), 0.5, 'hsv')[:3] in ((255, 0, 255), (254, 0, 255)),
		  'hsv mix shortest hue path')


# ---------------------------------------------------------------- S4 knobs

def s4(srcs, pals):
	sarr, parr = srcs['rand12'], pals['p4']
	base = S.swap_image(sarr, parr)
	keep = sorted(colors_of(sarr))[:2]
	ko = S.swap_image(sarr, parr, SwapOptions(source_keep=[c[:3] for c in keep]))
	km = P.color_match_mask(sarr, [c[:3] for c in keep], 0) & (sarr[:, :, 3] > 0)
	check('S4 keep', km.any() and np.array_equal(ko[km], sarr[km]), 'kept colors untouched')
	check('S4 keep', not np.array_equal(ko[~km & (sarr[:, :, 3] > 0)], sarr[~km & (sarr[:, :, 3] > 0)]),
		  'others recolored')
	kt = S.swap_image(sarr, parr, SwapOptions(source_keep=['#000000'], source_keep_tol=255))
	check('S4 keep', np.array_equal(kt, sarr) or not (sarr[:, :, 3] > 0).any(), 'keep tol 255 keeps everything')
	op = sarr[:, :, 3] > 0
	p1 = S.swap_image(sarr, parr, SwapOptions(preserve=1.0))
	check('S4 preserve', np.array_equal(p1[op, :3], sarr[op, :3]), 'preserve 1 = source RGB')
	ph = S.swap_image(sarr, parr, SwapOptions(preserve=0.5))
	mid = np.round((base[op, :3].astype(float) + sarr[op, :3]) / 2)
	check('S4 preserve', np.abs(ph[op, :3] - mid).max() <= 1, 'preserve 0.5 = midpoint')
	al = S.swap_image(sarr, parr, SwapOptions(alpha='source'))
	check('S4 alpha', np.array_equal(al[:, :, 3], sarr[:, :, 3]) and np.array_equal(al[:, :, :3], base[:, :, :3]),
		  'alpha source keeps source alpha')
	check('S4 alpha', not np.array_equal(base[:, :, 3], sarr[:, :, 3]), 'alpha palette (legacy) takes palette alpha')
	# stretch: a narrow dark source on a full gray ramp reaches both ends with stretch, not without
	grays = np.array([[(v, v, v, 255) for v in range(0, 256, 32)]], np.uint8)
	dark = np.array([[(v, v, v, 255) for v in (20, 30, 40, 50)]], np.uint8)
	for mode in ('nearest', 'ramp'):
		ns = S.swap_image(dark, grays, SwapOptions(mode=mode))
		ys = S.swap_image(dark, grays, SwapOptions(mode=mode, stretch=True))
		check('S4 stretch', ns[0, :, 0].max() <= 64 and ys[0, 0, 0] == 0 and ys[0, -1, 0] >= 224,
			  'stretch %s %s -> %s' % (mode, ns[0, :, 0].tolist(), ys[0, :, 0].tolist()))
	# dither: 8 levels onto 2 colors; outputs only palette colors, the middle levels mix both
	two = np.array([[(0, 0, 0, 255), (255, 255, 255, 255)]], np.uint8)
	src = np.zeros((32, 32, 4), np.uint8)
	for i in range(8):
		src[:, i * 4:i * 4 + 4, :3] = 20 + i * 30
	src[:, :, 3] = 255
	for mode in ('rank', 'even', 'ramp'):
		d = S.swap_image(src, two, SwapOptions(mode=mode, dither='ordered', stretch=True))
		cols = colors_of(d)
		mixed = sum(1 for i in range(8) if len(colors_of(d[:, i * 4:i * 4 + 4])) == 2)
		check('S4 dither', cols <= {(0, 0, 0, 255), (255, 255, 255, 255)} and mixed >= 4,
			  'dither %s: palette only, %d of 8 columns mixed' % (mode, mixed))
	big = pals['p20']
	check('S4 dither', np.array_equal(S.swap_image(sarr, big, SwapOptions(dither='ordered')),
									  S.swap_image(sarr, big)), 'dither ignored for a larger palette')
	# every mode with every knob at once runs on every fixture
	n = 0
	for sname, s in srcs.items():
		for pname, p in pals.items():
			for mode in S.MODES:
				o = SwapOptions(mode=mode, interpolate='always', interp_space='lab', interp_amount=0.7, preserve=0.2,
								palette_alpha=255, hue_shift=30, sat_shift=0.1, val_shift=-0.1, stretch=True,
								alpha='source', dither='ordered', source_keep=['#000000'], source_keep_tol=20,
								palette_exclude=['#ffffff'], palette_exclude_tol=10, invert=True)
				r = outcome(S.swap_image, s, p, o)
				n += 1
				check('S4 all', r[1] is None or 'no opaque' in r[1], 'all knobs %s %s %s %s' % (mode, sname, pname, r[1]))
	check('S4 all', n > 0, 'ran %d all-knob swaps' % n)
	err = outcome(S.swap_image, srcs['rand3'], pals['p4'], SwapOptions(mode='bogus'))
	check('S4 validate', err[1] is not None and 'mode' in err[1], 'unknown mode rejected')
	rt = SwapOptions.from_dict(dict(SwapOptions(mode='role', preserve=0.3).to_dict(), junk=1))
	check('S4 dict', rt == SwapOptions(mode='role', preserve=0.3), 'to_dict / from_dict round trip')


# ---------------------------------------------------------------- S5 CLI

def run_cli(argv, cwd):
	out, err = io.StringIO(), io.StringIO()
	code = 0
	old = os.getcwd()
	os.chdir(cwd)
	try:
		with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
			try:
				cli.main(argv, 'in', 'out')
			except SystemExit as e:
				code = e.code or 0
	finally:
		os.chdir(old)
	return code, out.getvalue(), err.getvalue()


def s5(srcs, pals, tmp):
	d = tempfile.mkdtemp(dir=tmp)
	Image.fromarray(srcs['rand12'], 'RGBA').save(os.path.join(d, 's.png'))
	Image.fromarray(pals['p4'], 'RGBA').save(os.path.join(d, 'p.png'))
	Image.fromarray(pals['p20'], 'RGBA').save(os.path.join(d, 'p20.png'))
	read = lambda n: open(os.path.join(d, n), 'rb').read()
	run_cli(['s.png', 'p.png', 'a.png', '--even'], d)
	run_cli(['s.png', 'p.png', 'b.png', '--mode', 'even'], d)
	check('S5 cli', read('a.png') == read('b.png'), '--mode even == --even')
	run_cli(['s.png', 'p.png', 'c.png', '--interpolate'], d)
	run_cli(['s.png', 'p.png', 'e.png', '--interp', 'always'], d)
	check('S5 cli', read('c.png') == read('e.png'), '--interp always == --interpolate')
	o = SwapOptions(mode='ramp', interpolate='smaller', interp_amount=0.5, interp_space='lab', invert=True,
					drops=['brown'], drop_darkest=1, palette_alpha=200, palette_exclude=['#102030'],
					palette_exclude_tol=3, source_keep=['#000000', '#ffffff'], source_keep_tol=2, hue_shift=15.0,
					sat_shift=-0.2, val_shift=0.1, preserve=0.25, stretch=True, alpha='source', dither='ordered')
	code, out, err = run_cli(['s.png', 'p20.png', 'f.png'] + o.cli_args(), d)
	check('S5 cli', code == 0 and os.path.exists(os.path.join(d, 'f.png')) and 'Options:' in out, 'all flags run: %s' % err)
	ref = S.swap_image(srcs['rand12'], pals['p20'], o)
	check('S5 cli', np.array_equal(np.array(Image.open(os.path.join(d, 'f.png')).convert('RGBA')), ref),
		  'CLI output == swap_image with the same options')
	parser_args = cli.options_from_args
	import argparse
	ap = argparse.ArgumentParser()
	ap.add_argument('--invert', action='store_true')
	ap.add_argument('--even', action='store_true')
	ap.add_argument('--interpolate', action='store_true')
	ap.add_argument('--drop', type=cli.parse_drops, default=[])
	ap.add_argument('--drop-darkest', type=int, default=0)
	ap.add_argument('--drop-lightest', type=int, default=0)
	cli.add_mode_args(ap)
	back = parser_args(ap.parse_args(o.cli_args()))
	back.palette_exclude = [P.parse_color(c) for c in back.palette_exclude]
	back.source_keep = [P.parse_color(c) for c in back.source_keep]
	o2 = SwapOptions.from_dict(dict(o.to_dict(), palette_exclude=[P.parse_color(c) for c in o.palette_exclude],
									source_keep=[P.parse_color(c) for c in o.source_keep]))
	check('S5 cli', back == o2, 'cli_args round trip')
	code, out, err = run_cli(['s.png', 'p.png', 'g.png', '--preserve', '2'], d)
	check('S5 cli', code == 1 and 'preserve' in err, 'bad --preserve exits 1')
	code, out, err = run_cli(['s.png', 'p.png', 'g.png', '--keep-source', '#zz'], d)
	check('S5 cli', code == 2, 'bad color exits 2 (argparse)')
	os.makedirs(os.path.join(d, 'in'))
	for n in ('p.png', 'p20.png'):
		shutil.copy(os.path.join(d, n), os.path.join(d, 'in', n))
	code, out, err = run_cli(['s.png', '--bulk', '--mode', 'role', '--dither', 'ordered'], d)
	check('S5 bulk', code == 0 and sorted(os.listdir(os.path.join(d, 'out'))) == ['p.png', 'p20.png'],
		  'bulk with new flags: %s' % out[-200:])
	sf = os.path.join(d, 'sf.png')
	S.swap_file(os.path.join(d, 's.png'), os.path.join(d, 'p.png'), sf, SwapOptions(mode='nearest'), preview=True)
	check('S5 file', os.path.exists(sf) and os.path.exists(os.path.join(d, 'sf_preview.png')), 'swap_file + preview')
	outside = [p for p in glob.glob(os.path.join(ROOT, '*.png')) if os.path.getmtime(p) > START]
	check('S5 file', not outside, 'nothing written into the repo root')


START = 0


def main():
	global START
	import time
	START = time.time()
	srcs, pals = fixtures()
	tmp = tempfile.mkdtemp(prefix='tt_swap_')
	try:
		s1_s2(srcs, pals, tmp)
		s3(srcs, pals)
		s4(srcs, pals)
		s5(srcs, pals, tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	failed = 0
	for sec in sorted(results):
		n, bad = results[sec]
		print('{0}  {1}: {2} checks, {3} failed'.format('PASS' if not bad else 'FAIL', sec, n, bad))
		failed += bad
	print('texturetools swap modes: {0} failed'.format(failed))
	sys.exit(1 if failed else 0)


if __name__ == '__main__':
	main()

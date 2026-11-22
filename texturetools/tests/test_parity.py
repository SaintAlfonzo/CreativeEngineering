# SPDX-License-Identifier: GPL-3.0-only
"""Parity of texturetools against the frozen pre-move scripts (tests/legacy/, plan_texture_tools_gui.md section 10).

T1 swap parity: core palette functions and palette_swap() array-equal to the legacy ones on every fixture, palette
   option and mode flag (legacy, --even, --interpolate, both); CLI runs (single and --bulk, every flag) byte-identical.
T2 combine parity: alpha, add, multiply, paint (mask, strength, inset, clip), --lcm, --center, --batch naming; the
   engine array-equal, the CLI byte-identical.
T2b scale parity: scale_tex.py and upscale_sprites.py CLI runs byte-identical.

Every CLI case runs three entry points in a fresh copy of the fixture folder: the frozen legacy script, the new
texturetools/cli module and the textureswapper/ shim. Exit code, stdout, stderr and every file in the folder must match.

Fixtures are synthetic arrays (seeded) written to a temp dir, plus the top-level textureswapper/*.png sprites when
present, read only. Nothing is written into the repo.

Usage: python3 texturetools/tests/test_parity.py [-v]
"""
import contextlib
import glob
import importlib.util
import io
import os
import runpy
import shutil
import sys
import tempfile
import time

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)
LEGACY = os.path.join(ROOT, 'texturetools', 'tests', 'legacy')
SCRIPTS = ('palette_swap', 'combine_tex', 'scale_tex', 'upscale_sprites')
VERBOSE = '-v' in sys.argv

from texturetools.core import combine as new_combine  # noqa: E402
from texturetools.core import image_io as new_io  # noqa: E402
from texturetools.core import palette as new_palette  # noqa: E402
from texturetools.core import swap as new_swap  # noqa: E402

results = {}   # section -> [compared, mismatches]


def load_legacy(name):
	spec = importlib.util.spec_from_file_location('legacy_' + name, os.path.join(LEGACY, name + '.py'))
	m = importlib.util.module_from_spec(spec)
	spec.loader.exec_module(m)
	return m


def record(section, ok, what):
	r = results.setdefault(section, [0, 0])
	r[0] += 1
	if not ok:
		r[1] += 1
		print('MISMATCH  {0}  {1}'.format(section, what))
	elif VERBOSE:
		print('ok        {0}  {1}'.format(section, what))


def outcome(fn, *a, **k):
	"""(result, None) or (None, (exception type name, message))."""
	try:
		return fn(*a, **k), None
	except Exception as e:  # compare failures too
		return None, (type(e).__name__, str(e))


def same(a, b):
	if isinstance(a, np.ndarray) or isinstance(b, np.ndarray):
		return isinstance(a, np.ndarray) and isinstance(b, np.ndarray) and a.dtype == b.dtype and a.shape == b.shape \
			and np.array_equal(a, b, equal_nan=True)
	if isinstance(a, (tuple, list)) and isinstance(b, (tuple, list)):
		return type(a) == type(b) and len(a) == len(b) and all(same(x, y) for x, y in zip(a, b))
	if isinstance(a, Image.Image) or isinstance(b, Image.Image):
		return isinstance(a, Image.Image) and isinstance(b, Image.Image) and a.mode == b.mode and a.size == b.size \
			and a.tobytes() == b.tobytes()
	return a == b


# ---------------------------------------------------------------- fixtures

def rand_colors(rng, k):
	return [tuple(int(v) for v in rng.integers(0, 256, 3)) for _ in range(k)]


def sprite(rng, colors, size=16, holes=0.2, partial=0.05):
	idx = rng.integers(0, len(colors), (size, size))
	arr = np.zeros((size, size, 4), np.uint8)
	for i, c in enumerate(colors):
		arr[idx == i, :3] = c
	arr[:, :, 3] = 255
	r = rng.random((size, size))
	arr[r < holes, 3] = 0
	arr[(r >= holes) & (r < holes + partial), 3] = 128
	return arr


def make_fixtures(d):
	rng = np.random.default_rng(20261122)
	srcs, pals = {}, {}
	ramp = np.zeros((16, 16, 4), np.uint8)
	for i in range(6):
		ramp[2 + i * 2:4 + i * 2, 2:14, :3] = 30 + i * 40
		ramp[2 + i * 2:4 + i * 2, 2:14, 3] = 255
	srcs['s_ramp'] = ramp
	for k in (3, 8, 16):
		srcs['s_rand%d' % k] = sprite(rng, rand_colors(rng, k))
	dup = sprite(rng, [(90, 30, 0), (0, 30, 90), (30, 90, 0), (200, 10, 10), (10, 10, 200), (250, 250, 250)])
	srcs['s_dupbright'] = dup
	srcs['s_single'] = sprite(rng, [(120, 60, 30)], holes=0.3, partial=0)
	srcs['s_empty'] = np.zeros((8, 8, 4), np.uint8)
	srcs['s_wide'] = sprite(rng, rand_colors(rng, 10), size=24, holes=0.05)
	for k in (1, 2, 4, 7, 12, 24):
		pals['p%d' % k] = sprite(rng, rand_colors(rng, k), holes=0.1, partial=0)
	browns = [(120, 70, 20), (150, 90, 30), (90, 50, 10)]
	whites = [(240, 240, 235), (250, 250, 250)]
	pals['p_brownwhite'] = sprite(rng, browns + whites + [(30, 120, 200), (200, 40, 40)], holes=0)
	pals['p_allbrown'] = sprite(rng, browns, holes=0)
	pals['p_empty'] = np.zeros((8, 8, 4), np.uint8)
	paths = {}
	for name, arr in list(srcs.items()) + list(pals.items()):
		p = os.path.join(d, name + '.png')
		Image.fromarray(arr, 'RGBA').save(p)
		paths[name] = p
	# existing human sprites, read only (untracked images; skipped when absent)
	real = sorted(glob.glob(os.path.join(ROOT, 'textureswapper', '*.png')))[:6]
	for p in real:
		name = 'r_' + os.path.splitext(os.path.basename(p))[0]
		arr = np.array(Image.open(p).convert('RGBA'))
		srcs[name] = arr
		pals[name] = arr
		paths[name] = p
	return srcs, pals, paths, len(real)


# ---------------------------------------------------------------- T1 / T2 engine level

def t1_engine(srcs, paths, pals):
	old = load_legacy('palette_swap')
	for name, arr in srcs.items():
		record('T1 engine', same(old.get_colors_by_brightness(arr), new_palette.get_colors_by_brightness(arr)),
			   'get_colors_by_brightness ' + name)
		record('T1 engine', same(old.color_counts(arr), new_palette.color_counts(arr)), 'color_counts ' + name)
	opts = [dict(), dict(drops=['brown', 'white']), dict(drop_lightest=2), dict(drop_darkest=1, drop_lightest=1),
			dict(drop_darkest=99)]
	for pname in pals:
		for invert in (False, True):
			for o in opts:
				a = outcome(old.load_palette_colors, paths[pname], invert, with_counts=True, **o)
				b = outcome(new_palette.load_palette_colors, paths[pname], invert, with_counts=True, **o)
				record('T1 engine', same(a, b), 'load_palette_colors {0} invert={1} {2}'.format(pname, invert, o))
				if a[0] is None:
					continue
				_, pal, counts = a[0]
				for sname, sarr in srcs.items():
					for interpolate in (False, True):
						for even in (False, True):
							for use_counts in ((True, False) if interpolate or even else (True,)):
								c = counts if use_counts else None
								x = outcome(old.palette_swap, sarr, pal, interpolate, even, c)
								y = outcome(new_swap.palette_swap, sarr, pal, interpolate, even, c)
								record('T1 engine', same(x, y), 'palette_swap {0} <- {1} inv={2} {3} interp={4} even={5} '
									   'counts={6}'.format(sname, pname, invert, o, interpolate, even, use_counts))
	for t in (0, 0.25, 0.5, 0.99, 1):
		for pname in ('p2', 'p7'):
			pal = old.get_colors_by_brightness(pals[pname])
			record('T1 engine', same(old.lerp_palette(pal, t), new_palette.lerp_palette(pal, t)), 'lerp_palette %s %s' % (pname, t))


def t2_engine():
	old = load_legacy('combine_tex')
	rng = np.random.default_rng(7)

	def img(h, w):
		a = rng.integers(0, 256, (h, w, 4)).astype(np.float32)
		a[rng.random((h, w)) < 0.2, 3] = 0
		a[rng.random((h, w)) < 0.2, 3] = 255
		return a

	for trial in range(6):
		base, over = img(16, 16), img(16, 16)
		for mode in ('alpha', 'add', 'multiply'):
			record('T2 engine', same(old.MODES[mode](base, over), new_combine.MODES[mode](base, over)), '%s #%d' % (mode, trial))
		mask = rng.random((16, 16)) < 0.6
		for m in (None, mask, np.zeros((16, 16), bool)):
			for strength in (0.0, 0.5, 1.0):
				for inset in (0, 1, 3):
					for clip in (False, True):
						x = old.combine_paint(base, over, m, strength, inset, clip)
						y = new_combine.combine_paint(base, over, m, strength, inset, clip)
						record('T2 engine', same(x, y), 'paint #%d mask=%s s=%s inset=%d clip=%s' % (
							trial, None if m is None else int(m.sum()), strength, inset, clip))
	for bs, os_ in (((12, 12), (8, 8)), ((48, 48), (16, 16)), ((8, 4), (8, 8)), ((6, 9), (4, 6))):
		b, o = img(*bs), img(*os_)
		record('T2 engine', same(outcome(old.lcm_pair, b, o), outcome(new_combine.lcm_pair, b, o)), 'lcm_pair %s %s' % (bs, os_))
	for os_ in ((16, 16), (8, 8), (5, 7), (17, 16), (16, 3)):
		b, o = img(16, 16), img(*os_)
		for center in (False, True):
			record('T2 engine', same(old.fit_overlay(b, o, center), new_combine.fit_overlay(b, o, center)),
				   'fit_overlay %s center=%s' % (os_, center))
	d = tempfile.mkdtemp(prefix='tt_t2_')
	try:
		arr = img(9, 13)
		old.save_raw(arr, os.path.join(d, 'a.png'))
		new_io.save_raw(arr, os.path.join(d, 'b.png'))
		record('T2 engine', open(os.path.join(d, 'a.png'), 'rb').read() == open(os.path.join(d, 'b.png'), 'rb').read(),
			   'save_raw bytes')
	finally:
		shutil.rmtree(d)


# ---------------------------------------------------------------- CLI level

ENTRIES = (('cli', 'texturetools/cli/{0}.py'), ('shim', 'textureswapper/{0}.py'))


def snapshot(d):
	out = {}
	for dp, _, fs in os.walk(d):
		for f in fs:
			p = os.path.join(dp, f)
			with open(p, 'rb') as fh:
				out[os.path.relpath(p, d)] = fh.read()
	return out


def run_script(path, script, argv, fixture_dir, work_root):
	work = tempfile.mkdtemp(dir=work_root)
	shutil.copytree(fixture_dir, work, dirs_exist_ok=True)
	cwd, old_argv = os.getcwd(), sys.argv
	out, err = io.StringIO(), io.StringIO()
	code = 0
	try:
		os.chdir(work)
		sys.argv = [script + '.py'] + list(argv)
		with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
			try:
				runpy.run_path(path, run_name='__main__')
			except SystemExit as e:
				code = e.code
			except Exception as e:  # an uncaught error must match too
				code = ('raised', type(e).__name__, str(e))
	finally:
		os.chdir(cwd)
		sys.argv = old_argv
	norm = lambda t: t.replace(os.path.realpath(work), '<work>').replace(work, '<work>')
	return code, norm(out.getvalue()), norm(err.getvalue()), snapshot(work)


def cli_case(section, script, argv, fixture_dir, work_root):
	ref = run_script(os.path.join(LEGACY, script + '.py'), script, argv, fixture_dir, work_root)
	for label, rel in ENTRIES:
		got = run_script(os.path.join(ROOT, rel.format(script)), script, argv, fixture_dir, work_root)
		ok = got == ref
		what = '{0} {1} {2}'.format(label, script, ' '.join(argv))
		if not ok and VERBOSE:
			for i, n in enumerate(('exit', 'stdout', 'stderr')):
				if got[i] != ref[i]:
					print('  {0}: legacy {1!r} new {2!r}'.format(n, ref[i], got[i]))
			if got[3] != ref[3]:
				print('  files differ:', sorted(set(got[3]) ^ set(ref[3])) or
					  sorted(k for k in ref[3] if got[3].get(k) != ref[3][k]))
		record(section, ok, what)
	return ref


def write(d, name, arr):
	p = os.path.join(d, name)
	os.makedirs(os.path.dirname(p), exist_ok=True)
	Image.fromarray(arr.astype(np.uint8), 'RGBA').save(p)


def t1_cli(fixdir, work_root, srcs, n_real):
	pairs = [('s_rand8.png', 'p4.png'), ('s_ramp.png', 'p24.png'), ('s_dupbright.png', 'p_brownwhite.png'),
			 ('s_rand16.png', 'p2.png'), ('s_wide.png', 'p12.png')]
	real = sorted(n for n in os.listdir(fixdir) if n.startswith('r_'))
	if len(real) >= 2:
		pairs += [(real[0], real[1]), (real[-1], real[0])]
	flagsets = [[], ['--invert'], ['--even'], ['--interpolate'], ['--even', '--interpolate'],
				['--invert', '--even', '--preview'], ['--invert', '--interpolate'], ['--drop', 'brown,white'],
				['--drop', 'brown', '--interpolate'], ['--drop-darkest', '1', '--drop-lightest', '1'],
				['--drop-darkest', '2', '--even']]
	for src, pal in pairs:
		for fl in flagsets:
			cli_case('T1 cli', 'palette_swap', [src, pal, 'out/res.png'] + fl, fixdir, work_root)
	for argv in (['s_rand8.png', 'p4.png', 'res.jpg'], ['s_empty.png', 'p4.png', 'res.png'],
				 ['s_rand8.png', 'p_empty.png', 'res.png'], ['missing.png', 'p4.png', 'res.png'],
				 ['s_rand8.png', 'missing.png', 'res.png'], ['s_rand8.png', 'p4.png', 'res.png', '--drop', 'nope'],
				 ['s_single.png', 'p1.png', 'res.png', '--even'], ['s_rand8.png']):
		cli_case('T1 cli', 'palette_swap', argv, fixdir, work_root)
	# --bulk: ./in holds the palettes (a broken file and an empty one included), ./out is written
	bulk = tempfile.mkdtemp(dir=work_root)
	shutil.copy(os.path.join(fixdir, 's_ramp.png'), os.path.join(bulk, 'shape.png'))
	os.makedirs(os.path.join(bulk, 'in'))
	for n in ('p1', 'p4', 'p12', 'p_brownwhite', 'p_empty', 'p24'):
		shutil.copy(os.path.join(fixdir, n + '.png'), os.path.join(bulk, 'in', n + '.png'))
	with open(os.path.join(bulk, 'in', 'broken.png'), 'w') as fh:
		fh.write('not a png')
	with open(os.path.join(bulk, 'in', 'notes.txt'), 'w') as fh:
		fh.write('skip me')
	for fl in ([], ['--invert', '--even', '--preview'], ['--interpolate', '--drop', 'brown'], ['--drop-darkest', '1']):
		cli_case('T1 cli', 'palette_swap', ['shape.png', '--bulk'] + fl, bulk, work_root)
	cli_case('T1 cli', 'palette_swap', ['in/p4.png', '--bulk'], bulk, work_root)   # shape inside ./in is skipped
	cli_case('T1 cli', 'palette_swap', ['shape.png', '--bulk'], fixdir, work_root)  # no ./in: error


def t2_cli(work_root):
	rng = np.random.default_rng(11)
	d = tempfile.mkdtemp(dir=work_root)

	def img(h, w, holes=0.2):
		a = rng.integers(0, 256, (h, w, 4))
		a[rng.random((h, w)) < holes, 3] = 0
		a[rng.random((h, w)) < 0.2, 3] = 255
		return a

	write(d, 'base.png', img(16, 16))
	write(d, 'over.png', img(16, 16))
	write(d, 'small.png', img(5, 7))
	write(d, 'big.png', img(20, 20))
	write(d, 'b12.png', img(12, 12))
	write(d, 'o8.png', img(8, 8))
	write(d, 'tall.png', img(8, 4))
	mask = img(16, 16, holes=0.5)
	write(d, 'mask.png', mask)
	write(d, 'mask_bad.png', img(8, 8))
	for name, (h, w) in (('apple1', (16, 16)), ('pear', (8, 8)), ('plum', (5, 7)), ('huge', (32, 32)),
						 ('flat', (16, 8))):
		write(d, os.path.join('labels', name + '.png'), img(h, w))
	Image.fromarray(img(16, 16)[:, :, :3].astype(np.uint8), 'RGB').save(os.path.join(d, 'labels', 'photo.jpg'))
	with open(os.path.join(d, 'labels', 'readme.txt'), 'w') as fh:
		fh.write('not an image')
	os.makedirs(os.path.join(d, 'labels', 'sub.png'))
	for mode in ('alpha', 'add', 'multiply', 'paint'):
		cli_case('T2 cli', 'combine_tex', ['base.png', 'over.png', 'res.png', '--mode', mode], d, work_root)
	for fl in (['--mask', 'mask.png'], ['--mask', 'mask.png', '--strength', '0'], ['--strength', '0.5', '--inset', '1'],
			   ['--inset', '2', '--clip'], ['--mask', 'mask.png', '--clip', '--strength', '0.25'],
			   ['--mask', 'mask_bad.png']):
		cli_case('T2 cli', 'combine_tex', ['base.png', 'over.png', 'res.png', '--mode', 'paint'] + fl, d, work_root)
	for argv in (['base.png', 'small.png', 'res.png', '--center'], ['base.png', 'small.png', 'res.png'],
				 ['base.png', 'big.png', 'res.png', '--center'], ['b12.png', 'o8.png', 'res.png', '--lcm'],
				 ['b12.png', 'o8.png', 'res.png', '--lcm', '--mode', 'multiply'], ['tall.png', 'o8.png', 'res.png', '--lcm'],
				 ['o8.png', 'small.png', 'res.png', '--lcm', '--center'], ['missing.png', 'over.png', 'res.png'],
				 ['base.png', 'missing.png', 'res.png'], ['base.png', 'over.png', 'res.png', '--mode', 'nope']):
		cli_case('T2 cli', 'combine_tex', argv, d, work_root)
	for fl in ([], ['--pattern', 'canned_{name}.png'], ['--pattern', '{base}_{name}.{ext}'], ['--center'],
			   ['--lcm'], ['--lcm', '--center', '--pattern', 'x_{name}_{base}.png'], ['--mode', 'add', '--center'],
			   ['--mode', 'paint', '--mask', 'mask.png', '--strength', '0'], ['--mode', 'multiply']):
		cli_case('T2 cli', 'combine_tex', ['--batch', 'base.png', 'labels', 'outdir'] + fl, d, work_root)
	cli_case('T2 cli', 'combine_tex', ['--batch', 'base.png', 'nolabels', 'outdir'], d, work_root)


def t2b_cli(work_root):
	rng = np.random.default_rng(5)
	d = tempfile.mkdtemp(dir=work_root)
	a = np.zeros((16, 16, 4), np.uint8)
	a[3:11, 4:13] = rng.integers(0, 256, (8, 9, 4))
	a[3:11, 4:13, 3] = 255
	write(d, 'icon.png', a)
	write(d, 'rect.png', rng.integers(0, 256, (8, 12, 4)))
	pimg = Image.fromarray(rng.integers(0, 4, (8, 8)).astype(np.uint8), 'P')
	pimg.putpalette([0, 0, 0, 255, 0, 0, 0, 255, 0, 0, 0, 255] + [0] * 756)
	pimg.save(os.path.join(d, 'pal.png'), transparency=0)
	Image.fromarray(rng.integers(0, 256, (8, 8, 3)).astype(np.uint8), 'RGB').save(os.path.join(d, 'rgb.png'))
	for argv in (['icon.png', '--factor', '2'], ['icon.png', 'big.png', '--factor', '4'], ['icon.png', '--size', '32'],
				 ['rect.png', '--size', '24x16'], ['rect.png', '--size', '24x24'], ['icon.png', '--size', '20'],
				 ['icon.png', '--size', '8'], ['icon.png', '--trim', '--canvas', '40', '--anchor', 'center'],
				 ['icon.png', '--trim', '--canvas', '40'], ['icon.png', '--canvas', '50x40'], ['icon.png', '--canvas', '8'],
				 ['icon.png', '--trim', '--factor', '3'], ['pal.png', '--factor', '3'], ['pal.png', '--canvas', '30'],
				 ['rgb.png', '--size', '16'], ['icon.png'], ['icon.png', '--factor', '0'], ['missing.png', '--factor', '2'],
				 ['icon.png', '--factor', '2', '--size', '32'], ['icon.png', '--size', 'axb']):
		cli_case('T2b cli', 'scale_tex', argv, d, work_root)
	u = tempfile.mkdtemp(dir=work_root)
	write(u, 'sprites/a.png', rng.integers(0, 256, (4, 4, 4)))
	write(u, 'sprites/sub/b.png', rng.integers(0, 256, (3, 5, 4)))
	shutil.copy(os.path.join(d, 'pal.png'), os.path.join(u, 'sprites', 'p.png'))
	shutil.copy(os.path.join(d, 'rgb.png'), os.path.join(u, 'sprites', 'c.png'))
	with open(os.path.join(u, 'sprites', 'bad.png'), 'w') as fh:
		fh.write('broken')
	os.makedirs(os.path.join(u, 'empty'))
	for argv in (['sprites', '--factors', '2', '3'], ['sprites', 'out', '--factors', '2', '--recursive'],
				 ['sprites', 'out', '--flat', '--factors', '4', '2', '2'], ['sprites', '--recursive', '--flat', '--factors', '3'],
				 ['sprites', '--factors', '1'], ['empty'], ['missing']):
		cli_case('T2b cli', 'upscale_sprites', argv, u, work_root)


def main():
	work_root = tempfile.mkdtemp(prefix='texturetools_parity_')
	try:
		fixdir = os.path.join(work_root, 'fixtures')
		os.makedirs(fixdir)
		srcs, pals, paths, n_real = make_fixtures(fixdir)
		# real sprites join the CLI fixture folder as copies (the originals are only read)
		for name, p in paths.items():
			if name.startswith('r_'):
				shutil.copy(p, os.path.join(fixdir, name + '.png'))
		for step in (lambda: t1_engine(srcs, paths, pals), t2_engine, lambda: t1_cli(fixdir, work_root, srcs, n_real),
					 lambda: t2_cli(work_root), lambda: t2b_cli(work_root)):
			t0 = time.time()
			step()
			if VERBOSE:
				print('step {0:.1f} s'.format(time.time() - t0))
	finally:
		shutil.rmtree(work_root, ignore_errors=True)
	bad = 0
	for section in sorted(results):
		n, m = results[section]
		bad += m
		print('{0}  {1}: {2} compared, {3} mismatches'.format('PASS' if not m else 'FAIL', section, n, m))
	print('real sprites used read-only: {0}'.format(n_real))
	print('texturetools parity: {0} failed'.format(bad))
	return 1 if bad else 0


if __name__ == '__main__':
	sys.exit(main())

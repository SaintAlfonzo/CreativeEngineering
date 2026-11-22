# SPDX-License-Identifier: GPL-3.0-only
"""Painter engine (plan_texture_tools_gui.md section 5, P3) on synthetic shapes.

P1 default PaintOptions equal the frozen legacy combine_paint (strength, inset, clip, mask) on every fixture
P2 sampling: offset with tile, flips and rotations, integer scale, inset, material alpha skip
P3 region: outline keep, only-colors, keep-colors, mask and mask invert, transparent base untouched
P4 projected: stretch, contain and cover fits keep the material in one piece across the bounding box
P5 files: paint_file writes only into the temp dir; options validate and round trip

Synthetic arrays only (seeded); the top-level textureswapper/*.png sprites read only; outputs in a temp dir.
Usage: python3 texturetools/tests/test_paint.py [-v]
"""
import glob
import importlib.util
import os
import shutil
import sys
import tempfile

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)
VERBOSE = '-v' in sys.argv

from texturetools.core import paint as PT  # noqa: E402
from texturetools.core.paint import PaintOptions  # noqa: E402

results = {}


def check(section, ok, what):
	r = results.setdefault(section, [0, 0])
	r[0] += 1
	if not ok:
		r[1] += 1
		print('FAIL  {0}  {1}'.format(section, what))
	elif VERBOSE:
		print('ok    {0}  {1}'.format(section, what))


def legacy_paint():
	spec = importlib.util.spec_from_file_location('legacy_ct', os.path.join(ROOT, 'texturetools', 'tests', 'legacy',
																			 'combine_tex.py'))
	m = importlib.util.module_from_spec(spec)
	spec.loader.exec_module(m)
	return m.combine_paint


def blob(size=16, r=6, rng=None):
	"""A round opaque shape with two gray shades, transparent outside."""
	yy, xx = np.mgrid[0:size, 0:size]
	c = (size - 1) / 2.0
	inside = (yy - c) ** 2 + (xx - c) ** 2 <= r * r
	arr = np.zeros((size, size, 4), np.float32)
	arr[inside, :3] = 140
	arr[inside & (yy < c), :3] = 200
	arr[inside, 3] = 255
	if rng is not None:
		arr[inside, :3] += rng.integers(-20, 20, (int(inside.sum()), 1))
	return arr


def material(size=16, seed=1):
	rng = np.random.default_rng(seed)
	m = rng.integers(0, 256, (size, size, 4)).astype(np.float32)
	m[:, :, 3] = 255
	return m


def p1():
	old = legacy_paint()
	rng = np.random.default_rng(11)
	shapes = [blob(16, 6, rng), blob(16, 8), blob(32, 13, rng)]
	real = sorted(glob.glob(os.path.join(ROOT, 'textureswapper', '*.png')))[:4]
	shapes += [np.array(Image.open(p).convert('RGBA')).astype(np.float32) for p in real]
	for i, base in enumerate(shapes):
		mat = material(base.shape[0], seed=i)
		for strength in (0.0, 0.5, 1.0):
			for inset in (0, 1, 2):
				for clip in (False, True):
					for use_mask in (False, True):
						mask = None
						if use_mask:
							mask = np.zeros(base.shape[:2], bool)
							mask[: base.shape[0] // 2] = True
						a = old(base, mat, mask, strength, inset, clip)
						b = PT.paint(base, mat, PaintOptions(strength=strength, inset=inset, clip=clip), mask)
						check('P1 legacy', a.dtype == b.dtype and np.array_equal(a, b),
							  'paint == combine_paint shape %d s=%s inset=%d clip=%s mask=%s'
							  % (i, strength, inset, clip, use_mask))


def flat_base(size=8):
	b = np.zeros((size, size, 4), np.float32)
	b[:, :, :3] = 100
	b[:, :, 3] = 255
	return b


def p2():
	base = flat_base(8)
	mat = material(8, 3)
	plain = PaintOptions(strength=0.0)
	out = PT.paint(base, mat, plain)
	check('P2 sample', np.array_equal(out[:, :, :3], mat[:, :, :3]), 'strength 0 on a full base = material')
	for dx, dy in ((8, 0), (0, 8), (-16, 24)):
		o = PT.paint(base, mat, PaintOptions(strength=0.0, tile=True, offset_x=dx, offset_y=dy))
		check('P2 tile', np.array_equal(o, out), 'tile offset by a material size (%d, %d) is identity' % (dx, dy))
	o = PT.paint(base, mat, PaintOptions(strength=0.0, tile=True, offset_x=3, offset_y=1))
	check('P2 tile', np.array_equal(o[:, :, :3], np.roll(mat, (1, 3), axis=(0, 1))[:, :, :3]), 'tile offset = roll')
	o = PT.paint(base, mat, PaintOptions(strength=0.0, offset_x=2))
	check('P2 clamp', np.array_equal(o[:, :2, :3], np.repeat(mat[:, :1, :3], 2, axis=1)), 'no tile clamps the edge')
	for fh, fv, rot, ref in ((True, False, 0, mat[:, ::-1]), (False, True, 0, mat[::-1]),
							 (False, False, 90, np.rot90(mat, -1)), (False, False, 180, np.rot90(mat, 2)),
							 (False, False, 270, np.rot90(mat, 1)), (True, True, 180, mat)):
		o = PT.paint(base, mat, PaintOptions(strength=0.0, flip_h=fh, flip_v=fv, rotate=rot))
		check('P2 transform', np.array_equal(o[:, :, :3], ref[:, :, :3]), 'flip_h=%s flip_v=%s rotate=%d' % (fh, fv, rot))
	m = mat
	for _ in range(4):
		m = PT.transform_material(m, rotate=90)
	check('P2 transform', np.array_equal(m, mat), 'rotate 90 x4 = identity')
	check('P2 transform', np.array_equal(PT.transform_material(PT.transform_material(mat, True, True), True, True), mat),
		  'flip twice = identity')
	o = PT.paint(flat_base(16), mat, PaintOptions(strength=0.0, scale=2))
	check('P2 scale', np.array_equal(o[:, :, :3], np.repeat(np.repeat(mat, 2, 0), 2, 1)[:, :, :3]), 'scale 2 = 2x2 blocks')
	o = PT.paint(base, mat, PaintOptions(strength=0.0, inset=1))
	check('P2 inset', np.array_equal(o[0, 0, :3], mat[1, 1, :3]) and np.array_equal(o[7, 7, :3], mat[6, 6, :3]),
		  'inset 1 samples inside the edge')
	holes = mat.copy()
	holes[::2, :, 3] = 0
	o = PT.paint(base, holes, PaintOptions(strength=0.0, material_alpha='skip'))
	check('P2 alpha', np.array_equal(o[::2], base[::2]) and np.array_equal(o[1::2, :, :3], mat[1::2, :, :3]),
		  'material_alpha skip leaves the shape under transparent material')
	o = PT.paint(base, holes, PaintOptions(strength=0.0))
	check('P2 alpha', np.array_equal(o[:, :, 3], base[:, :, 3]), 'material_alpha ignore keeps base alpha (legacy)')


def p3():
	base = blob(16, 6)
	mat = material(16, 5)
	op = base[:, :, 3] > 0
	edge = PT.outline_mask(base)
	check('P3 outline', edge.any() and (edge & ~op).sum() == 0 and (op & ~edge).any(), 'outline is the opaque rim')
	o = PT.paint(base, mat, PaintOptions(outline='keep'))
	check('P3 outline', np.array_equal(o[edge], base[edge]) and not np.array_equal(o[op & ~edge], base[op & ~edge]),
		  'outline keep leaves the rim, paints inside')
	check('P3 base', np.array_equal(o[~op], base[~op]), 'transparent base pixels untouched')
	full = flat_base(4)
	check('P3 outline', PT.outline_mask(full).sum() == 12, 'image edge counts as outline (4x4 -> 12)')
	top = base[:, :, 0] >= 200
	o = PT.paint(base, mat, PaintOptions(only_colors=['#c8c8c8']))
	check('P3 only', np.array_equal(o[op & ~top], base[op & ~top]) and not np.array_equal(o[top], base[top]),
		  'only-colors paints just the light half')
	o2 = PT.paint(base, mat, PaintOptions(keep_colors=['#c8c8c8']))
	check('P3 keep', np.array_equal(o2[top], base[top]) and not np.array_equal(o2[op & ~top], base[op & ~top]),
		  'keep-colors leaves the light half')
	o3 = PT.paint(base, mat, PaintOptions(keep_colors=['#8c8c8c'], keep_tol=70))
	check('P3 keep', np.array_equal(o3, base), 'keep tolerance covering both shades leaves everything')
	mask = np.zeros(base.shape[:2], bool)
	mask[:, :8] = True
	om = PT.paint(base, mat, PaintOptions(), mask)
	oi = PT.paint(base, mat, PaintOptions(mask_invert=True), mask)
	check('P3 mask', np.array_equal(om[:, 8:], base[:, 8:]) and np.array_equal(oi[:, :8], base[:, :8]),
		  'mask and inverted mask paint opposite halves')
	check('P3 mask', np.array_equal(oi[~op], base[~op]), 'inverted mask never paints transparent pixels')
	rgba_mask = np.zeros(base.shape, np.float32)
	rgba_mask[:, :8, 3] = 255
	check('P3 mask', np.array_equal(PT.paint(base, mat, PaintOptions(), rgba_mask), om), 'RGBA mask = bool mask')


def p4():
	base = np.zeros((16, 16, 4), np.float32)
	base[4:12, 2:10, :3] = 120
	base[4:12, 2:10, 3] = 255
	mat = material(4, 9)
	o = PT.paint(base, mat, PaintOptions(mode='projected', strength=0.0))
	ref = np.repeat(np.repeat(mat, 2, 0), 2, 1)
	check('P4 stretch', np.array_equal(o[4:12, 2:10, :3], ref[:, :, :3]), 'stretch: 4x4 material over an 8x8 box')
	check('P4 stretch', np.array_equal(o[:4], base[:4]) and np.array_equal(o[:, 10:], base[:, 10:]), 'outside untouched')
	wide = np.zeros((16, 16, 4), np.float32)
	wide[6:10, 0:16, :3] = 120
	wide[6:10, 0:16, 3] = 255
	cov = PT.paint(wide, mat, PaintOptions(mode='projected', fit='cover', strength=0.0))
	exp = np.repeat(mat[[1, 1, 2, 2]], 4, axis=1)   # scale 4, centered: box rows take material rows 1, 1, 2, 2
	check('P4 cover', np.array_equal(cov[6:10, :, :3], exp[:, :, :3]), 'cover: 4x scale, centered, rows cropped')
	con = PT.paint(wide, mat, PaintOptions(mode='projected', fit='contain', strength=0.0))
	block = con[6:10, 6:10, :3]
	check('P4 contain', np.array_equal(block, mat[:, :, :3]), 'contain: 4x4 material centered 1:1 in a 16x4 box')
	big = material(32, 4)
	o = PT.paint(base, big, PaintOptions(mode='projected', strength=0.0))
	check('P4 stretch', np.array_equal(o[4:12, 2:10, :3], big[::4, ::4, :3]), 'stretch down: 32 -> 8 nearest')
	shp = blob(16, 6)
	o = PT.paint(shp, mat, PaintOptions(mode='projected'))
	check('P4 shape', np.array_equal(o[:, :, 3], shp[:, :, 3]), 'projected keeps the shape alpha')
	o = PT.paint(base, mat, PaintOptions(mode='projected', strength=0.0, offset_x=2))
	check('P4 offset', np.array_equal(o[4:12, 4:10, :3], ref[:, :6, :3]), 'projected offset shifts the material')


def p5():
	tmp = tempfile.mkdtemp(prefix='tt_paint_')
	try:
		b = os.path.join(tmp, 'b.png')
		m = os.path.join(tmp, 'm.png')
		Image.fromarray(blob(16, 6).astype(np.uint8), 'RGBA').save(b)
		Image.fromarray(material(16, 2).astype(np.uint8), 'RGBA').save(m)
		out = PT.paint_file(b, m, os.path.join(tmp, 'o.png'), PaintOptions(mode='projected', outline='keep'))
		check('P5 file', os.path.exists(out) and sorted(os.listdir(tmp)) == ['b.png', 'm.png', 'o.png'], 'paint_file')
		mk = os.path.join(tmp, 'mask.png')
		Image.fromarray(np.full((16, 16, 4), 255, np.uint8), 'RGBA').save(mk)
		PT.paint_file(b, m, os.path.join(tmp, 'o2.png'), None, mk)
		check('P5 file', os.path.exists(os.path.join(tmp, 'o2.png')), 'paint_file with a mask')
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	for bad in (dict(mode='x'), dict(rotate=45), dict(scale=0), dict(strength=2), dict(fit='zoom')):
		try:
			PaintOptions(**bad).validate()
			ok = False
		except ValueError:
			ok = True
		check('P5 validate', ok, 'rejects %s' % bad)
	o = PaintOptions(mode='projected', fit='cover', keep_colors=['#000000'], rotate=90)
	check('P5 dict', PaintOptions.from_dict(dict(o.to_dict(), junk=1)) == o, 'to_dict / from_dict round trip')
	try:
		PT.paint(blob(16), material(16), None, np.zeros((8, 8), bool))
		ok = False
	except ValueError:
		ok = True
	check('P5 validate', ok, 'mask size mismatch rejected')


def main():
	p1()
	p2()
	p3()
	p4()
	p5()
	failed = 0
	for sec in sorted(results):
		n, bad = results[sec]
		print('{0}  {1}: {2} checks, {3} failed'.format('PASS' if not bad else 'FAIL', sec, n, bad))
		failed += bad
	print('texturetools paint: {0} failed'.format(failed))
	sys.exit(1 if failed else 0)


if __name__ == '__main__':
	main()

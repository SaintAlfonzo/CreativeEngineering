# SPDX-License-Identifier: GPL-3.0-only
"""P2 combine layer stack tests (plan_texture_tools_gui.md sections 4, 10).

Two-layer parity: combine_stack with two layers equals the frozen old combine_tex (tests/legacy/) array for array,
dtype included, in every old mode, paint options, --center and --lcm. N-layer properties: fold order, opacity,
visibility, offset clip / wrap / grow, anchor, canvas, background, resize, trim, mask and inverted mask, the new
modes' defining identities, preset round trip and cartesian job counts. Synthetic arrays only; files in a temp dir.
"""

import importlib.util
import os
import shutil
import sys
import tempfile

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)

from texturetools.core import batch  # noqa: E402
from texturetools.core import combine as C  # noqa: E402
from texturetools.core.combine import Layer, combine_stack  # noqa: E402

spec = importlib.util.spec_from_file_location("legacy_combine_tex",
											  os.path.join(ROOT, "texturetools", "tests", "legacy", "combine_tex.py"))
old = importlib.util.module_from_spec(spec)
spec.loader.exec_module(old)

results = [0, 0]


def check(ok, what):
	results[0] += 1
	if not ok:
		results[1] += 1
		print("FAIL  " + what)


def same(a, b):
	return a.dtype == b.dtype and a.shape == b.shape and np.array_equal(a, b)


def close(a, b, tol=1e-3):
	return a.shape == b.shape and np.allclose(a, b, atol=tol)


rng = np.random.default_rng(2026)


def img(h, w, holes=0.25, opaque=False):
	a = rng.integers(0, 256, (h, w, 4)).astype(np.float32)
	if opaque:
		a[:, :, 3] = 255
	else:
		a[rng.random((h, w)) < holes, 3] = 0
		a[rng.random((h, w)) < 0.25, 3] = 255
	return a


def write(d, name, arr):
	p = os.path.join(d, name)
	os.makedirs(os.path.dirname(p), exist_ok=True)
	Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA").save(p)
	return p


def parity(tmp):
	for trial in range(12):
		b, o = img(16, 16), img(16, 16)
		for mode in ("alpha", "add", "multiply", "paint"):
			check(same(old.MODES[mode](b, o), combine_stack([Layer(b), Layer(o, mode=mode)])), "2-layer %s #%d" % (mode, trial))
			check(same(old.combine(mode, b, o, None), C.combine(mode, b, o, None)), "combine() %s #%d" % (mode, trial))
		mpath = write(tmp, "m%d.png" % trial, img(16, 16, holes=0.5))
		for strength in (0.0, 0.5, 1.0):
			for inset in (0, 1, 3):
				for clip in (False, True):
					for mask in (None, mpath):
						po = dict(mask=mask, strength=strength, inset=inset, clip=clip)
						got = combine_stack([Layer(b), Layer(o, mode="paint", mask=mask, strength=strength, inset=inset,
															 clip=clip)])
						check(same(old.combine("paint", b, o, po), got), "paint %r #%d" % (po, trial))
		small = img(5, 7)
		for mode in ("alpha", "add", "multiply"):
			ref = old.combine(mode, b, old.center_overlay(b, small), None)
			check(same(ref, combine_stack([Layer(b), Layer(small, mode=mode, anchor="center")], fit="contain")),
				  "center %s #%d" % (mode, trial))
		b12, o8 = img(12, 12), img(8, 8)
		pb, po_, _ = old.lcm_pair(b12, o8)
		check(same(old.MODES["multiply"](pb, po_), combine_stack([Layer(b12), Layer(o8, mode="multiply")], lcm=True)),
			  "lcm #%d" % trial)
	try:
		combine_stack([Layer(img(16, 16)), Layer(img(5, 7))], fit="same")
		check(False, "fit same raises on a size mismatch")
	except C.SizeMismatch:
		check(True, "")
	try:
		combine_stack([Layer(img(16, 16)), Layer(img(20, 20), anchor="center")], fit="contain")
		check(False, "fit contain raises on a larger layer")
	except C.SizeMismatch:
		check(True, "")
	try:
		combine_stack([Layer(img(8, 4)), Layer(img(8, 8))], lcm=True)
		check(False, "lcm raises on uneven factors")
	except ValueError:
		check(True, "")


def properties():
	for trial in range(10):
		a, b, c = img(12, 12), img(12, 12), img(12, 12)
		# fold order: three layers = two nested two-layer combines
		for m1, m2 in (("alpha", "multiply"), ("add", "screen"), ("max", "alpha"), ("overlay", "subtract")):
			ab = combine_stack([Layer(a), Layer(b, mode=m1)])
			check(close(combine_stack([Layer(a), Layer(b, mode=m1), Layer(c, mode=m2)]),
						combine_stack([Layer(ab), Layer(c, mode=m2)])), "fold %s %s #%d" % (m1, m2, trial))
		# hidden layer = no layer = opacity 0 (alpha and W3C modes)
		ab = combine_stack([Layer(a), Layer(b)])
		check(same(ab, combine_stack([Layer(a), Layer(c, visible=False), Layer(b)])), "hidden layer #%d" % trial)
		check(same(combine_stack([Layer(a), Layer(c, visible=False)]), combine_stack([Layer(a)])), "hidden top #%d" % trial)
		for mode in ("alpha", "screen", "min", "add", "multiply"):
			check(close(combine_stack([Layer(a), Layer(b, mode=mode, opacity=0.0)]), combine_stack([Layer(a)])),
				  "opacity 0 %s #%d" % (mode, trial))
		# alpha opacity = overlay alpha scaled
		b2 = b.copy()
		b2[:, :, 3] *= 0.4
		check(close(combine_stack([Layer(a), Layer(b, opacity=0.4)]), old.combine_alpha(a, b2)), "alpha opacity #%d" % trial)
		# W3C normal (alpha) identity: the w3c formula with B = Cs equals combine_alpha
		check(close(C._w3c_blend(a, b, lambda cb, cs: cs), old.combine_alpha(a, b), 1e-2), "w3c normal #%d" % trial)
		# new modes on opaque layers are the plain per-channel formulas
		oa, ob = img(6, 6, opaque=True), img(6, 6, opaque=True)
		x, y = oa[:, :, :3] / 255.0, ob[:, :, :3] / 255.0
		for mode, ref in (("min", np.minimum(x, y)), ("max", np.maximum(x, y)), ("screen", 1 - (1 - x) * (1 - y)),
						  ("subtract", np.clip(x - y, 0, 1)),
						  ("overlay", np.where(x <= 0.5, 2 * x * y, 1 - 2 * (1 - x) * (1 - y)))):
			got = combine_stack([Layer(oa), Layer(ob, mode=mode)])
			check(close(got[:, :, :3], ref * 255, 1e-2) and close(got[:, :, 3], np.full((6, 6), 255.0)),
				  "opaque %s #%d" % (mode, trial))
		black = np.zeros((6, 6, 4), np.float32)
		black[:, :, 3] = 255
		check(close(combine_stack([Layer(oa), Layer(black, mode="screen")]), oa, 1e-2), "screen black #%d" % trial)
		check(close(combine_stack([Layer(oa), Layer(oa, mode="subtract")])[:, :, :3], np.zeros((6, 6, 3))),
			  "subtract self #%d" % trial)
		check(close(combine_stack([Layer(oa), Layer(ob, mode="max")]), combine_stack([Layer(ob), Layer(oa, mode="max")])),
			  "max symmetric #%d" % trial)
		# transparent overlay leaves the base for the W3C modes
		clear = np.zeros((6, 6, 4), np.float32)
		for mode in C.NEW_MODES:
			check(close(combine_stack([Layer(oa), Layer(clear, mode=mode)]), oa, 1e-2), "clear %s #%d" % (mode, trial))
		# offsets
		o = img(12, 12, opaque=True)
		base_clear = np.zeros((12, 12, 4), np.float32)
		for dx, dy in ((3, 0), (0, -5), (-4, 7), (12, 12)):
			got = combine_stack([Layer(base_clear), Layer(o, offset=(dx, dy), offset_mode="wrap")])
			check(close(got, np.roll(np.roll(o, dy, axis=0), dx, axis=1)), "wrap %d,%d #%d" % (dx, dy, trial))
		got = combine_stack([Layer(base_clear), Layer(o, offset=(5, 2))])
		check(close(got[2:, 5:], o[:10, :7]) and not got[:2].any() and not got[:, :5].any(), "clip offset #%d" % trial)
		got, lay = combine_stack([Layer(base_clear), Layer(o, offset=(-3, 4), offset_mode="grow")], return_layout=True)
		check(got.shape == (16, 15, 4) and lay["origin"] == (3, 0) and close(got[4:, :12], o), "grow #%d" % trial)
		# canvas and anchor
		s = img(4, 6, opaque=True)
		got = combine_stack([Layer(s), Layer(img(10, 8, opaque=True), visible=False)], canvas="largest")
		check(got.shape == (4, 6, 4), "largest ignores hidden #%d" % trial)
		got = combine_stack([Layer(s), Layer(s, anchor="bottom_right")], canvas=(10, 9))
		check(got.shape == (9, 10, 4) and close(got[5:, 4:], s) and close(got[:4, :6], s), "fixed canvas anchors #%d" % trial)
		# background: a fully transparent base shows the background
		bg = (10, 20, 30, 255)
		got = combine_stack([Layer(base_clear)], background=bg)
		check(close(got, np.broadcast_to(np.array(bg, np.float32), got.shape)), "background #%d" % trial)
		# resize and trim
		check(close(combine_stack([Layer(s, scale=3)]), np.repeat(np.repeat(s, 3, 0), 3, 1)), "scale 3 #%d" % trial)
		check(close(combine_stack([Layer(s, size=(12, 8))]), np.repeat(np.repeat(s, 2, 0), 2, 1)), "size WxH #%d" % trial)
		pad = np.zeros((10, 11, 4), np.float32)
		pad[3:7, 2:8] = s
		check(close(combine_stack([Layer(pad, trim=True)]), s), "trim #%d" % trial)
		# mask and inverted mask split the result exactly
		m = rng.random((12, 12)) < 0.5
		for mode in ("alpha", "screen", "multiply"):
			full = combine_stack([Layer(a), Layer(b, mode=mode)])
			got_m = combine_stack([Layer(a), Layer(b, mode=mode, mask=m)])
			got_i = combine_stack([Layer(a), Layer(b, mode=mode, mask=m, mask_invert=True)])
			check(close(got_m[m], full[m]) and close(got_m[~m], a[~m]) and close(got_i[~m], full[~m])
				  and close(got_i[m], a[m]), "mask split %s #%d" % (mode, trial))
		pm = combine_stack([Layer(a), Layer(b, mode="paint", mask=m)])
		pi = combine_stack([Layer(a), Layer(b, mode="paint", mask=m, mask_invert=True)])
		check(close(pm, old.combine_paint(a, b, m)) and close(pi, old.combine_paint(a, b, ~m)), "paint mask invert #%d" % trial)
	# preset round trip
	layers = [Layer("base.png"), Layer("labels", mode="screen", opacity=0.5, offset=(1, -2), anchor="center", scale=2,
								   mask="m.png", mask_invert=True)]
	d = C.stack_to_dict(layers, canvas=(32, 32), background=(1, 2, 3, 4), lcm=True, fit="contain", vary=[1])
	import json
	back, opts = C.stack_from_dict(json.loads(json.dumps(d)))
	check(back == layers and opts == {"canvas": (32, 32), "background": (1, 2, 3, 4), "lcm": True, "fit": "contain",
									  "vary": [1]}, "preset round trip")
	check(C.parse_color("#0a0b0c") == (10, 11, 12, 255) and C.parse_color("1,2,3,4") == (1, 2, 3, 4), "parse_color")


def bulk(tmp):
	base = write(tmp, "can.png", img(8, 8))
	for n in ("a", "b", "c"):
		write(tmp, "fruit/%s.png" % n, img(8, 8))
	write(tmp, "fruit/odd.png", img(5, 5))
	for n in ("x", "y"):
		write(tmp, "lid/%s.png" % n, img(8, 8))
	layers = [Layer(base), Layer(None), Layer(None, mode="multiply")]
	vary = {1: os.path.join(tmp, "fruit"), 2: os.path.join(tmp, "lid")}
	n = C.count_stack_jobs(vary)
	jobs = C.stack_jobs(layers, vary, {"fit": "same"}, "{base}_{name1}_{name2}.png")
	check(n == 8 and len(jobs) == 8, "cartesian count 4 x 2")
	out = os.path.join(tmp, "out")
	rep = batch.run_batch(jobs, out)
	check(len(rep.written) == 6 and len(rep.skipped) == 2 and all("odd" in r.reason for r in rep.skipped),
		  "cartesian skips the odd size: %r" % rep.counts())
	check(sorted(os.listdir(out)) == sorted("can_%s_%s.png" % (f, l) for f in "abc" for l in "xy"), "cartesian names")
	ref = combine_stack([Layer(base), Layer(os.path.join(tmp, "fruit", "b.png")),
						 Layer(os.path.join(tmp, "lid", "y.png"), mode="multiply")])
	got = np.array(Image.open(os.path.join(out, "can_b_y.png"))).astype(np.float32)
	check(np.array_equal(got, np.clip(ref, 0, 255).astype(np.uint8).astype(np.float32)), "cartesian output = stack")
	one = C.stack_jobs(layers[:2], {1: os.path.join(tmp, "fruit")}, None, "{name}.png", exclude="o*")
	check([j.fields["name"] for j in one] == ["a", "b", "c"], "single slot with exclude")


def main():
	tmp = tempfile.mkdtemp(prefix="tt_combine_stack_")
	try:
		parity(tmp)
		properties()
		bulk(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("{0}  combine stack: {1} checks, {2} failed".format("PASS" if not results[1] else "FAIL", *results))
	return 1 if results[1] else 0


if __name__ == "__main__":
	sys.exit(main())

# SPDX-License-Identifier: GPL-3.0-only
"""T4 editor engine tests (plan_texture_tools_gui.md sections 6, 10): undo and redo identity, bucket tolerance 0
equals an exact-match flood (checked against Pillow's own floodfill), global bucket count equals the pixel count of
that color, rotate x4 and flip x2 identity, transform round trips, rasterizer shape rules, cut and paste, palettes.
Synthetic arrays only; palette files in a temp dir.
"""

import os
import shutil
import sys
import tempfile

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)

from texturetools.core import edit as E  # noqa: E402
from texturetools.core.undo import UndoStack  # noqa: E402

results = [0, 0]


def check(ok, what):
	results[0] += 1
	if not ok:
		results[1] += 1
		print("FAIL  " + what)


rng = np.random.default_rng(31)


def few_colors(h, w, k=3):
	pal = rng.integers(0, 256, (k, 4)).astype(np.uint8)
	pal[:, 3] = 255
	return pal[rng.integers(0, k, (h, w))]


def rand_img(h, w):
	return rng.integers(0, 256, (h, w, 4)).astype(np.uint8)


def undo_tests():
	for trial in range(20):
		states = [rand_img(8, 8) for _ in range(6)]
		s = UndoStack(states[0])
		for st in states[1:]:
			s.push(st, "step")
		for k in range(5, 0, -1):
			check(np.array_equal(s.current, states[k]), "undo walk %d" % k)
			s.undo()
		check(np.array_equal(s.current, states[0]) and s.undo() is None, "undo to start")
		for k in range(1, 6):
			check(np.array_equal(s.redo(), states[k]), "redo %d" % k)
		check(s.redo() is None and not s.can_redo, "redo end")
		s.undo()
		s.undo()
		s.push(states[0], "branch")
		check(not s.can_redo and len(s) == 5, "push drops the redo branch")
		got = s.current
		got[:] = 0
		check(not np.array_equal(s.current, got) or not states[0].any(), "returned state is a copy")
		check([l for l, _ in s.history()][-1] == "branch" and s.history()[-1][1], "history labels")
		check(np.array_equal(s.jump(0), states[0]), "jump")
	big = UndoStack(np.zeros((16, 16, 4), np.uint8), limit_bytes=5 * 16 * 16 * 4)
	for i in range(20):
		big.push(np.full((16, 16, 4), i, np.uint8))
	check(len(big) == 5 and big.bytes_used <= 5 * 1024 and big.current[0, 0, 0] == 19, "memory limit drops oldest")
	cap = UndoStack(np.zeros(1), max_steps=3)
	for i in range(10):
		cap.push(np.full(1, i))
	check(len(cap) == 3 and [cap.undo()[0], cap.undo()[0], cap.undo()] == [8, 7, None], "max_steps")


def fill_tests():
	for trial in range(40):
		img = few_colors(12, 14)
		x, y = int(rng.integers(0, 14)), int(rng.integers(0, 12))
		new = (1, 2, 3, 4)
		got = E.bucket_fill(img, x, y, new, tolerance=0)
		ref = Image.fromarray(img, "RGBA").copy()
		ImageDraw.floodfill(ref, (x, y), new, thresh=0)
		check(np.array_equal(got, np.array(ref)), "bucket tol 0 = Pillow floodfill #%d" % trial)
		target = tuple(img[y, x])
		count = int((img == np.array(target)).all(axis=2).sum())
		g = E.global_fill(img, x, y, new)
		check(int((g == np.array(new)).all(axis=2).sum()) == count and
			  np.array_equal(g[(img != np.array(target)).any(axis=2)], img[(img != np.array(target)).any(axis=2)]),
			  "global bucket count #%d" % trial)
		# tolerance: a high tolerance fills a superset
		wide = E.bucket_fill(img, x, y, new, tolerance=255)
		check((wide == np.array(new)).all(), "tolerance 255 fills everything #%d" % trial)
		check(E.eyedropper(img, x, y) == tuple(int(v) for v in img[y, x]), "eyedropper #%d" % trial)
		sel = E.select_rect(img.shape, 2, 2, 6, 5)
		g2 = E.global_fill(img, x, y, new, selection=sel)
		check(np.array_equal(g2[~sel], img[~sel]), "selection limits fill #%d" % trial)
		wand = E.magic_wand(img, x, y, contiguous=True)
		check(np.array_equal(wand, (got != img).any(axis=2)), "magic wand = bucket region #%d" % trial)


def transform_tests():
	for trial in range(30):
		h, w = int(rng.integers(1, 20)), int(rng.integers(1, 20))
		img = rand_img(h, w)
		r = img
		for _ in range(4):
			r = E.rotate(r, 90)
		check(np.array_equal(r, img), "rotate x4 identity #%d" % trial)
		check(np.array_equal(E.rotate(E.rotate(img, 90), 90), E.rotate(img, 180)), "90+90 = 180 #%d" % trial)
		check(np.array_equal(E.rotate(E.rotate(img, 90), 270), img), "90+270 identity #%d" % trial)
		check(E.rotate(img, 90).shape == (w, h, 4) and np.array_equal(E.rotate(img, 90)[0, -1], img[0, 0]),
			  "rotate is clockwise #%d" % trial)
		for ax in ("h", "v"):
			check(np.array_equal(E.flip(E.flip(img, ax), ax), img), "flip %s twice #%d" % (ax, trial))
		check(np.array_equal(E.flip(E.flip(img, "h"), "v"), E.rotate(img, 180)), "flip h+v = rotate 180 #%d" % trial)
		dx, dy = int(rng.integers(-30, 30)), int(rng.integers(-30, 30))
		check(np.array_equal(E.shift(E.shift(img, dx, dy), -dx, -dy), img), "shift wrap round trip #%d" % trial)
		s = E.shift(img, 2, 1, wrap=False)
		check(np.array_equal(s[1:, 2:], img[:h - 1, :w - 2]) and not s[:1].any() and not s[:, :2].any(), "shift clip #%d" % trial)
		f = int(rng.integers(1, 5))
		check(np.array_equal(E.downscale_int(E.scale_int(img, f), f), img), "scale round trip #%d" % trial)
		for anchor in E.ANCHORS:
			grown = E.resize_canvas(img, w + 5, h + 3, anchor)
			ax, ay = E.ANCHORS[anchor]
			x0, y0 = 5 * ax // 2, 3 * ay // 2
			check(grown.shape == (h + 3, w + 5, 4) and np.array_equal(E.crop(grown, x0, y0, x0 + w - 1, y0 + h - 1), img),
				  "resize canvas %s #%d" % (anchor, trial))
		pad = np.zeros((h + 6, w + 4, 4), np.uint8)
		opaque = img.copy()
		opaque[:, :, 3] = 255
		pad[2:2 + h, 3:3 + w] = opaque
		check(np.array_equal(E.trim(pad), opaque), "trim #%d" % trial)
	check(np.array_equal(E.trim(np.zeros((3, 3, 4), np.uint8)), np.zeros((3, 3, 4), np.uint8)), "trim empty")


def draw_tests():
	for trial in range(40):
		x0, y0, x1, y1 = (int(v) for v in rng.integers(-3, 20, 4))
		pts = E.line_points(x0, y0, x1, y1)
		check(pts[0] == (x0, y0) and pts[-1] == (x1, y1) and len(pts) == max(abs(x1 - x0), abs(y1 - y0)) + 1 and
			  all(max(abs(a[0] - b[0]), abs(a[1] - b[1])) == 1 for a, b in zip(pts, pts[1:])), "line #%d" % trial)
		xa, ya, xb, yb = 0, 0, int(rng.integers(0, 15)), int(rng.integers(0, 15))
		fr = E.rect_points(xa, ya, xb, yb, True)
		ol = E.rect_points(xb, yb, xa, ya, False)
		check(len(fr) == (xb + 1) * (yb + 1) and set(ol) <= set(fr) and
			  len(ol) == (len(fr) if xb < 2 or yb < 2 else 2 * (xb + 1) + 2 * (yb - 1)), "rect #%d" % trial)
		for filled in (True, False):
			ep = set(E.ellipse_points(xa, ya, xb, yb, filled))
			check(ep == {(xb - x, y) for x, y in ep} and ep == {(x, yb - y) for x, y in ep}, "ellipse symmetric #%d" % trial)
			check(all(0 <= x <= xb and 0 <= y <= yb for x, y in ep) and len(ep) > 0, "ellipse in box #%d" % trial)
		check(set(E.ellipse_points(xa, ya, xb, yb, False)) <= set(E.ellipse_points(xa, ya, xb, yb, True)), "outline in fill")
	img = np.zeros((8, 10, 4), np.uint8)
	got = E.pencil(img, 1, 2, (9, 9, 9, 255), mirror="both")
	check({(int(x), int(y)) for y, x in zip(*np.nonzero(got[:, :, 3]))} == {(1, 2), (8, 2), (1, 5), (8, 5)}, "mirror both")
	got = E.pencil(img, 6, 0, (9, 9, 9, 255), prev=(0, 0))
	check(int((got[:, :, 3] > 0).sum()) == 7, "pencil drag joins with a line")
	check(np.array_equal(E.pencil(img, 50, 50, (1, 1, 1, 255)), img), "off-canvas point ignored")
	check(not img.any(), "input never changed")


def clipboard_tests():
	for trial in range(20):
		img = rand_img(10, 12)
		mask = rng.random((10, 12)) < 0.4
		if not mask.any():
			continue
		cut_img, fl = E.cut_selection(img, mask)
		check(not cut_img[mask].any() and np.array_equal(cut_img[~mask], img[~mask]), "cut clears #%d" % trial)
		check(np.array_equal(E.commit_floating(cut_img, fl), img), "cut + paste in place = original #%d" % trial)
		moved = fl.moved(3, -2).moved(-3, 2)
		check(np.array_equal(E.commit_floating(cut_img, moved), img), "move there and back #%d" % trial)
		far = E.commit_floating(img, fl.moved(100, 100))
		check(np.array_equal(far, img), "paste off canvas is a no-op #%d" % trial)
		p = E.paste(img[:3, :4], 2, 5)
		got = E.commit_floating(np.zeros_like(img), p)
		check(np.array_equal(got[5:8, 2:6], img[:3, :4]), "clipboard paste #%d" % trial)
		opaque = img.copy()
		opaque[:, :, 3] = 255
		check(np.array_equal(E.commit_floating(img, E.paste(opaque), blend=True), opaque), "blend opaque = replace #%d" % trial)
	check(E.copy_selection(img, np.zeros((10, 12), bool)) is None, "empty selection copies nothing")
	a = E.select_rect((6, 6), 0, 0, 2, 2)
	b = E.select_rect((6, 6), 2, 2, 4, 4)
	check(E.combine_selection(a, b, "add").sum() == 17 and E.combine_selection(a, b, "intersect").sum() == 1 and
		  E.combine_selection(a, b, "subtract").sum() == 8, "selection ops")


def palette_tests(tmp):
	img = few_colors(9, 9, 4)
	img[0, 0] = (5, 6, 7, 0)
	pal = E.image_palette(img)
	check(sum(n for _, n in pal) == 80 and all(c[3] > 0 for c, _ in pal), "palette counts opaque pixels")
	check([n for _, n in pal] == sorted((n for _, n in pal), reverse=True), "palette most used first")
	check(len(E.image_palette(img, include_transparent=True)) == len(pal) + 1, "include transparent")
	entries = [((1, 2, 3, 255), "one"), ((250, 128, 0, 77), "two"), ((0, 0, 0, 255), "")]
	for ext in (".hex", ".txt", ".json"):
		p = os.path.join(tmp, "p" + ext)
		E.export_palette(entries, p)
		back = E.import_palette(p)
		check([c for c, _ in back] == [c for c, _ in entries], "palette round trip %s" % ext)
	E.export_palette(entries, os.path.join(tmp, "p.json"))
	check(E.import_palette(os.path.join(tmp, "p.json")) == entries, "json keeps names")
	E.export_palette(entries, os.path.join(tmp, "p.gpl"))
	back = E.import_palette(os.path.join(tmp, "p.gpl"))
	check([c[:3] for c, _ in back] == [c[:3] for c, _ in entries] and back[0][1] == "one", "gpl round trip (no alpha)")
	E.export_palette([c for c, _ in entries], os.path.join(tmp, "plain.hex"))
	check(len(E.import_palette(os.path.join(tmp, "plain.hex"))) == 3, "plain colors export")
	ip = os.path.join(tmp, "img.png")
	Image.fromarray(img, "RGBA").save(ip)
	check([c for c, _ in E.import_palette(ip)] == E.image_colors(img), "palette from image")


def main():
	tmp = tempfile.mkdtemp(prefix="tt_edit_")
	try:
		undo_tests()
		fill_tests()
		transform_tests()
		draw_tests()
		clipboard_tests()
		palette_tests(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("{0}  T4 editor: {1} checks, {2} failed".format("PASS" if not results[1] else "FAIL", *results))
	return 1 if results[1] else 0


if __name__ == "__main__":
	sys.exit(main())

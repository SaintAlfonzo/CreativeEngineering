# SPDX-License-Identifier: GPL-3.0-only
"""Combine engine: two-layer raw compositing (alpha, add, multiply, paint), --lcm scaling, --center
placement and the batch over an overlay folder. From combine_tex.py, behavior unchanged (plan_texture_tools_gui.md
P0; the layer stack comes in P2).
"""

import math
import os
import sys

import numpy as np

from texturetools.core.image_io import load_rgba_array, save_raw


def combine_alpha(base_arr, overlay_arr):
	"""Standard 'over' alpha compositing, straight (non-premultiplied) alpha,
	done directly on raw stored values -- no gamma linearization."""
	base_rgb, base_a = base_arr[:, :, :3], base_arr[:, :, 3:4] / 255.0
	over_rgb, over_a = overlay_arr[:, :, :3], overlay_arr[:, :, 3:4] / 255.0

	out_a = over_a + base_a * (1 - over_a)
	# Avoid divide-by-zero where out_a is 0 (fully transparent result)
	safe_a = np.where(out_a == 0, 1, out_a)
	out_rgb = (over_rgb * over_a + base_rgb * base_a * (1 - over_a)) / safe_a

	result = np.concatenate([out_rgb, out_a * 255.0], axis=2)
	return result


def combine_add(base_arr, overlay_arr):
	"""Additive blend on RGB, alpha combined via standard 'over'."""
	result = combine_alpha(base_arr, overlay_arr)
	added_rgb = np.clip(base_arr[:, :, :3] + overlay_arr[:, :, :3], 0, 255)
	result[:, :, :3] = added_rgb
	return result


def combine_multiply(base_arr, overlay_arr):
	"""Multiplicative blend on RGB (darkens), alpha combined via standard 'over'."""
	result = combine_alpha(base_arr, overlay_arr)
	mult_rgb = (base_arr[:, :, :3] / 255.0) * (overlay_arr[:, :, :3] / 255.0) * 255.0
	result[:, :, :3] = mult_rgb
	return result


def load_mask(path, base_arr):
	"""Boolean paint mask: alpha > 0 in the mask PNG, or the base's opaque pixels."""
	if path is None:
		return base_arr[:, :, 3] > 0
	m = load_rgba_array(path)
	if m.shape[:2] != base_arr.shape[:2]:
		raise ValueError("mask {0} is {1}, base is {2}".format(path, m.shape[:2][::-1], base_arr.shape[:2][::-1]))
	return m[:, :, 3] > 0


def combine_paint(base_arr, overlay_arr, mask=None, strength=1.0, inset=0, clip=False):
	"""Paint-on: overlay painted into the base's masked pixels, keeping the base's
	luminance relative to the masked mean (strength 0..1). See the docstring."""
	if mask is None:
		mask = base_arr[:, :, 3] > 0
	h, w = base_arr.shape[:2]
	ys = np.clip(np.arange(h), inset, h - 1 - inset)
	xs = np.clip(np.arange(w), inset, w - 1 - inset)
	paint = overlay_arr[ys][:, xs]
	lum = base_arr[:, :, 0] * 0.299 + base_arr[:, :, 1] * 0.587 + base_arr[:, :, 2] * 0.114
	mean = lum[mask].mean() if mask.any() else 1.0
	f = 1.0 + (lum / (mean or 1.0) - 1.0) * strength
	result = np.zeros(base_arr.shape, dtype=np.float32) if clip else base_arr.copy()
	result[mask, :3] = paint[mask, :3] * f[mask][:, None]
	result[mask, 3] = base_arr[mask, 3]
	return result


def lcm_size(*arrs):
	"""(height, width) that every array's size divides evenly."""
	h = w = 1
	for a in arrs:
		h = h * a.shape[0] // math.gcd(h, a.shape[0])
		w = w * a.shape[1] // math.gcd(w, a.shape[1])
	return h, w


def scale_to(arr, size):
	"""Nearest-neighbour upscale arr to size (h, w) by one whole-number
	factor. Returns (scaled, factor), or (None, None) if the two axes would
	need different factors."""
	fy, fx = size[0] // arr.shape[0], size[1] // arr.shape[1]
	if fy != fx:
		return None, None
	return np.repeat(np.repeat(arr, fy, axis=0), fx, axis=1), fy


def lcm_pair(base_arr, overlay_arr):
	"""Scale both arrays to their lcm size. Returns (base, overlay, info) or
	raises ValueError naming the image that would be stretched."""
	size = lcm_size(base_arr, overlay_arr)
	b, bf = scale_to(base_arr, size)
	o, of = scale_to(overlay_arr, size)
	for name, arr, f in (("base", base_arr, bf), ("overlay", overlay_arr, of)):
		if f is None:
			raise ValueError(
				"{0} {1} would need different x and y factors to reach {2}".format(
					name, arr.shape[:2][::-1], size[::-1]
				)
			)
	return b, o, "{0}x{1} (base {2}x, overlay {3}x)".format(size[1], size[0], bf, of)


def center_overlay(base_arr, overlay_arr):
	"""Pad overlay_arr with transparent pixels so it sits centred on a canvas
	the size of base_arr. Returns None if the overlay is larger than the base
	on either axis."""
	bh, bw = base_arr.shape[:2]
	oh, ow = overlay_arr.shape[:2]
	if oh > bh or ow > bw:
		return None
	canvas = np.zeros(base_arr.shape, dtype=np.float32)
	y, x = (bh - oh) // 2, (bw - ow) // 2
	canvas[y:y + oh, x:x + ow] = overlay_arr
	return canvas


def fit_overlay(base_arr, overlay_arr, center):
	"""Return overlay matched to the base size, or None if it can't be."""
	if overlay_arr.shape == base_arr.shape:
		return overlay_arr
	return center_overlay(base_arr, overlay_arr) if center else None


MODES = {
	"alpha": combine_alpha,
	"add": combine_add,
	"multiply": combine_multiply,
	"paint": combine_paint,
}


IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".bmp", ".tga", ".gif", ".webp"}


def combine(mode, base_arr, overlay_arr, paint_opts=None):
	"""Run one mode; paint_opts = dict(mask=path, strength=, inset=, clip=) for paint."""
	if mode != "paint":
		return MODES[mode](base_arr, overlay_arr)
	o = dict(paint_opts or {})
	o["mask"] = load_mask(o.get("mask"), base_arr)
	return combine_paint(base_arr, overlay_arr, **o)


def combine_dir(base, overlay_dir, output_dir, pattern="{name}.png", mode="alpha", center=False, lcm=False,
				paint_opts=None):
	"""Combine base with every image in overlay_dir, saving each result to
	output_dir under pattern.format(name=, ext=, base=). Returns the number
	of files written."""
	base_arr = load_rgba_array(base)
	base_stem = os.path.splitext(os.path.basename(base))[0]
	os.makedirs(output_dir, exist_ok=True)

	written = 0
	for fname in sorted(os.listdir(overlay_dir)):
		src = os.path.join(overlay_dir, fname)
		stem, ext = os.path.splitext(fname)
		if not os.path.isfile(src) or ext.lower() not in IMAGE_EXTS:
			continue
		overlay_arr = load_rgba_array(src)
		pair_base = base_arr
		if lcm:
			try:
				pair_base, overlay_arr, info = lcm_pair(base_arr, overlay_arr)
			except ValueError as e:
				sys.stderr.write("Skip:    {0}: {1}\n".format(src, e))
				continue
		fitted = fit_overlay(pair_base, overlay_arr, center)
		if fitted is None:
			sys.stderr.write(
				"Skip:    {0} is {1}, base is {2}\n".format(
					src, overlay_arr.shape[:2][::-1], base_arr.shape[:2][::-1]
				)
			)
			continue
		out = os.path.join(output_dir, pattern.format(name=stem, ext=ext[1:], base=base_stem))
		save_raw(combine(mode, pair_base, fitted, paint_opts), out)
		print("Saved:   {0}{1}".format(out, "  " + info if lcm else ""))
		written += 1

	print("Mode:    {0}".format(mode))
	print("Base:    {0}".format(base))
	print("Wrote {0} file(s) to {1}".format(written, output_dir))
	return written

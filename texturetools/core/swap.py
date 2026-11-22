# SPDX-License-Identifier: GPL-3.0-only
"""Palette swap engine: remap a source image onto a palette by brightness rank (legacy, --even,
--interpolate). From palette_swap.py, behavior unchanged (plan_texture_tools_gui.md P0; modes come in P1).
"""

import os

from PIL import Image

from texturetools.core.palette import bin_mean, bin_pick, get_colors_by_brightness, lerp_palette, palette_bins


IMAGE_EXTENSIONS = (".png", ".bmp", ".tga", ".gif", ".webp")


def palette_swap(src_arr, palette_colors, interpolate=False, even=False, palette_counts=None):
	"""
	Remap every opaque color in src_arr to a palette color by brightness.
	Transparent pixels are left untouched. palette_colors is ordered darkest-first
	for a plain swap (load_palette_colors does that), lightest-first with --invert.

	Default (even=False, interpolate=False): legacy rank map, unchanged. Each source
	color's brightness VALUE is scaled onto the palette index, so bunched source
	shades land on bunched palette entries and a long palette is sampled sparsely.

	The source shades are first collapsed to their distinct brightness levels
	(n of them), ranked darkest to lightest. Rank i of n sits at t = i/(n-1).

	even: even rank spacing. Rank i takes palette index round(t * (len-1)), so the
	shades spread evenly over the palette however unevenly their brightness is
	spaced. When the palette has more colors than the source has levels, the
	palette is split into n contiguous runs, run i around palette position t, and
	level i takes the most-used real color of run i (palette_counts; the color at
	t without counts, which is the plain even pick), so no stretch of the palette
	is skipped, including colors whose hue breaks the brightness order (a dark
	red run among browns).

	interpolate: blend instead of picking. A palette with fewer colors than the
	source has colors is sampled as a gradient at t (unchanged); otherwise (palette
	as large as the source or larger, which used to fall back to the legacy map)
	the palette is split into n runs as above and level i takes run i's
	pixel-weighted mean color. Even rank spacing either way.

	Raises ValueError if src_arr has no opaque pixels, or if palette_colors is empty.
	"""
	src_colors = get_colors_by_brightness(src_arr)
	if not src_colors:
		raise ValueError("source image has no opaque pixels to recolor")

	n_palette = len(palette_colors)
	if n_palette == 0:
		raise ValueError("palette image has no opaque pixels to sample colors from")

	def brightness(c):
		return int(c[0]) + int(c[1]) + int(c[2])

	src_bright = [brightness(c) for c in src_colors]
	src_min = min(src_bright)
	src_max = max(src_bright)

	legacy_blend = interpolate and 1 < n_palette < len(src_colors)
	levels = sorted(set(src_bright))
	n_levels = len(levels)
	level_rank = dict((b, i) for i, b in enumerate(levels))

	def rank_t(b):
		return level_rank[b] / float(n_levels - 1) if n_levels > 1 else 0.5

	binned = None
	# Legacy blend (interpolate, palette smaller than the source's color count) keeps its output.
	if (even or interpolate) and not legacy_blend and n_palette >= n_levels > 1:
		bins = palette_bins(palette_colors, n_levels)
		pick = bin_mean if interpolate else bin_pick
		binned = [pick(run, center, palette_counts) for run, center in bins]

	def map_color(b):
		if binned is not None:
			return binned[level_rank[b]]
		if legacy_blend:
			return lerp_palette(palette_colors, rank_t(b))
		if even:
			idx = int(round(rank_t(b) * (n_palette - 1)))
			return palette_colors[max(0, min(n_palette - 1, idx))]
		t = (b - src_min) / float(src_max - src_min) if src_max != src_min else 0.5
		idx = int(round(t * (n_palette - 1)))
		idx = max(0, min(n_palette - 1, idx))  # clamp defensively
		return palette_colors[idx]

	color_map = dict((c, map_color(brightness(c))) for c in src_colors)

	result_arr = src_arr.copy()
	for src, dst in color_map.items():
		mask = (
			(src_arr[:, :, 0] == src[0])
			& (src_arr[:, :, 1] == src[1])
			& (src_arr[:, :, 2] == src[2])
			& (src_arr[:, :, 3] == src[3])
		)
		result_arr[mask] = dst

	return result_arr


def process_one(src_arr, palette_colors, out_path, preview, interpolate=False, even=False, palette_counts=None):
	"""Apply palette_colors onto an already-loaded src_arr and save the result.
	Raises on failure; caller decides how to handle."""
	result_arr = palette_swap(src_arr, palette_colors, interpolate, even, palette_counts)
	result_img = Image.fromarray(result_arr, "RGBA")

	out_dir = os.path.dirname(out_path)
	if out_dir and not os.path.exists(out_dir):
		os.makedirs(out_dir, exist_ok=True)

	_, out_ext = os.path.splitext(out_path)
	if out_ext.lower() in (".jpg", ".jpeg"):
		# JPEG has no alpha channel; flatten onto white rather than crashing.
		result_img = result_img.convert("RGB")

	result_img.save(out_path)

	if preview:
		base, ext = os.path.splitext(out_path)
		preview_path = base + "_preview" + ext
		preview_img = result_img.convert("RGBA") if result_img.mode != "RGBA" else result_img
		preview_img.resize((160, 160), Image.NEAREST).save(preview_path)
		return out_path, preview_path

	return out_path, None

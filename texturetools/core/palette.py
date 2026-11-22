# SPDX-License-Identifier: GPL-3.0-only
"""Palette extraction: opaque colors by brightness, pixel counts, drop classes, gradient sampling and even
runs. From palette_swap.py, behavior unchanged (plan_texture_tools_gui.md P0).
"""

import colorsys

import numpy as np
from PIL import Image


# --drop classes, tested on (hue degrees, saturation, value). The Vinery port's
# BottleLooks.Drop uses the same bounds -- keep the two in step.
DROP_CLASSES = {
	"brown": lambda h, s, v: 10 <= h <= 45 and s >= 0.4 and v <= 0.7,
	"white": lambda h, s, v: s <= 0.15 and v >= 0.85,
}


def drop_colors(colors, drops):
	"""Leave out colors matching any of the named DROP_CLASSES (all kept if none would remain)."""
	if not drops:
		return colors

	def dropped(c):
		h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
		return any(DROP_CLASSES[d](h * 360, s, v) for d in drops)

	kept = [c for c in colors if not dropped(c)]
	return kept or colors


def get_colors_by_brightness(arr, descending=True):
	"""Return unique opaque colors sorted by brightness (sum of RGB channels)."""
	pixels = arr.reshape(-1, 4)
	colors = set(map(tuple, pixels[pixels[:, 3] > 0].tolist()))
	return sorted(
		colors, key=lambda c: int(c[0]) + int(c[1]) + int(c[2]), reverse=descending
	)


def color_counts(arr):
	"""Pixel count of every opaque color in arr, as {(r, g, b, a): count}."""
	pixels = arr.reshape(-1, 4)
	pixels = pixels[pixels[:, 3] > 0]
	if not len(pixels):
		return {}
	uniq, counts = np.unique(pixels, axis=0, return_counts=True)
	return dict((tuple(int(v) for v in c), int(n)) for c, n in zip(uniq, counts))


def lerp_palette(palette_colors, t):
	"""Sample the palette as a continuous gradient at t in [0, 1] (RGBA blended linearly)."""
	pos = t * (len(palette_colors) - 1)
	lo = int(pos)
	hi = min(lo + 1, len(palette_colors) - 1)
	f = pos - lo
	a, b = palette_colors[lo], palette_colors[hi]
	return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(4))


def palette_bins(palette_colors, n_bins):
	"""Split the ordered palette into n_bins contiguous runs, 1 < n_bins <= len(palette): run i holds the colors
	nearest the even position i/(n_bins-1) along the palette (end runs are half width, so the palette's first and
	last colors anchor the ends). Returns [(run colors, index of the color at the position within the run)]."""
	n = len(palette_colors)
	runs = [[] for _ in range(n_bins)]
	for k, c in enumerate(palette_colors):
		runs[int(round(k * (n_bins - 1) / float(n - 1)))].append((k, c))
	out = []
	for i, run in enumerate(runs):
		center = i * (n - 1) / float(n_bins - 1)
		ks = [k for k, _ in run]
		out.append(([c for _, c in run], min(range(len(ks)), key=lambda j: (abs(ks[j] - center), ks[j]))))
	return out


def bin_mean(colors, center, counts):
	"""Pixel-weighted mean color of one palette run (each color weight 1 when counts are unknown)."""
	w = [float(counts.get(tuple(c), 1)) if counts else 1.0 for c in colors]
	total = sum(w)
	return tuple(int(round(sum(c[i] * wi for c, wi in zip(colors, w)) / total)) for i in range(4))


def bin_pick(colors, center, counts):
	"""The most-used real color of one palette run (the color at the run's position on a tie or without counts)."""
	if not counts:
		return tuple(colors[center])
	order = sorted(range(len(colors)), key=lambda i: (-counts.get(tuple(colors[i]), 0), abs(i - center), i))
	return tuple(colors[order[0]])


def load_palette_colors(pal_path, invert, drops=(), drop_darkest=0, drop_lightest=0, with_counts=False):
	"""Load a palette image once and return its brightness-ordered color list
	(and, with with_counts, a third value: {color: pixel count} for --even / --interpolate)."""
	pal_img = Image.open(pal_path).convert("RGBA")
	pal_arr = np.array(pal_img)
	palette_colors = drop_colors(get_colors_by_brightness(pal_arr), drops)
	if 0 < drop_darkest < len(palette_colors):
		palette_colors = palette_colors[:-drop_darkest]
	if 0 < drop_lightest < len(palette_colors):
		palette_colors = palette_colors[drop_lightest:]
	if not invert:
		palette_colors = list(reversed(palette_colors))
	if with_counts:
		return pal_img, palette_colors, color_counts(pal_arr)
	return pal_img, palette_colors

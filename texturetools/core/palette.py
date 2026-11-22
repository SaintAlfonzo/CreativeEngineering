# SPDX-License-Identifier: GPL-3.0-only
"""Palette extraction: opaque colors by brightness, pixel counts, drop classes, gradient sampling and even
runs. From palette_swap.py, behavior unchanged (plan_texture_tools_gui.md P0). P1 adds extract_palette (alpha
threshold, exclusion list with tolerance, HSV shift), parse_color and color_match_mask (shared with paint.py).
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


# ---------------------------------------------------------------- P1: extraction knobs, color lists

def parse_color(value):
	"""(r, g, b) from '#rrggbb', 'rrggbb', '#rgb', 'r,g,b' or an (r, g, b[, a]) sequence. Raises ValueError."""
	if isinstance(value, (tuple, list)):
		if len(value) < 3:
			raise ValueError("color needs 3 channels: {0!r}".format(value))
		return tuple(int(v) for v in value[:3])
	v = str(value).strip()
	if "," in v:
		parts = [int(x) for x in v.split(",")]
		if len(parts) < 3:
			raise ValueError("color needs 3 channels: {0!r}".format(value))
		return tuple(parts[:3])
	v = v.lstrip("#")
	if len(v) == 3:
		v = "".join(ch * 2 for ch in v)
	if len(v) not in (6, 8):
		raise ValueError("not a hex color: {0!r}".format(value))
	return tuple(int(v[i:i + 2], 16) for i in (0, 2, 4))


def color_match_mask(arr, colors, tol=0):
	"""Boolean HxW mask of pixels whose RGB is within tol of any color in colors, per channel (max channel
	difference <= tol, so tol 0 is an exact match). Alpha is not compared."""
	h, w = arr.shape[:2]
	mask = np.zeros((h, w), dtype=bool)
	rgb = arr[:, :, :3].astype(np.int32)
	for c in colors or ():
		c = np.array(parse_color(c), dtype=np.int32)
		mask |= np.abs(rgb - c).max(axis=2) <= tol
	return mask


def color_in_list(color, colors, tol=0):
	"""True when color's RGB is within tol (max channel difference) of any color in colors."""
	for c in colors or ():
		c = parse_color(c)
		if max(abs(int(color[i]) - c[i]) for i in range(3)) <= tol:
			return True
	return False


def hsv_shift_color(c, hue=0.0, sat=0.0, val=0.0):
	"""Shift one RGBA color: hue in degrees (wraps), saturation and value as additive deltas in -1..1 (clamped)."""
	if not (hue or sat or val):
		return tuple(c)
	h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
	h = (h + hue / 360.0) % 1.0
	s = min(1.0, max(0.0, s + sat))
	v = min(1.0, max(0.0, v + val))
	r, g, b = colorsys.hsv_to_rgb(h, s, v)
	out = (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)))
	return out + tuple(c[3:4]) if len(c) > 3 else out


def extract_palette(pal_arr, invert=False, drops=(), drop_darkest=0, drop_lightest=0, alpha_min=1, exclude=(),
					exclude_tol=0, hsv_shift=(0.0, 0.0, 0.0)):
	"""Palette colors and pixel counts from an RGBA array, as (colors, counts).

	Order: darkest first, lightest first with invert (as load_palette_colors). Steps: pixels with alpha below
	alpha_min are ignored (1 = every non-transparent pixel, the legacy rule; 255 = opaque only); colors within
	exclude_tol of an exclude color are left out; drop classes, drop_darkest and drop_lightest as before; then the
	HSV shift (hue degrees, saturation delta, value delta) and a re-sort by brightness. Each step that would leave
	no colors is skipped. With the defaults the result equals load_palette_colors(..., with_counts=True)[1:].
	"""
	pal_arr = np.asarray(pal_arr)
	if pal_arr.dtype != np.uint8:
		pal_arr = np.clip(np.round(pal_arr), 0, 255).astype(np.uint8)
	alpha_min = max(1, int(alpha_min))
	if alpha_min > 1:
		pal_arr = pal_arr.copy()
		pal_arr[pal_arr[:, :, 3] < alpha_min] = 0
	counts = color_counts(pal_arr)
	colors = get_colors_by_brightness(pal_arr)
	if exclude:
		kept = [c for c in colors if not color_in_list(c, exclude, exclude_tol)]
		colors = kept or colors
	colors = drop_colors(colors, drops)
	if 0 < drop_darkest < len(colors):
		colors = colors[:-drop_darkest]
	if 0 < drop_lightest < len(colors):
		colors = colors[drop_lightest:]
	hue, sat, val = (tuple(hsv_shift) + (0.0, 0.0, 0.0))[:3]
	if hue or sat or val:
		shifted, new_counts = [], {}
		for c in colors:
			d = hsv_shift_color(c, hue, sat, val)
			if d not in new_counts:
				shifted.append(d)
			new_counts[d] = new_counts.get(d, 0) + counts.get(tuple(c), 0)
		colors = sorted(shifted, key=lambda c: int(c[0]) + int(c[1]) + int(c[2]), reverse=True)
		counts = new_counts
	if not invert:
		colors = list(reversed(colors))
	return colors, counts

# SPDX-License-Identifier: GPL-3.0-only
"""Palette swap engine (plan_texture_tools_gui.md section 3).

palette_swap() is the legacy rank map (with --even / --interpolate), unchanged from palette_swap.py and kept for
parity (T1). P1 adds SwapOptions and swap_image() / swap_colors(): modes rank, even, role, nearest, ramp;
interpolation off / smaller / always with an amount and RGB, HSV or Lab space; palette alpha threshold, exclusion
with tolerance and HSV shift; source keep list; preserve-source blend; contrast stretch; alpha source; ordered
dither. With default SwapOptions, swap_image() equals load_palette_colors + palette_swap array for array.

The Lab / HSV helpers below are private copies; consolidate into core/colors.py (P4) later.
"""

import colorsys
import math
import os
from dataclasses import asdict, dataclass, field, fields, replace

import numpy as np
from PIL import Image

from texturetools.core.palette import (bin_mean, bin_pick, color_counts, color_in_list, extract_palette,
									   get_colors_by_brightness, lerp_palette, palette_bins, parse_color)


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


# ================================================================ P1: options

MODES = ("rank", "even", "role", "nearest", "ramp")
INTERPOLATE = ("off", "smaller", "always")
SPACES = ("rgb", "hsv", "lab")
ALPHA_SOURCES = ("palette", "source")
DITHERS = ("none", "ordered")


@dataclass
class SwapOptions:
	"""Every swap knob. Defaults reproduce the legacy rank map exactly.

	mode           rank (legacy, by brightness value), even (by brightness rank), role (hue family, then lightness
	               rank inside it), nearest (closest palette color in Lab), ramp (source Lab lightness fitted onto
	               the palette sorted by lightness)
	interpolate    off; smaller = blend only when the palette has fewer colors than the source; always = blend
	               also when it is larger (rank/even: pixel-weighted mean of each palette run, as legacy
	               --interpolate). rank/even with interpolate use even rank spacing (legacy --interpolate)
	interp_amount  0..1, how far toward the blended color (1 = full blend)
	interp_space   rgb, hsv or lab: the space the blend and run means are taken in
	invert         palette order reversed (dark <-> light)
	drops, drop_darkest, drop_lightest   as the CLI flags
	palette_alpha  palette pixels with alpha below this are ignored (1 = legacy, every non-transparent pixel;
	               255 = opaque only)
	palette_exclude, palette_exclude_tol  colors (hex strings or tuples) left out of the palette, with every color
	               within tol per channel
	source_keep, source_keep_tol   source colors left untouched (outlines, eyes) and not ranked
	hue_shift, sat_shift, val_shift    applied to the palette before mapping (degrees, -1..1, -1..1)
	preserve       0..1, blend the result back toward the source per pixel
	stretch        contrast stretch: nearest and ramp fit the source lightness range onto the palette's (rank,
	               even and role already rank relative to the source's own range, so it changes nothing there)
	alpha          palette = output alpha from the palette color (legacy); source = keep the source alpha
	dither         none or ordered (4x4 Bayer between the two nearest palette colors; only where the palette has
	               fewer colors than the source; replaces blending there; rank, even and ramp)
	"""
	mode: str = "rank"
	interpolate: str = "off"
	interp_amount: float = 1.0
	interp_space: str = "rgb"
	invert: bool = False
	drops: list = field(default_factory=list)
	drop_darkest: int = 0
	drop_lightest: int = 0
	palette_alpha: int = 1
	palette_exclude: list = field(default_factory=list)
	palette_exclude_tol: int = 0
	source_keep: list = field(default_factory=list)
	source_keep_tol: int = 0
	hue_shift: float = 0.0
	sat_shift: float = 0.0
	val_shift: float = 0.0
	preserve: float = 0.0
	stretch: bool = False
	alpha: str = "palette"
	dither: str = "none"

	def validate(self):
		for name, allowed in (("mode", MODES), ("interpolate", INTERPOLATE), ("interp_space", SPACES),
							  ("alpha", ALPHA_SOURCES), ("dither", DITHERS)):
			if getattr(self, name) not in allowed:
				raise ValueError("{0} must be one of {1}, not {2!r}".format(name, ", ".join(allowed), getattr(self, name)))
		if not 0.0 <= self.interp_amount <= 1.0 or not 0.0 <= self.preserve <= 1.0:
			raise ValueError("interp_amount and preserve must be in 0..1")
		if not 1 <= int(self.palette_alpha) <= 255:
			raise ValueError("palette_alpha must be in 1..255")
		return self

	def to_dict(self):
		return asdict(self)

	@classmethod
	def from_dict(cls, d):
		"""Build from a preset dict; unknown keys are ignored, missing ones take the default."""
		names = set(f.name for f in fields(cls))
		return cls(**dict((k, v) for k, v in (d or {}).items() if k in names)).validate()

	def is_legacy(self):
		"""True when only legacy knobs are set (palette_swap.py output, flags --invert/--drop*/--even/--interpolate)."""
		d = SwapOptions()
		return all(getattr(self, k) == getattr(d, k) for k in (
			"interp_amount", "interp_space", "palette_alpha", "palette_exclude", "source_keep", "hue_shift",
			"sat_shift", "val_shift", "preserve", "stretch", "alpha", "dither")) \
			and self.mode in ("rank", "even") and self.interpolate in ("off", "always")

	def cli_args(self):
		"""The palette_swap.py flags for these options (defaults omitted), for "Copy as CLI"."""
		d, out = SwapOptions(), []
		if self.invert:
			out.append("--invert")
		if self.drops:
			out += ["--drop", ",".join(self.drops)]
		if self.drop_darkest:
			out += ["--drop-darkest", str(self.drop_darkest)]
		if self.drop_lightest:
			out += ["--drop-lightest", str(self.drop_lightest)]
		if self.mode != d.mode:
			out += ["--mode", self.mode]
		if self.interpolate != d.interpolate:
			out += ["--interp", self.interpolate]
		simple = (("interp_amount", "--interp-amount"), ("interp_space", "--interp-space"),
				  ("palette_alpha", "--palette-alpha"), ("palette_exclude_tol", "--palette-tol"),
				  ("source_keep_tol", "--keep-tol"), ("hue_shift", "--hue-shift"), ("sat_shift", "--sat-shift"),
				  ("val_shift", "--val-shift"), ("preserve", "--preserve"), ("alpha", "--alpha"),
				  ("dither", "--dither"))
		for name, flag in simple:
			if getattr(self, name) != getattr(d, name):
				out += [flag, str(getattr(self, name))]
		if self.palette_exclude:
			out += ["--palette-exclude", ",".join(_hex(c) for c in self.palette_exclude)]
		if self.source_keep:
			out += ["--keep-source", ",".join(_hex(c) for c in self.source_keep)]
		if self.stretch:
			out.append("--stretch")
		return out


def _hex(c):
	return "#{0:02x}{1:02x}{2:02x}".format(*parse_color(c))


# ================================================================ P1: private color math (move to colors.py, P4)

def _srgb_to_lin(c):
	c = c / 255.0
	return np.where(c <= 0.04045, c / 12.92, ((c + 0.055) / 1.055) ** 2.4)


def _lin_to_srgb(c):
	c = np.clip(c, 0.0, 1.0)
	return np.where(c <= 0.0031308, c * 12.92, 1.055 * np.power(c, 1 / 2.4) - 0.055) * 255.0


_M = np.array([[0.4124564, 0.3575761, 0.1804375], [0.2126729, 0.7151522, 0.0721750], [0.0193339, 0.1191920, 0.9503041]])
_MI = np.linalg.inv(_M)
_WHITE = np.array([0.95047, 1.0, 1.08883])


def rgb_to_lab(rgb):
	"""sRGB (0..255, shape (..., 3)) -> CIE Lab D65."""
	xyz = _srgb_to_lin(np.asarray(rgb, dtype=np.float64)[..., :3]) @ _M.T / _WHITE
	f = np.where(xyz > (6 / 29.0) ** 3, np.cbrt(xyz), xyz / (3 * (6 / 29.0) ** 2) + 4 / 29.0)
	return np.stack([116 * f[..., 1] - 16, 500 * (f[..., 0] - f[..., 1]), 200 * (f[..., 1] - f[..., 2])], axis=-1)


def lab_to_rgb(lab):
	"""CIE Lab D65 -> sRGB floats 0..255 (clamped to gamut)."""
	lab = np.asarray(lab, dtype=np.float64)
	fy = (lab[..., 0] + 16) / 116.0
	f = np.stack([fy + lab[..., 1] / 500.0, fy, fy - lab[..., 2] / 200.0], axis=-1)
	xyz = np.where(f > 6 / 29.0, f ** 3, 3 * (6 / 29.0) ** 2 * (f - 4 / 29.0)) * _WHITE
	return _lin_to_srgb(xyz @ _MI.T)


def _lightness(c):
	return float(rgb_to_lab(np.array(c[:3], dtype=np.float64))[0])


def _hsv(c):
	return colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)


def _from_hsv(h, s, v):
	r, g, b = colorsys.hsv_to_rgb(h % 1.0, s, v)
	return [r * 255.0, g * 255.0, b * 255.0]


def _round_color(rgb, a):
	return tuple(int(round(min(255.0, max(0.0, float(x))))) for x in rgb) + (int(round(a)),)


def mix(a, b, f, space="rgb"):
	"""Blend RGBA a -> b by f in 0..1 in the given space (alpha linear). rgb matches lerp_palette's rounding."""
	if space == "rgb" or f <= 0 or f >= 1:
		if f <= 0:
			return tuple(int(v) for v in a)
		if f >= 1:
			return tuple(int(v) for v in b)
		return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(4))
	alpha = a[3] + (b[3] - a[3]) * f
	if space == "lab":
		la, lb = rgb_to_lab(np.array(a[:3], float)), rgb_to_lab(np.array(b[:3], float))
		return _round_color(lab_to_rgb(la + (lb - la) * f), alpha)
	ha, sa, va = _hsv(a)
	hb, sb, vb = _hsv(b)
	if sa == 0:
		ha = hb
	if sb == 0:
		hb = ha
	dh = ((hb - ha + 0.5) % 1.0) - 0.5
	return _round_color(_from_hsv(ha + dh * f, sa + (sb - sa) * f, va + (vb - va) * f), alpha)


def lerp_palette_space(palette_colors, t, space="rgb"):
	"""lerp_palette in a chosen space (rgb is exactly lerp_palette)."""
	if space == "rgb":
		return lerp_palette(palette_colors, t)
	pos = t * (len(palette_colors) - 1)
	lo = int(pos)
	hi = min(lo + 1, len(palette_colors) - 1)
	return mix(palette_colors[lo], palette_colors[hi], pos - lo, space)


def mean_space(colors, center, counts, space="rgb"):
	"""bin_mean in a chosen space (rgb is exactly bin_mean); hsv takes a saturation-weighted circular hue mean."""
	if space == "rgb":
		return bin_mean(colors, center, counts)
	w = np.array([float(counts.get(tuple(c), 1)) if counts else 1.0 for c in colors])
	w = w / w.sum()
	alpha = float((np.array([c[3] for c in colors], float) * w).sum())
	if space == "lab":
		lab = rgb_to_lab(np.array([c[:3] for c in colors], float))
		return _round_color(lab_to_rgb((lab * w[:, None]).sum(axis=0)), alpha)
	hsv = [_hsv(c) for c in colors]
	x = sum(wi * s * math.cos(2 * math.pi * h) for wi, (h, s, v) in zip(w, hsv))
	y = sum(wi * s * math.sin(2 * math.pi * h) for wi, (h, s, v) in zip(w, hsv))
	h = (math.atan2(y, x) / (2 * math.pi)) % 1.0
	return _round_color(_from_hsv(h, sum(wi * s for wi, (_, s, _) in zip(w, hsv)),
								  sum(wi * v for wi, (_, _, v) in zip(w, hsv))), alpha)


# ================================================================ P1: mapping per mode
# A mapping is {source RGBA: (lo RGBA, hi RGBA, frac)}: the pixel takes lo, or hi where the ordered dither
# threshold is below frac. Without dither frac is 0 and lo is the final color.

def _fixed(c):
	return (tuple(c), tuple(c), 0.0)


def _blend_or_dither(pal, pos, opts, blend, space, dither=False):
	"""Entry for a continuous palette position: a dither pair, a blend, or the pick at round(pos)."""
	n = len(pal)
	pos = max(0.0, min(n - 1.0, pos))
	lo = int(math.floor(pos))
	hi = min(lo + 1, n - 1)
	frac = pos - lo
	if dither:
		return (tuple(pal[lo]), tuple(pal[hi]), frac)
	pick = tuple(pal[max(0, min(n - 1, int(round(pos))))])
	if not blend:
		return _fixed(pick)
	blended = mix(pal[lo], pal[hi], frac, space) if space != "rgb" else lerp_palette(pal, pos / float(n - 1) if n > 1 else 0)
	return _fixed(mix(pick, blended, opts.interp_amount, space) if opts.interp_amount < 1 else blended)


def _rank_map(src_colors, pal, counts, opts, even):
	"""rank and even modes over one source color set onto one ordered palette. With rgb space, amount 1, no
	dither and interpolate off/always this is palette_swap() exactly (same branches)."""
	def brightness(c):
		return int(c[0]) + int(c[1]) + int(c[2])

	n_palette = len(pal)
	space = opts.interp_space
	interp = opts.interpolate != "off"
	src_bright = [brightness(c) for c in src_colors]
	src_min, src_max = min(src_bright), max(src_bright)
	smaller = 1 < n_palette < len(src_colors)
	if opts.interpolate == "smaller" and not smaller:
		interp = False   # only when the palette is smaller: otherwise the plain pick of the mode
	legacy_blend = interp and smaller
	if interp:
		even = True  # legacy --interpolate always ranks evenly
	levels = sorted(set(src_bright))
	n_levels = len(levels)
	level_rank = dict((b, i) for i, b in enumerate(levels))

	def rank_t(b):
		return level_rank[b] / float(n_levels - 1) if n_levels > 1 else 0.5

	binned = None
	if (even or interp) and not legacy_blend and n_palette >= n_levels > 1:
		bins = palette_bins(pal, n_levels)
		picks = [bin_pick(run, center, counts) for run, center in bins]
		if opts.interpolate == "always":
			means = [mean_space(run, center, counts, space) for run, center in bins]
			binned = [m if opts.interp_amount >= 1 else mix(p, m, opts.interp_amount, space)
					  for p, m in zip(picks, means)]
		else:
			binned = picks

	def entry(b):
		if binned is not None:
			return _fixed(binned[level_rank[b]])
		if n_palette == 1:
			return _fixed(pal[0])
		if legacy_blend:
			pos = rank_t(b) * (n_palette - 1)
			if opts.dither != "ordered" and space == "rgb":
				return _fixed(lerp_palette(pal, rank_t(b)))
			return _blend_or_dither(pal, pos, replace(opts, interp_amount=1.0), True, space, opts.dither == "ordered")
		if even:
			pos = rank_t(b) * (n_palette - 1)
		else:
			t = (b - src_min) / float(src_max - src_min) if src_max != src_min else 0.5
			pos = t * (n_palette - 1)
		if opts.dither == "ordered" and n_palette < n_levels:
			return _blend_or_dither(pal, pos, opts, False, space, True)
		idx = max(0, min(n_palette - 1, int(round(pos))))
		return _fixed(pal[idx])

	out = dict((c, entry(brightness(c))) for c in src_colors)
	if interp and opts.interp_amount < 1 and binned is None:
		# partial blend: toward the blend from this mode's own pick (even ranks, no blend), never from a dither pair
		picks = _rank_map(src_colors, pal, counts, replace(opts, interpolate="off", interp_amount=1.0, dither="none"),
						  True)
		for c, (lo, hi, frac) in out.items():
			if frac == 0 and lo == hi:
				out[c] = _fixed(mix(picks[c][0], lo, opts.interp_amount, space))
	return out


def _lightness_targets(src_colors, pal_l, opts):
	"""Source Lab lightness per color, stretched onto the palette range (stretch) or clamped to it, and mirrored
	with invert."""
	sl = dict((c, _lightness(c)) for c in src_colors)
	pmin, pmax = min(pal_l), max(pal_l)
	vals = list(sl.values())
	smin, smax = min(vals), max(vals)
	out = {}
	for c, l in sl.items():
		if opts.stretch:
			t = (l - smin) / (smax - smin) if smax > smin else 0.5
			target = pmin + t * (pmax - pmin)
		else:
			target = min(pmax, max(pmin, l))
		if opts.invert:
			target = pmin + pmax - target
		out[c] = target
	return out


def _ramp_map(src_colors, pal, counts, opts):
	"""ramp: the palette sorted by Lab lightness (ties by brightness), each source color placed on it by its own
	lightness, picking or blending the two palette neighbors."""
	ordered = sorted(pal, key=lambda c: (_lightness(c), int(c[0]) + int(c[1]) + int(c[2])))
	pal_l = [_lightness(c) for c in ordered]
	targets = _lightness_targets(src_colors, pal_l, opts)
	n = len(ordered)
	n_levels = len(set(int(c[0]) + int(c[1]) + int(c[2]) for c in src_colors))
	blend = opts.interpolate == "always" or (opts.interpolate == "smaller" and n < n_levels)
	out = {}
	for c, target in targets.items():
		if n == 1:
			out[c] = _fixed(ordered[0])
			continue
		i = int(np.searchsorted(pal_l, target, side="right")) - 1
		i = max(0, min(n - 2, i))
		span = pal_l[i + 1] - pal_l[i]
		frac = (target - pal_l[i]) / span if span > 0 else 0.0
		pos = i + max(0.0, min(1.0, frac))
		out[c] = _blend_or_dither(ordered, pos, opts, blend, opts.interp_space, opts.dither == "ordered" and n < n_levels)
	return out


def _nearest_map(src_colors, pal, counts, opts):
	"""nearest: the palette color closest in Lab (source lightness stretched / mirrored first when asked).
	Interpolation blends the two nearest, weighted by inverse distance."""
	pal_lab = rgb_to_lab(np.array([c[:3] for c in pal], float))
	targets = _lightness_targets(src_colors, list(pal_lab[:, 0]), opts) if (opts.stretch or opts.invert) else None
	n_levels = len(set(int(c[0]) + int(c[1]) + int(c[2]) for c in src_colors))
	blend = len(pal) > 1 and (opts.interpolate == "always" or (opts.interpolate == "smaller" and len(pal) < n_levels))
	out = {}
	for c in src_colors:
		lab = rgb_to_lab(np.array(c[:3], float))
		if targets is not None:
			lab = np.array([targets[c], lab[1], lab[2]])
		d = np.sqrt(((pal_lab - lab) ** 2).sum(axis=1))
		order = np.argsort(d, kind="stable")
		best = tuple(pal[order[0]])
		if blend:
			d1, d2 = d[order[0]], d[order[1]]
			f = d1 / (d1 + d2) if d1 + d2 > 0 else 0.0
			best = mix(best, pal[order[1]], f * opts.interp_amount, opts.interp_space)
		out[c] = _fixed(best)
	return out


GRAY_SAT = 0.18     # role mode: saturation below this (or value below GRAY_VAL) is the gray family
GRAY_VAL = 0.12
HUE_GAP = 40.0      # role mode: hue gap in degrees that splits two chromatic families


def hue_families(colors, counts=None):
	"""Split colors into role families: [('gray', colors)] plus chromatic families split at hue gaps over
	HUE_GAP degrees (circular), each as ('hue', colors, weighted mean hue in degrees, pixel weight)."""
	gray, chroma = [], []
	for c in colors:
		h, s, v = _hsv(c)
		(gray if s < GRAY_SAT or v < GRAY_VAL else chroma).append((h * 360.0, c))
	fams = []
	if chroma:
		chroma.sort(key=lambda hc: hc[0])
		hues = [h for h, _ in chroma]
		gaps = [((hues[(i + 1) % len(hues)] - hues[i]) % 360.0, i) for i in range(len(hues))]
		if len(hues) == 1:
			groups = [chroma]
		else:
			cut = [i for g, i in gaps if g > HUE_GAP]
			if not cut:
				groups = [chroma]
			else:
				start = (max(gaps)[1] + 1) % len(chroma)   # begin after the widest gap
				rot = chroma[start:] + chroma[:start]
				groups, cur = [], [rot[0]]
				for a, b in zip(rot, rot[1:]):
					if (b[0] - a[0]) % 360.0 > HUE_GAP:
						groups.append(cur)
						cur = []
					cur.append(b)
				groups.append(cur)
		for g in groups:
			w = [float(counts.get(tuple(c), 1)) if counts else 1.0 for _, c in g]
			x = sum(wi * math.cos(math.radians(h)) for wi, (h, _) in zip(w, g))
			y = sum(wi * math.sin(math.radians(h)) for wi, (h, _) in zip(w, g))
			fams.append(("hue", [c for _, c in g], math.degrees(math.atan2(y, x)) % 360.0, sum(w)))
	out = []
	if gray:
		w = sum(float(counts.get(tuple(c), 1)) if counts else 1.0 for _, c in gray)
		out.append(("gray", [c for _, c in gray], None, w))
	return out + fams


def _role_map(src_colors, pal, counts, opts, src_counts):
	"""role (first design, owner reviews): source and palette split into hue families (hue_families). The
	source's heaviest chromatic family goes to the palette's heaviest; every other chromatic family to the
	palette family nearest its hue plus the same hue offset, so accents keep their relation. The gray family goes
	to the palette grays when the source also has chromatic families and the palette has 2 or more grays,
	otherwise to the whole palette; a one-family source uses the whole palette. Inside each pair: even lightness
	rank (the even mode, with the interpolation knobs)."""
	sf = hue_families(src_colors, src_counts)
	pf = hue_families(pal, counts)
	s_hue = [f for f in sf if f[0] == "hue"]
	p_hue = [f for f in pf if f[0] == "hue"]
	p_gray = [f for f in pf if f[0] == "gray"]
	whole = list(pal)

	def subset(colors):
		keep = set(tuple(c) for c in colors)
		return [c for c in pal if tuple(c) in keep]

	offset = 0.0
	if s_hue and p_hue:
		offset = max(p_hue, key=lambda f: f[3])[2] - max(s_hue, key=lambda f: f[3])[2]
	out = {}
	for fam in sf:
		if len(sf) == 1:
			target = whole
		elif fam[0] == "gray":
			target = subset(p_gray[0][1]) if p_gray and len(p_gray[0][1]) >= 2 and s_hue else whole
		elif p_hue:
			want = (fam[2] + offset) % 360.0
			best = min(p_hue, key=lambda f: (min(abs(f[2] - want), 360.0 - abs(f[2] - want)), -f[3]))
			target = subset(best[1])
		else:
			target = subset(p_gray[0][1]) if p_gray else whole
		out.update(_rank_map(fam[1], target or whole, counts, opts, True))
	return out


# ================================================================ P1: public entry points

BAYER4 = (np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]], dtype=np.float64) + 0.5) / 16.0


def prepare_palette(pal_arr, opts=None):
	"""Palette colors (ordered as palette_swap expects) and pixel counts from a palette RGBA array, with the
	options' alpha threshold, exclusions, drops and HSV shift."""
	o = opts or SwapOptions()
	return extract_palette(pal_arr, o.invert, o.drops, o.drop_darkest, o.drop_lightest, o.palette_alpha,
						   o.palette_exclude, o.palette_exclude_tol, (o.hue_shift, o.sat_shift, o.val_shift))


def swap_colors(src_arr, palette_colors, opts=None, palette_counts=None):
	"""Recolor src_arr (HxWx4 uint8) with an already prepared palette (prepare_palette or load_palette_colors).
	Returns a new uint8 array; transparent pixels and source_keep colors are untouched.
	Raises ValueError as palette_swap does (no opaque source pixels, empty palette)."""
	o = (opts or SwapOptions()).validate()
	src_arr = np.asarray(src_arr)
	if src_arr.dtype != np.uint8:
		src_arr = np.clip(np.round(src_arr), 0, 255).astype(np.uint8)
	keep = np.zeros(src_arr.shape[:2], dtype=bool)
	if o.source_keep:
		from texturetools.core.palette import color_match_mask
		keep = color_match_mask(src_arr, o.source_keep, o.source_keep_tol) & (src_arr[:, :, 3] > 0)
	work = src_arr.copy()
	work[keep, 3] = 0   # kept colors are neither ranked nor changed
	src_colors = get_colors_by_brightness(work)
	if not src_colors:
		if keep.any():
			return src_arr.copy()   # every opaque color is on the keep list
		raise ValueError("source image has no opaque pixels to recolor")
	if not len(palette_colors):
		raise ValueError("palette image has no opaque pixels to sample colors from")
	pal = [tuple(int(v) for v in c) for c in palette_colors]
	pal = [c if len(c) == 4 else c + (255,) for c in pal]

	if o.mode in ("rank", "even"):
		mapping = _rank_map(src_colors, pal, palette_counts, o, o.mode == "even")
	elif o.mode == "ramp":
		mapping = _ramp_map(src_colors, pal, palette_counts, o)
	elif o.mode == "nearest":
		mapping = _nearest_map(src_colors, pal, palette_counts, o)
	else:
		mapping = _role_map(src_colors, pal, palette_counts, o, color_counts(work))

	# vectorized apply: index every pixel by its packed RGBA
	packed = work.astype(np.uint32)
	key = (packed[:, :, 0] << 24) | (packed[:, :, 1] << 16) | (packed[:, :, 2] << 8) | packed[:, :, 3]
	cols = list(mapping.keys())
	ckeys = np.array([(c[0] << 24) | (c[1] << 16) | (c[2] << 8) | c[3] for c in cols], dtype=np.uint32)
	order = np.argsort(ckeys)
	ckeys = ckeys[order]
	lo = np.array([mapping[cols[i]][0] for i in order], dtype=np.float64)
	hi = np.array([mapping[cols[i]][1] for i in order], dtype=np.float64)
	frac = np.array([mapping[cols[i]][2] for i in order], dtype=np.float64)
	active = work[:, :, 3] > 0
	idx = np.searchsorted(ckeys, key[active])
	h, w = work.shape[:2]
	if o.dither == "ordered" and frac.any():
		ys, xs = np.nonzero(active)
		take_hi = frac[idx] > BAYER4[ys % 4, xs % 4]
		new = np.where(take_hi[:, None], hi[idx], lo[idx])
	else:
		new = lo[idx]
	src_px = src_arr[active].astype(np.float64)
	if o.alpha == "source":
		new[:, 3] = src_px[:, 3]
	if o.preserve > 0:
		new[:, :3] = new[:, :3] * (1.0 - o.preserve) + src_px[:, :3] * o.preserve
	result = src_arr.copy()
	result[active] = np.clip(np.round(new), 0, 255).astype(np.uint8)
	return result


def swap_image(src_arr, pal_arr, opts=None):
	"""One swap from two RGBA arrays (the GUI preview and batch call). Returns a uint8 RGBA array."""
	o = (opts or SwapOptions()).validate()
	pal, counts = prepare_palette(pal_arr, o)
	return swap_colors(src_arr, pal, o, counts)


def load_rgba_u8(path):
	return np.array(Image.open(path).convert("RGBA"))


def swap_file(src_path, pal_path, out_path, opts=None, preview=False):
	"""Swap two files and save (process_one's save rules: JPEG flattened, optional 160x160 preview). For
	core/batch.py. Returns (out_path, preview_path or None)."""
	o = opts or SwapOptions()
	result = swap_image(load_rgba_u8(src_path), load_rgba_u8(pal_path), o)
	return _save(result, out_path, preview)


def _save(result_arr, out_path, preview):
	result_img = Image.fromarray(result_arr, "RGBA")
	out_dir = os.path.dirname(out_path)
	if out_dir and not os.path.exists(out_dir):
		os.makedirs(out_dir, exist_ok=True)
	if os.path.splitext(out_path)[1].lower() in (".jpg", ".jpeg"):
		result_img = result_img.convert("RGB")
	result_img.save(out_path)
	if preview:
		base, ext = os.path.splitext(out_path)
		preview_path = base + "_preview" + ext
		result_img.convert("RGBA").resize((160, 160), Image.NEAREST).save(preview_path)
		return out_path, preview_path
	return out_path, None


# ================================================================ P6: hand-built palettes, batch kind, bulk jobs

COLORS_PREFIX = "colors:"


def palette_array(colors):
	"""1xN opaque RGBA uint8 palette image from a hand-built swatch strip (hex strings or (r, g, b) tuples)."""
	px = [tuple(parse_color(c)) + (255,) for c in colors or ()]
	if not px:
		raise ValueError("the swatch strip is empty")
	return np.array([px], dtype=np.uint8)


def colors_spec(colors):
	"""'colors:#rrggbb,#rrggbb': a swatch strip written where a palette path goes (CLI and bulk jobs)."""
	return COLORS_PREFIX + ",".join(_hex(c) for c in colors)


def is_colors_spec(spec):
	return isinstance(spec, str) and spec.startswith(COLORS_PREFIX)


def load_palette_source(spec):
	"""RGBA uint8 array from a palette path or a colors: spec."""
	if is_colors_spec(spec):
		return palette_array([c for c in spec[len(COLORS_PREFIX):].split(",") if c.strip()])
	return load_rgba_u8(spec)


def _swap_worker(job):
	"""Job kind "swap": inputs source and palette (path, colors: spec or array), params options (SwapOptions dict).
	An engine ValueError (no opaque pixels, empty palette) becomes a Skip."""
	from texturetools.core import batch
	opts = SwapOptions.from_dict(job.params.get("options"))
	src, pal = job.inputs["source"], job.inputs["palette"]
	src_arr = load_rgba_u8(src) if isinstance(src, str) else np.asarray(src)
	pal_arr = load_palette_source(pal) if isinstance(pal, str) else np.asarray(pal)
	try:
		return swap_image(src_arr, pal_arr, opts)
	except ValueError as e:
		raise batch.Skip(str(e))


def save_swap(arr, out_path):
	"""batch saver with swap_file's save rules (JPEG flattened), no preview."""
	_save(np.asarray(arr, dtype=np.uint8), out_path, False)


def _stem(spec):
	return "swatches" if is_colors_spec(spec) else os.path.splitext(os.path.basename(spec))[0]


def swap_jobs(fixed, folder, reverse=False, opts=None, pattern="{name}.png", include=None, exclude=None,
			  recursive=False):
	"""Bulk swap jobs (kind "swap"; run with batch.run_batch(..., saver=save_swap)).

	reverse False (legacy --bulk): fixed is the shape (source) and every image in folder supplies a palette.
	reverse True: fixed is the palette (path or colors: spec) and every image in folder is a source.
	folder: a folder (scanned with include / exclude globs) or a list of paths. The fixed file itself is left out.
	Name fields: {name} the varying file's stem, {ext}, {shape} source stem, {palette} palette stem, {n}."""
	from texturetools.core import batch
	files = batch.scan_folder(folder, include, exclude, recursive) if isinstance(folder, str) else list(folder)
	fixed_abs = None if is_colors_spec(fixed) else os.path.abspath(fixed)
	files = [f for f in files if os.path.abspath(f) != fixed_abs]
	params = {"options": (opts or SwapOptions()).to_dict()}
	jobs = []
	for n, path in enumerate(files, 1):
		f = batch.name_fields(path)
		f["n"] = n
		if reverse:
			inputs = {"source": path, "palette": fixed}
			f["shape"], f["palette"] = f["name"], _stem(fixed)
		else:
			inputs = {"source": fixed, "palette": path}
			f["shape"], f["palette"] = _stem(fixed), f["name"]
		jobs.append(batch.Job(inputs=inputs, params=dict(params), pattern=pattern, fields=f, kind="swap",
							  label=os.path.basename(path)))
	return jobs


def _register_kind():
	from texturetools.core import batch
	batch.register_kind("swap", _swap_worker)


_register_kind()

# SPDX-License-Identifier: GPL-3.0-only
"""Combine engine (plan_texture_tools_gui.md section 4): raw compositing with no color management.

The two-layer functions (alpha, add, multiply, paint, --lcm, --center, the overlay-folder batch) are the old
combine_tex.py code, unchanged. P2 adds the layer stack: combine_stack folds any number of Layer objects over those
per-mode functions plus the new modes screen, overlay, subtract, min and max, with per-layer opacity, visibility,
whole-pixel offset (clip, wrap, grow), nearest resize (factor or WxH), anchor, trim, LCM fit, mask and inverted mask,
and a canvas (base, largest or fixed; transparent or a color). A two-layer stack equals the old output array for
array (T2). Bulk runs go through core/batch.py: stack_jobs (one varying slot, or several for a cartesian run) and
the registered "combine" job kind; combine_dir is a thin caller of it with the old printed output.
"""

import math
import os
import sys
from dataclasses import asdict, dataclass, field, fields as dc_fields
from typing import Any, Optional, Sequence

import numpy as np

from texturetools.core import batch
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
	"""Nearest-neighbor upscale arr to size (h, w) by one whole-number
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
	"""Pad overlay_arr with transparent pixels so it sits centered on a canvas
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


# ---------------------------------------------------------------- new blend modes (P2)

def _w3c_blend(base_arr, overlay_arr, fn, opacity=1.0):
	"""Separable blend per the W3C compositing spec on raw values: the blended color B(Cb, Cs) shows where both
	layers have alpha, the overlay's own color where the base is transparent, then source-over. Straight alpha,
	no gamma. opacity scales the overlay's alpha."""
	cb, ab = base_arr[:, :, :3] / 255.0, base_arr[:, :, 3:4] / 255.0
	cs, a_s = overlay_arr[:, :, :3] / 255.0, overlay_arr[:, :, 3:4] / 255.0 * opacity
	mixed = (1 - ab) * cs + ab * np.clip(fn(cb, cs), 0.0, 1.0)
	out_a = a_s + ab * (1 - a_s)
	safe_a = np.where(out_a == 0, 1, out_a)
	out_rgb = (mixed * a_s + cb * ab * (1 - a_s)) / safe_a
	return np.concatenate([out_rgb * 255.0, out_a * 255.0], axis=2)


def _overlay_fn(cb, cs):
	return np.where(cb <= 0.5, 2 * cb * cs, 1 - 2 * (1 - cb) * (1 - cs))


W3C_FNS = {
	"screen": lambda cb, cs: 1 - (1 - cb) * (1 - cs),
	"overlay": _overlay_fn,
	"subtract": lambda cb, cs: cb - cs,
	"min": np.minimum,
	"max": np.maximum,
}


def combine_screen(base_arr, overlay_arr):
	return _w3c_blend(base_arr, overlay_arr, W3C_FNS["screen"])


def combine_overlay(base_arr, overlay_arr):
	return _w3c_blend(base_arr, overlay_arr, W3C_FNS["overlay"])


def combine_subtract(base_arr, overlay_arr):
	return _w3c_blend(base_arr, overlay_arr, W3C_FNS["subtract"])


def combine_min(base_arr, overlay_arr):
	return _w3c_blend(base_arr, overlay_arr, W3C_FNS["min"])


def combine_max(base_arr, overlay_arr):
	return _w3c_blend(base_arr, overlay_arr, W3C_FNS["max"])


# MODES stays the old four (the old CLI's --mode choices); BLEND_MODES is every mode the stack takes.
BLEND_MODES = dict(MODES)
BLEND_MODES.update({
	"screen": combine_screen,
	"overlay": combine_overlay,
	"subtract": combine_subtract,
	"min": combine_min,
	"max": combine_max,
})
NEW_MODES = ("screen", "overlay", "subtract", "min", "max")


def combine(mode, base_arr, overlay_arr, paint_opts=None):
	"""Run one mode on two same-size arrays; paint_opts = dict(mask=path, strength=, inset=, clip=) for paint.
	A two-layer combine_stack (same output as before P2)."""
	o = dict(paint_opts or {}) if mode == "paint" else {}
	return combine_stack([Layer(base_arr), Layer(overlay_arr, mode=mode, mask=o.get("mask"),
												 strength=o.get("strength", 1.0), inset=o.get("inset", 0),
												 clip=o.get("clip", False))])


# ---------------------------------------------------------------- the layer stack (P2)

ANCHORS = {
	"top_left": (0, 0), "top": (1, 0), "top_right": (2, 0),
	"left": (0, 1), "center": (1, 1), "right": (2, 1),
	"bottom_left": (0, 2), "bottom": (1, 2), "bottom_right": (2, 2),
}
OFFSET_MODES = ("clip", "wrap", "grow")
FIT_POLICIES = ("any", "contain", "same")


class SizeMismatch(ValueError):
	"""A layer does not fit the canvas under the stack's fit policy (the old batch's size-mismatch skip)."""


@dataclass
class Layer:
	"""One layer. image: RGBA array (float or uint8, HxWx4) or a file path. The first visible layer is the base;
	its mode is ignored unless a background color is set (then it is blended onto the background).

	mode: a BLEND_MODES key. opacity 0..1: alpha and the W3C modes scale the layer's alpha; add, multiply and paint
	mix the result with the layer below. offset (x, y): whole pixels after the anchor. offset_mode: clip (cut at
	the canvas edge), wrap (re-enter on the other side), grow (the canvas grows to hold the layer).
	scale (whole factor) or size (w, h): nearest resize, after trim. anchor: ANCHORS key (where the layer sits
	when its size differs from the canvas). mask: path, bool array or None; canvas-sized, or layer-sized (moves with
	the layer); paint mode keeps the old rule (base-sized path, default the base's opaque pixels). mask_invert
	flips it. strength, inset, clip: paint options (old --strength, --inset, --clip)."""
	image: Any = None
	mode: str = "alpha"
	opacity: float = 1.0
	visible: bool = True
	offset: Sequence[int] = (0, 0)
	offset_mode: str = "clip"
	scale: Optional[int] = None
	size: Optional[Sequence[int]] = None
	anchor: str = "top_left"
	trim: bool = False
	mask: Any = None
	mask_invert: bool = False
	strength: float = 1.0
	inset: int = 0
	clip: bool = False
	name: str = ""

	def with_image(self, image):
		d = {f.name: getattr(self, f.name) for f in dc_fields(self)}
		d["image"] = image
		return Layer(**d)


def layer_to_dict(layer):
	"""JSON-safe dict (presets): an array image or array mask becomes None."""
	d = asdict(layer) if not isinstance(layer.image, np.ndarray) and not isinstance(layer.mask, np.ndarray) else \
		{f.name: getattr(layer, f.name) for f in dc_fields(layer)}
	for k in ("image", "mask"):
		if isinstance(d[k], np.ndarray):
			d[k] = None
	for k in ("offset", "size"):
		if d[k] is not None:
			d[k] = list(d[k])
	return d


def layer_from_dict(d, base_dir=None):
	"""Layer from a preset dict; unknown keys are ignored; relative image and mask paths resolve against base_dir."""
	names = {f.name for f in dc_fields(Layer)}
	kw = {k: v for k, v in d.items() if k in names}
	for k in ("image", "mask"):
		v = kw.get(k)
		if base_dir and isinstance(v, str) and not os.path.isabs(v):
			kw[k] = os.path.join(base_dir, v)
	for k in ("offset", "size"):
		if kw.get(k) is not None:
			kw[k] = tuple(kw[k])
	return Layer(**kw)


def stack_to_dict(layers, canvas="base", background=None, lcm=False, fit="any", vary=None):
	"""Preset dict for a whole stack; vary = list of slot indices that a bulk run fills from folders."""
	return {
		"layers": [layer_to_dict(l) for l in layers],
		"canvas": list(canvas) if isinstance(canvas, (tuple, list)) else canvas,
		"background": list(background) if background is not None else None,
		"lcm": bool(lcm),
		"fit": fit,
		"vary": list(vary or []),
	}


def stack_from_dict(d, base_dir=None):
	"""(layers, options) from a preset dict; options = dict(canvas=, background=, lcm=, fit=) for combine_stack, plus
	'vary' (list of slot indices)."""
	layers = [layer_from_dict(x, base_dir) for x in d.get("layers", [])]
	canvas = d.get("canvas", "base")
	if isinstance(canvas, list):
		canvas = tuple(canvas)
	bg = d.get("background")
	return layers, {"canvas": canvas, "background": tuple(bg) if bg is not None else None,
					"lcm": bool(d.get("lcm", False)), "fit": d.get("fit", "any"), "vary": list(d.get("vary", []))}


def parse_color(text):
	"""'RRGGBB', 'RRGGBBAA' (optional '#') or 'r,g,b[,a]' -> (r, g, b, a)."""
	t = text.strip().lstrip("#")
	if "," in t:
		v = [int(x) for x in t.split(",")]
	elif len(t) in (6, 8):
		v = [int(t[i:i + 2], 16) for i in range(0, len(t), 2)]
	else:
		raise ValueError("color must be RRGGBB, RRGGBBAA or r,g,b[,a]: {0}".format(text))
	if len(v) == 3:
		v.append(255)
	if len(v) != 4 or any(not 0 <= x <= 255 for x in v):
		raise ValueError("bad color: {0}".format(text))
	return tuple(v)


def _as_array(image):
	if isinstance(image, str):
		return load_rgba_array(image)
	if image is None:
		raise ValueError("layer has no image")
	a = np.asarray(image)
	if a.ndim != 3 or a.shape[2] != 4:
		raise ValueError("layer image must be HxWx4 RGBA, got shape {0}".format(a.shape))
	return a if a.dtype == np.float32 else a.astype(np.float32)


def trim_array(arr):
	"""Crop to the bounding box of alpha > 0 (an all-transparent array is returned unchanged)."""
	ys, xs = np.nonzero(arr[:, :, 3] > 0)
	if not len(ys):
		return arr
	return arr[ys.min():ys.max() + 1, xs.min():xs.max() + 1]


def resize_nearest(arr, w, h):
	"""Nearest-neighbor resize to w x h (whole factors give exact pixel blocks, same as np.repeat)."""
	if w < 1 or h < 1:
		raise ValueError("size must be at least 1x1")
	sh, sw = arr.shape[:2]
	ys = (np.arange(h) * sh) // h
	xs = (np.arange(w) * sw) // w
	return arr[ys][:, xs]


def _prepared(layer):
	arr = _as_array(layer.image)
	if layer.trim:
		arr = trim_array(arr)
	if layer.scale is not None:
		if int(layer.scale) != layer.scale or layer.scale < 1:
			raise ValueError("scale must be a whole number of 1 or more")
		f = int(layer.scale)
		if f != 1:
			arr = np.repeat(np.repeat(arr, f, axis=0), f, axis=1)
	elif layer.size is not None:
		arr = resize_nearest(arr, int(layer.size[0]), int(layer.size[1]))
	return arr


def _label(layer, i):
	if layer.name:
		return layer.name
	return layer.image if isinstance(layer.image, str) else "layer {0}".format(i)


def stack_layout(layers, canvas="base", lcm=False, fit="any", _arrays=None):
	"""Where every visible layer lands. Returns dict(size=(w, h) final canvas, origin=(x, y) of the requested
	canvas inside it (non-zero when a 'grow' layer extends it up or left), lcm=info text or None,
	layers=[dict(index, rect=(x, y, w, h) in final canvas coordinates, size=(w, h))]). Raises SizeMismatch per fit:
	'same' every visible layer must be the canvas size (old default), 'contain' must fit inside it at its place
	(old --center), 'any' crops. Raises ValueError for an LCM fit that would stretch a layer."""
	vis = [(i, l) for i, l in enumerate(layers) if l.visible]
	if not vis:
		raise ValueError("no visible layer")
	if fit not in FIT_POLICIES:
		raise ValueError("fit must be one of {0}".format(", ".join(FIT_POLICIES)))
	arrays = _arrays if _arrays is not None else {i: _prepared(l) for i, l in vis}
	info = None
	if lcm:
		size = lcm_size(*(arrays[i] for i, _ in vis))
		facs = []
		for i, l in vis:
			scaled, f = scale_to(arrays[i], size)
			if f is None:
				raise ValueError("{0} {1} would need different x and y factors to reach {2}".format(
					_label(l, i), arrays[i].shape[:2][::-1], size[::-1]))
			arrays[i] = scaled
			facs.append("{0}x".format(f))
		info = "{0}x{1} ({2})".format(size[1], size[0], ", ".join(facs))
	if canvas == "base":
		ch, cw = arrays[vis[0][0]].shape[:2]
	elif canvas == "largest":
		ch = max(arrays[i].shape[0] for i, _ in vis)
		cw = max(arrays[i].shape[1] for i, _ in vis)
	else:
		cw, ch = int(canvas[0]), int(canvas[1])
	rects = []
	for i, l in vis:
		if l.anchor not in ANCHORS:
			raise ValueError("anchor must be one of {0}".format(", ".join(ANCHORS)))
		if l.offset_mode not in OFFSET_MODES:
			raise ValueError("offset_mode must be one of {0}".format(", ".join(OFFSET_MODES)))
		h, w = arrays[i].shape[:2]
		ax, ay = ANCHORS[l.anchor]
		x = (cw - w) * ax // 2 + int(l.offset[0])
		y = (ch - h) * ay // 2 + int(l.offset[1])
		if fit == "same" and (w, h) != (cw, ch):
			raise SizeMismatch("{0} is {1}x{2}, canvas is {3}x{4}".format(_label(l, i), w, h, cw, ch))
		if fit == "contain" and l.offset_mode == "clip" and (x < 0 or y < 0 or x + w > cw or y + h > ch):
			raise SizeMismatch("{0} is {1}x{2}, canvas is {3}x{4}".format(_label(l, i), w, h, cw, ch))
		rects.append([i, x, y, w, h])
	x0, y0, x1, y1 = 0, 0, cw, ch
	for i, x, y, w, h in rects:
		if layers[i].offset_mode == "grow":
			x0, y0, x1, y1 = min(x0, x), min(y0, y), max(x1, x + w), max(y1, y + h)
	return {
		"size": (x1 - x0, y1 - y0),
		"origin": (-x0, -y0),
		"lcm": info,
		"layers": [{"index": i, "rect": (x - x0, y - y0, w, h), "size": (w, h)} for i, x, y, w, h in rects],
		"_arrays": arrays,
	}


def _place(arr, size, x, y, wrap):
	"""arr on a transparent canvas of size (w, h) at (x, y); wrap re-enters pixels that fall off an edge."""
	cw, ch = size
	h, w = arr.shape[:2]
	out = np.zeros((ch, cw, arr.shape[2]), dtype=arr.dtype)
	if x == 0 and y == 0 and (w, h) == (cw, ch):
		out[:] = arr
		return out
	if wrap:
		ys = (y + np.arange(h)) % ch
		xs = (x + np.arange(w)) % cw
		out[np.ix_(ys, xs)] = arr
		return out
	sx0, sy0 = max(0, -x), max(0, -y)
	dx0, dy0 = max(0, x), max(0, y)
	cw_ = min(w - sx0, cw - dx0)
	ch_ = min(h - sy0, ch - dy0)
	if cw_ > 0 and ch_ > 0:
		out[dy0:dy0 + ch_, dx0:dx0 + cw_] = arr[sy0:sy0 + ch_, sx0:sx0 + cw_]
	return out


def _mask_array(mask, canvas_size, layer_arr, rect, wrap):
	"""bool mask in canvas coordinates from a path / array: canvas-sized as is, layer-sized placed with the layer."""
	m = load_rgba_array(mask)[:, :, 3] > 0 if isinstance(mask, str) else np.asarray(mask)
	if m.ndim == 3:
		m = m[:, :, 3] > 0
	m = m.astype(bool)
	cw, ch = canvas_size
	if m.shape == (ch, cw):
		return m
	if m.shape == layer_arr.shape[:2]:
		return _place(m[:, :, None], canvas_size, rect[0], rect[1], wrap)[:, :, 0]
	raise ValueError("mask is {0}, canvas is {1} and layer is {2}".format(
		m.shape[::-1], (cw, ch), layer_arr.shape[:2][::-1]))


def _fold(acc, placed, layer, size, arr, rect):
	mode = layer.mode
	if mode not in BLEND_MODES:
		raise ValueError("unknown blend mode {0!r}; one of {1}".format(mode, ", ".join(BLEND_MODES)))
	op = float(layer.opacity)
	if op <= 0:
		return acc
	wrap = layer.offset_mode == "wrap"
	if mode == "paint":
		if isinstance(layer.mask, str) or layer.mask is None:
			m = load_mask(layer.mask, acc)
		else:
			m = _mask_array(layer.mask, size, arr, rect, wrap)
		if layer.mask_invert:
			m = ~m
		out = combine_paint(acc, placed, m, layer.strength, layer.inset, layer.clip)
		return out if op >= 1 else acc + (out - acc) * op
	if mode == "alpha":
		if op >= 1:
			out = combine_alpha(acc, placed)
		else:
			p = placed.copy()
			p[:, :, 3] *= op
			out = combine_alpha(acc, p)
	elif mode in W3C_FNS:
		out = _w3c_blend(acc, placed, W3C_FNS[mode], op)
	else:
		out = BLEND_MODES[mode](acc, placed)
		if op < 1:
			out = acc + (out - acc) * op
	if layer.mask is not None:
		m = _mask_array(layer.mask, size, arr, rect, wrap)
		if layer.mask_invert:
			m = ~m
		out = np.where(m[:, :, None], out, acc)
	return out


def combine_stack(layers, canvas="base", background=None, lcm=False, fit="any", return_layout=False):
	"""Fold the visible layers bottom to top. canvas: 'base' (first visible layer's size), 'largest', or (w, h);
	background: None (transparent) or (r, g, b, a); lcm: scale every visible layer to the least common multiple
	size first (whole factors only); fit: 'any' | 'contain' | 'same' (see stack_layout). Returns the float RGBA array
	(save with image_io.save_raw), or (array, layout) with return_layout."""
	layout = stack_layout(layers, canvas, lcm, fit)
	arrays = layout.pop("_arrays")
	size = layout["size"]
	acc = None
	if background is not None:
		acc = np.zeros((size[1], size[0], 4), dtype=np.float32)
		acc[:] = np.asarray(background, dtype=np.float32)
	for k, entry in enumerate(layout["layers"]):
		i, rect = entry["index"], entry["rect"]
		layer, arr = layers[i], arrays[i]
		placed = _place(arr, size, rect[0], rect[1], layer.offset_mode == "wrap")
		if acc is None:
			acc = placed
			if layer.opacity < 1:
				acc[:, :, 3] *= float(layer.opacity)
			if layer.mask is not None and layer.mode != "paint":
				m = _mask_array(layer.mask, size, arr, rect, layer.offset_mode == "wrap")
				if layer.mask_invert:
					m = ~m
				acc[~m] = 0
			continue
		if k == 0 and layer.mode == "paint":
			layer = layer.with_image(None)
			layer.mode = "alpha"
		acc = _fold(acc, placed, layer, size, arr, rect)
	return (acc, layout) if return_layout else acc


# ---------------------------------------------------------------- bulk: varying slots and cartesian (P2)

def _combine_worker(job):
	"""Registered job kind "combine": params = dict(layers=[Layer templates], options=combine_stack kwargs);
	inputs = slot index -> image path replacing that layer's image. A size mismatch becomes a Skip."""
	layers = list(job.params["layers"])
	for slot, path in job.inputs.items():
		if isinstance(slot, int):
			layers[slot] = layers[slot].with_image(path)
	opts = dict(job.params.get("options") or {})
	opts.pop("vary", None)
	try:
		arr, layout = combine_stack(layers, return_layout=True, **opts)
	except SizeMismatch as e:
		raise batch.Skip(str(e))
	except ValueError as e:
		if "different x and y factors" in str(e):
			raise batch.Skip(str(e))
		raise
	return arr, layout.get("lcm")


batch.register_kind("combine", _combine_worker)


def stack_jobs(layers, vary, options=None, pattern="{name}.png", include=None, exclude=None, recursive=False):
	"""Jobs for a bulk stack run. vary: slot index -> folder (scanned with include / exclude globs) or a list of
	paths. One slot = every file fills that layer; two or more = every combination (cartesian; check
	batch.count_product or len() of the result before running). Name fields: {name} (varying stems joined with
	'_'), {ext}, {name<slot>}, {ext<slot>}, {n}, {base} (stem of layer 0 when it is a fixed file)."""
	varying = {}
	for slot, src in sorted(vary.items()):
		varying[int(slot)] = batch.scan_folder(src, include, exclude, recursive) if isinstance(src, str) else list(src)
	extra = {}
	if isinstance(layers[0].image, str):
		extra["base"] = os.path.splitext(os.path.basename(layers[0].image))[0]
	return batch.product_jobs(varying, pattern=pattern, kind="combine",
							  params={"layers": list(layers), "options": dict(options or {})}, extra_fields=extra)


def count_stack_jobs(vary, include=None, exclude=None, recursive=False):
	"""How many jobs stack_jobs would make (the cartesian confirm count), without loading any image."""
	return batch.count_product({k: batch.scan_folder(v, include, exclude, recursive) if isinstance(v, str) else list(v)
								for k, v in vary.items()})


def combine_dir(base, overlay_dir, output_dir, pattern="{name}.png", mode="alpha", center=False, lcm=False,
				paint_opts=None, dry_run=False, overwrite="replace", include=None, exclude=None, layer_opts=None):
	"""Combine base with every image in overlay_dir, saving each result to output_dir under
	pattern.format(name=, ext=, base=). Returns the number of files written (dry run: the number it would write).
	The old batch, run through batch.run_batch with the old printed output; dry_run, overwrite, include, exclude
	and layer_opts (extra Layer fields for the overlay, e.g. opacity) are P2 additions."""
	base_arr = load_rgba_array(base)
	base_stem = os.path.splitext(os.path.basename(base))[0]
	o = dict(paint_opts or {}) if mode == "paint" else {}
	extra = dict(layer_opts or {})

	def worker(job):
		src = job.inputs["overlay"]
		overlay_arr = load_rgba_array(src)
		pair_base, info = base_arr, None
		if lcm:
			try:
				pair_base, overlay_arr, info = lcm_pair(base_arr, overlay_arr)
			except ValueError as e:
				raise batch.Skip("{0}: {1}".format(src, e))
		fitted = fit_overlay(pair_base, overlay_arr, center)
		if fitted is None:
			raise batch.Skip("{0} is {1}, base is {2}".format(src, overlay_arr.shape[:2][::-1], base_arr.shape[:2][::-1]))
		top = Layer(fitted, mode=mode, mask=o.get("mask"), strength=o.get("strength", 1.0), inset=o.get("inset", 0),
					clip=o.get("clip", False), **extra)
		return (lambda: combine_stack([Layer(pair_base), top])), info

	jobs = []
	for src in batch.scan_folder(overlay_dir, include, exclude):
		f = batch.name_fields(src)
		f["base"] = base_stem
		jobs.append(batch.Job(inputs={"overlay": src}, pattern=pattern, fields=f, label=src))

	def show(r):
		if r.status == "skipped":
			sys.stderr.write("Skip:    {0}\n".format(r.reason))
		elif r.status == "exists":
			sys.stderr.write("Skip:    {0}\n".format(r.reason))
		elif r.status in ("written", "dry"):
			print("{0}{1}{2}".format("Saved:   " if r.status == "written" else "Would save: ", r.output,
									 "  " + r.info if lcm else ""))

	report = batch.run_batch(jobs, output_dir, worker, dry_run=dry_run, overwrite=overwrite, on_result=show,
							 on_error="raise", compute_in_dry_run=True)
	written = len(report.written) + len(report.by_status("dry"))
	print("Mode:    {0}".format(mode))
	print("Base:    {0}".format(base))
	print("{0} {1} file(s) to {2}".format("Would write" if dry_run else "Wrote", written, output_dir))
	return written

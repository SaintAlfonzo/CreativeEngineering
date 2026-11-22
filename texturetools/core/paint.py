# SPDX-License-Identifier: GPL-3.0-only
"""Shape-preserving texture paint (plan_texture_tools_gui.md section 5, P3).

A material texture is painted into a shape's pixels, keeping the shape's luminance shading. Two modes:
- inplace: the material is sampled at the same pixel (after offset, integer scale, tile or edge clamp, inset,
  flips and rotation). With default PaintOptions this equals combine.combine_paint (the combine screen's paint
  mode, which stays in combine.py untouched).
- projected: the material is laid in one piece across the bounding box of the painted region (stretch, contain
  or cover fit, nearest), so its own image survives; the shape decides where it shows.

Region painted: the mask (base alpha by default, or a loaded mask, optionally inverted), narrowed by the
"paint only these colors" list, minus the keep list, minus the shape's outline when outline="keep", minus
transparent material pixels when material_alpha="skip". Shading: f = 1 + (lum / mean lum of the region - 1) *
strength, as combine_paint.

Arrays: any HxWx4 numeric RGBA in, float32 out (save with image_io.save_raw), as the combine engine.
"""

import os
from dataclasses import asdict, dataclass, field, fields

import numpy as np

from texturetools.core.image_io import load_rgba_array, save_raw
from texturetools.core.palette import color_match_mask

PAINT_MODES = ("inplace", "projected")
OUTLINES = ("shade", "keep")
FITS = ("stretch", "contain", "cover")
MATERIAL_ALPHA = ("ignore", "skip")
ROTATIONS = (0, 90, 180, 270)


@dataclass
class PaintOptions:
	"""Every painter knob. Defaults equal combine_paint(base, material) (strength 1, inset 0, no clip).

	mode            inplace or projected
	offset_x/y      shift the material right / down by whole base pixels
	scale           integer: each material pixel covers scale x scale base pixels (inplace)
	tile            wrap the material instead of clamping to its edge
	flip_h, flip_v, rotate   transform the material first (rotate 0/90/180/270, clockwise)
	inset           clamp sampling inside the material's edge by this many pixels (inplace and projected)
	strength        0..1, how much of the shape's luminance shading is kept (0 = the material's own colors)
	outline         shade (paint it like the rest, legacy) or keep (opaque pixels touching transparency or the
	                image edge keep the shape's colors)
	mask_invert     invert the loaded mask (only the shape's opaque pixels are ever painted)
	only_colors, only_tol    when set, paint only shape pixels within tol (per channel) of these colors
	keep_colors, keep_tol    never paint shape pixels within tol of these colors (eyes, rim)
	material_alpha  ignore (legacy: material alpha unused) or skip (transparent material pixels leave the shape)
	fit             projected: stretch to the bounding box, contain (whole material, aspect kept, centered) or
	                cover (fills the box, aspect kept, centered, cropped)
	clip            unpainted pixels become transparent (combine paint --clip)
	"""
	mode: str = "inplace"
	offset_x: int = 0
	offset_y: int = 0
	scale: int = 1
	tile: bool = False
	flip_h: bool = False
	flip_v: bool = False
	rotate: int = 0
	inset: int = 0
	strength: float = 1.0
	outline: str = "shade"
	mask_invert: bool = False
	only_colors: list = field(default_factory=list)
	only_tol: int = 0
	keep_colors: list = field(default_factory=list)
	keep_tol: int = 0
	material_alpha: str = "ignore"
	fit: str = "stretch"
	clip: bool = False

	def validate(self):
		for name, allowed in (("mode", PAINT_MODES), ("outline", OUTLINES), ("fit", FITS),
							  ("material_alpha", MATERIAL_ALPHA), ("rotate", ROTATIONS)):
			if getattr(self, name) not in allowed:
				raise ValueError("{0} must be one of {1}, not {2!r}".format(
					name, ", ".join(str(a) for a in allowed), getattr(self, name)))
		if int(self.scale) < 1 or int(self.inset) < 0:
			raise ValueError("scale must be 1 or more and inset 0 or more")
		if not 0.0 <= float(self.strength) <= 1.0:
			raise ValueError("strength must be in 0..1")
		return self

	def to_dict(self):
		return asdict(self)

	@classmethod
	def from_dict(cls, d):
		"""Build from a preset dict; unknown keys are ignored, missing ones take the default."""
		names = set(f.name for f in fields(cls))
		return cls(**dict((k, v) for k, v in (d or {}).items() if k in names)).validate()


def transform_material(material, flip_h=False, flip_v=False, rotate=0):
	"""Flip, then rotate clockwise by 0/90/180/270."""
	m = material
	if flip_h:
		m = m[:, ::-1]
	if flip_v:
		m = m[::-1]
	k = (int(rotate) // 90) % 4
	if k:
		m = np.rot90(m, -k)   # np.rot90 turns counterclockwise
	return np.ascontiguousarray(m)


def outline_mask(base_arr):
	"""Opaque pixels with a transparent 4-neighbor or on the image edge."""
	op = base_arr[:, :, 3] > 0
	pad = np.pad(op, 1, constant_values=False)
	inner = pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:]
	return op & ~inner


def _as_mask(mask, base_arr):
	if mask is None:
		return base_arr[:, :, 3] > 0
	m = np.asarray(mask)
	if m.ndim == 3:
		m = m[:, :, 3] > 0
	m = m.astype(bool)
	if m.shape != base_arr.shape[:2]:
		raise ValueError("mask is {0}, base is {1}".format(m.shape[::-1], base_arr.shape[:2][::-1]))
	return m


def _fold(idx, size, inset, tile):
	"""Clamp or wrap integer sample indices into [inset, size - 1 - inset]."""
	lo = min(inset, (size - 1) // 2)
	hi = max(lo, size - 1 - inset)
	if tile:
		return lo + np.mod(idx - lo, hi - lo + 1)
	return np.clip(idx, lo, hi)


def paint_region(base_arr, mask=None, opts=None):
	"""The boolean HxW region paint() writes, before the material alpha test."""
	o = opts or PaintOptions()
	region = _as_mask(mask, base_arr)
	if o.mask_invert:
		region = ~region & (base_arr[:, :, 3] > 0)
	if o.only_colors:
		region &= color_match_mask(base_arr, o.only_colors, o.only_tol)
	if o.keep_colors:
		region &= ~color_match_mask(base_arr, o.keep_colors, o.keep_tol)
	if o.outline == "keep":
		region &= ~outline_mask(base_arr)
	return region


def sample_coords(base_shape, material_shape, region, opts):
	"""Material (row, col) index arrays, HxW each, for every base pixel."""
	h, w = base_shape[:2]
	mh, mw = material_shape[:2]
	yy, xx = np.mgrid[0:h, 0:w]
	if opts.mode == "inplace":
		s = int(opts.scale)
		sy = np.floor_divide(yy - int(opts.offset_y), s)
		sx = np.floor_divide(xx - int(opts.offset_x), s)
	else:
		if region.any():
			ys, xs = np.nonzero(region)
			y0, y1, x0, x1 = ys.min(), ys.max(), xs.min(), xs.max()
		else:
			y0, y1, x0, x1 = 0, h - 1, 0, w - 1
		bh, bw = y1 - y0 + 1, x1 - x0 + 1
		ky, kx = bh / float(mh), bw / float(mw)
		if opts.fit == "contain":
			ky = kx = min(ky, kx)
		elif opts.fit == "cover":
			ky = kx = max(ky, kx)
		pad_y = (bh - mh * ky) / 2.0
		pad_x = (bw - mw * kx) / 2.0
		sy = np.floor((yy - y0 - int(opts.offset_y) - pad_y) / ky + 1e-9).astype(np.int64)
		sx = np.floor((xx - x0 - int(opts.offset_x) - pad_x) / kx + 1e-9).astype(np.int64)
	return _fold(sy, mh, int(opts.inset), opts.tile), _fold(sx, mw, int(opts.inset), opts.tile)


def paint(base_arr, material_arr, opts=None, mask=None):
	"""Paint material_arr into base_arr's shape. mask: None (base alpha), a bool HxW array or an RGBA array
	(alpha > 0). Returns float32 HxWx4 (save with image_io.save_raw)."""
	o = (opts or PaintOptions()).validate()
	base = np.asarray(base_arr, dtype=np.float32)
	mat = transform_material(np.asarray(material_arr, dtype=np.float32), o.flip_h, o.flip_v, o.rotate)
	region = paint_region(base, mask, o)
	sy, sx = sample_coords(base.shape, mat.shape, region, o)
	sampled = mat[sy, sx]
	if o.material_alpha == "skip":
		region = region & (sampled[:, :, 3] > 0)
	lum = base[:, :, 0] * 0.299 + base[:, :, 1] * 0.587 + base[:, :, 2] * 0.114
	mean = lum[region].mean() if region.any() else 1.0
	f = 1.0 + (lum / (mean or 1.0) - 1.0) * float(o.strength)
	result = np.zeros(base.shape, dtype=np.float32) if o.clip else base.copy()
	result[region, :3] = sampled[region, :3] * f[region][:, None]
	result[region, 3] = base[region, 3]
	return result


def paint_file(base_path, material_path, out_path, opts=None, mask_path=None):
	"""Paint two files and save raw RGBA (for core/batch.py). Returns out_path."""
	base = load_rgba_array(base_path)
	mask = load_rgba_array(mask_path) if mask_path else None
	save_raw(paint(base, load_rgba_array(material_path), opts, mask), out_path)
	return out_path


# ---------------------------------------------------------------- bulk (core/batch.py kind "paint", P8)

def _stem(path):
	return os.path.splitext(os.path.basename(path))[0]


def paint_jobs(fixed, folder, reverse=False, opts=None, mask=None, pattern="{name}.png", include=None, exclude=None,
			   recursive=False):
	"""Bulk paint jobs (kind "paint"; run with batch.run_batch, saver save_raw).

	reverse False: fixed is the shape and every image in folder is a material (one shape, many materials).
	reverse True: fixed is the material and every image in folder is a shape (one material, many shapes).
	folder: a folder (scanned with include / exclude globs) or a list of paths. The fixed file itself is left out.
	mask: optional mask path shared by every job (a shape of another size skips).
	Name fields: {name} the varying file's stem, {ext}, {shape} shape stem, {material} material stem, {n}."""
	from texturetools.core import batch
	files = batch.scan_folder(folder, include, exclude, recursive) if isinstance(folder, str) else list(folder)
	fixed_abs = os.path.abspath(fixed)
	files = [f for f in files if os.path.abspath(f) != fixed_abs]
	params = {"options": (opts or PaintOptions()).validate().to_dict(), "mask": mask or ""}
	jobs = []
	for n, path in enumerate(files, 1):
		f = batch.name_fields(path)
		f["n"] = n
		if reverse:
			inputs = {"shape": path, "material": fixed}
			f["shape"], f["material"] = f["name"], _stem(fixed)
		else:
			inputs = {"shape": fixed, "material": path}
			f["shape"], f["material"] = _stem(fixed), f["name"]
		jobs.append(batch.Job(inputs=inputs, params=dict(params), pattern=pattern, fields=f, kind="paint",
							  label=os.path.basename(path)))
	return jobs


def _paint_worker(job):
	"""Job kind "paint": inputs shape and material (path or array), params options (PaintOptions dict) and mask
	(path, or ""). A size mismatch with the mask becomes a Skip."""
	from texturetools.core import batch
	opts = PaintOptions.from_dict(job.params.get("options"))
	shape, mat = job.inputs["shape"], job.inputs["material"]
	shape_arr = load_rgba_array(shape) if isinstance(shape, str) else np.asarray(shape)
	mat_arr = load_rgba_array(mat) if isinstance(mat, str) else np.asarray(mat)
	mask_path = job.params.get("mask")
	mask = load_rgba_array(mask_path) if mask_path else None
	try:
		return paint(shape_arr, mat_arr, opts, mask)
	except ValueError as e:
		raise batch.Skip(str(e))


def _register_kind():
	from texturetools.core import batch
	batch.register_kind("paint", _paint_worker)


_register_kind()

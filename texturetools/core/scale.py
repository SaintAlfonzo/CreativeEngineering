# SPDX-License-Identifier: GPL-3.0-only
"""Integer nearest-neighbor scaling: whole-number factor, trim, canvas (scale_tex.py) and the folder batch
upscale (upscale_sprites.py). Behavior unchanged (plan_texture_tools_gui.md P0).

P8 (plan section 7, one implementation): scale_pipeline() is scale_tex.py's single-image path as one call (the Scale
screen's preview and Run), upscale_pil() / upscale_plan() are upscale_sprites.py's per-image resize and folder plan,
and the array functions scale_array, downscale_array, trim_array back the Texture Editor's Scale and Trim buttons
(core/edit.py re-exports them under its old names).
"""

import os
from pathlib import Path

import numpy as np
from PIL import Image


def parse_size(text):
	parts = text.lower().split("x")
	if len(parts) == 1:
		parts = parts * 2
	if len(parts) != 2:
		raise ValueError(text)
	return int(parts[0]), int(parts[1])


def resolve_factor(src_size, factor=None, size=None):
	"""Return the integer factor, or raise ValueError with a reason."""
	w, h = src_size
	if factor is not None:
		if factor < 1:
			raise ValueError("factor must be 1 or more")
		return factor
	tw, th = size
	if tw % w or th % h:
		raise ValueError(
			"{0}x{1} is not a whole-number multiple of {2}x{3}".format(tw, th, w, h)
		)
	fx, fy = tw // w, th // h
	if fx != fy:
		raise ValueError(
			"{0}x{1} would scale the sides unevenly ({2}x and {3}x)".format(tw, th, fx, fy)
		)
	if fx < 1:
		raise ValueError("target is smaller than the source")
	return fx


def scale_image(img, factor):
	w, h = img.size
	out = img.resize((w * factor, h * factor), Image.NEAREST)
	out.info = {}
	if "transparency" in img.info:  # palette transparency index
		out.info["transparency"] = img.info["transparency"]
	return out


def trim(img):
	box = img.convert("RGBA").split()[3].getbbox()
	return img.crop(box) if box else img


def fit_factor(src_size, canvas):
	f = min(canvas[0] // src_size[0], canvas[1] // src_size[1])
	if f < 1:
		raise ValueError("source is larger than the canvas")
	return f


def place_on_canvas(img, canvas, anchor):
	cw, ch = canvas
	w, h = img.size
	if w > cw or h > ch:
		raise ValueError("scaled image {0}x{1} is larger than the canvas {2}x{3}".format(w, h, cw, ch))
	x, y = ((cw - w) // 2, (ch - h) // 2) if anchor == "center" else (0, 0)
	out = Image.new("RGBA", canvas, (0, 0, 0, 0))
	out.paste(img.convert("RGBA"), (x, y))
	return out


def default_output(path, size):
	stem, _ = os.path.splitext(path)
	w, h = size
	tag = str(w) if w == h else "{0}x{1}".format(w, h)
	return "{0}_{1}.png".format(stem, tag)


SUPPORTED_EXTS = {".png", ".jpg", ".jpeg", ".bmp", ".gif", ".tga", ".webp"}


def find_images(input_dir: Path, recursive: bool):
	pattern = "**/*" if recursive else "*"
	for path in sorted(input_dir.glob(pattern)):
		if path.is_file() and path.suffix.lower() in SUPPORTED_EXTS:
			yield path


def upscale_pil(img, factor):
	"""upscale_sprites.py's resize of one opened image (modes with alpha become RGBA, others are kept)."""
	img = img.convert("RGBA") if img.mode != "RGBA" and "A" in img.mode else img
	new_size = (img.width * factor, img.height * factor)
	# NEAREST preserves hard pixel edges -- essential for pixel art sprites.
	return img.resize(new_size, resample=Image.NEAREST)


def upscale_image(src_path: Path, factor: int, dest_path: Path):
	with Image.open(src_path) as img:
		upscaled = upscale_pil(img, factor)
		dest_path.parent.mkdir(parents=True, exist_ok=True)
		upscaled.save(dest_path)


def upscale_output_dir(input_dir: Path, output_dir=None):
	"""upscale_sprites.py's default output folder: <input_dir>_upscaled beside the input folder."""
	input_dir = Path(input_dir)
	return Path(output_dir) if output_dir else input_dir.parent / "{0}_upscaled".format(input_dir.name)


def upscale_factors(factors):
	"""The factors upscale_sprites.py uses: sorted, unique, only those above 1."""
	return sorted(set(int(f) for f in factors if int(f) > 1))


def upscale_plan(input_dir, output_dir, factors, recursive=False, flat=False):
	"""Every (src_path, rel_path, factor, dest_path) upscale_sprites.py writes, in its order (image, then factor).
	output_dir: as resolved by upscale_output_dir; factors: as filtered by upscale_factors."""
	input_dir, output_dir = Path(input_dir), Path(output_dir)
	plan = []
	for src_path in find_images(input_dir, recursive):
		rel_path = src_path.relative_to(input_dir)
		for factor in factors:
			if flat:
				dest_path = output_dir / "{0}_x{1}{2}".format(rel_path.stem, factor, rel_path.suffix)
			else:
				dest_path = output_dir / "x{0}".format(factor) / rel_path
			plan.append((src_path, rel_path, factor, dest_path))
	return plan


# ---------------------------------------------------------------- scale_tex.py as one call (P8)

def scale_pipeline(img, factor=None, size=None, do_trim=False, canvas=None, anchor="topleft"):
	"""scale_tex.py's single-image path: trim, factor (given, from a target size, or the largest that fits the
	canvas), nearest scale, optional canvas. size and canvas are (w, h) tuples or None. Returns (out, info) with
	info {"trimmed": (w, h) after trim, "factor": n}. Raises ValueError with scale_tex.py's reasons."""
	if factor is None and size is None and canvas is None:
		raise ValueError("give a factor, a target size or a canvas")
	src = trim(img) if do_trim else img
	if factor is None and size is None:
		f = fit_factor(src.size, canvas)
	else:
		f = resolve_factor(src.size, factor, size)
	out = scale_image(src, f)
	if canvas:
		out = place_on_canvas(out, canvas, anchor)
	return out, {"trimmed": src.size, "factor": f}


def pil_to_rgba(img):
	"""HxWx4 uint8 array of any PIL image (for previews)."""
	return np.array(img.convert("RGBA"))


# ---------------------------------------------------------------- array versions (Texture Editor, P8)

def scale_array(arr, factor):
	"""Nearest-neighbor scale of an HxWxC array by a whole factor (every pixel becomes a factor x factor block);
	the same pixels scale_image gives."""
	if int(factor) != factor or factor < 1:
		raise ValueError("factor must be a whole number of 1 or more")
	f = int(factor)
	return np.repeat(np.repeat(arr, f, axis=0), f, axis=1)


def downscale_array(arr, factor):
	"""Inverse of scale_array: keep the top-left pixel of every factor x factor block (size must divide)."""
	if int(factor) != factor or factor < 1:
		raise ValueError("factor must be a whole number of 1 or more")
	f = int(factor)
	h, w = arr.shape[:2]
	if h % f or w % f:
		raise ValueError("{0}x{1} is not a multiple of {2}".format(w, h, f))
	return arr[::f, ::f].copy()


def alpha_bbox(arr):
	"""(x0, y0, x1, y1) inclusive bounds of alpha > 0, or None when fully transparent (trim's box)."""
	ys, xs = np.nonzero(arr[:, :, 3] > 0)
	if not len(ys):
		return None
	return int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())


def trim_array(arr):
	"""Crop to the non-transparent pixels, as trim() (a fully transparent image comes back unchanged, a copy)."""
	box = alpha_bbox(arr)
	if box is None:
		return arr.copy()
	x0, y0, x1, y1 = box
	return arr[y0:y1 + 1, x0:x1 + 1].copy()


# ---------------------------------------------------------------- folder preview jobs (batch kind "upscale", P8)

def upscale_jobs(input_dir, output_dir, factors, recursive=False, flat=False):
	"""One batch.Job per upscale_plan entry (kind "upscale"): label "<rel path> x<factor>", output name the destination
	relative to output_dir. For the Scale screen's preview-all grid; the folder Run itself follows upscale_sprites.py
	(upscale_image per plan entry, the image mode kept)."""
	from texturetools.core import batch
	jobs = []
	for n, (src, rel, factor, dest) in enumerate(upscale_plan(input_dir, output_dir, factors, recursive, flat), 1):
		rel_out = os.path.relpath(str(dest), str(output_dir))
		jobs.append(batch.Job(inputs={"source": str(src)}, params={"factor": factor}, pattern="{dest}",
							  fields={"dest": rel_out, "n": n}, kind="upscale",
							  label="{0} x{1}".format(rel.as_posix(), factor)))
	return jobs


def _upscale_worker(job):
	with Image.open(job.inputs["source"]) as img:
		return pil_to_rgba(upscale_pil(img, int(job.params["factor"])))


def _register_kind():
	from texturetools.core import batch
	batch.register_kind("upscale", _upscale_worker)


_register_kind()

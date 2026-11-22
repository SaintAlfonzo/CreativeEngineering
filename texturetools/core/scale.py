# SPDX-License-Identifier: GPL-3.0-only
"""Integer nearest-neighbor scaling: whole-number factor, trim, canvas (scale_tex.py) and the folder batch
upscale (upscale_sprites.py). Behavior unchanged (plan_texture_tools_gui.md P0).
"""

import os
from pathlib import Path

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


def upscale_image(src_path: Path, factor: int, dest_path: Path):
	with Image.open(src_path) as img:
		img = img.convert("RGBA") if img.mode != "RGBA" and "A" in img.mode else img
		new_size = (img.width * factor, img.height * factor)
		# NEAREST preserves hard pixel edges -- essential for pixel art sprites.
		upscaled = img.resize(new_size, resample=Image.NEAREST)
		dest_path.parent.mkdir(parents=True, exist_ok=True)
		upscaled.save(dest_path)

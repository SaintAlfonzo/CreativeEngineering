# SPDX-License-Identifier: GPL-3.0-only
"""Pixel editor engine (plan_texture_tools_gui.md section 6). Headless: every function takes an RGBA array (HxWx4,
uint8 or float) and returns a new array of the same dtype; the caller's array is never changed. Coordinates are
(x, y) with y down; colors are (r, g, b, a) tuples.

Drawing: rasterizers return point lists (pencil, line, rect, ellipse; filled or outline), draw_points paints them with
an optional mirror (x, y, both) and selection limit. Fills: bucket (contiguous, tolerance), global bucket (every
pixel within tolerance of the clicked color). Eyedropper. Selections are bool HxW masks: rect, magic wand
(contiguous or global), with add / subtract / intersect. Cut, copy and paste go through a Floating piece that moves
and commits. Transforms: flip, rotate 90/180/270, resize canvas with anchor, integer scale, shift (wrap or not),
trim. Palettes: image palette list (by count), import and export (.gpl, .hex, .txt, .json, or any image).
"""

import json
import os
from collections import deque
from dataclasses import dataclass

import numpy as np
from PIL import Image

from texturetools.core import scale as _scale

TRANSPARENT = (0, 0, 0, 0)
ANCHORS = {
	"top_left": (0, 0), "top": (1, 0), "top_right": (2, 0),
	"left": (0, 1), "center": (1, 1), "right": (2, 1),
	"bottom_left": (0, 2), "bottom": (1, 2), "bottom_right": (2, 2),
}


def _rgba(color):
	c = tuple(color)
	return c + (255,) if len(c) == 3 else c


def blank(w, h, color=TRANSPARENT, dtype=np.uint8):
	"""New w x h canvas filled with color."""
	a = np.zeros((h, w, 4), dtype=dtype)
	a[:] = _rgba(color)
	return a


def in_bounds(img, x, y):
	return 0 <= x < img.shape[1] and 0 <= y < img.shape[0]


# ---------------------------------------------------------------- rasterizers

def line_points(x0, y0, x1, y1):
	"""Bresenham line, both ends included, in drawing order."""
	pts = []
	dx, dy = abs(x1 - x0), -abs(y1 - y0)
	sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
	err = dx + dy
	x, y = x0, y0
	while True:
		pts.append((x, y))
		if x == x1 and y == y1:
			return pts
		e2 = 2 * err
		if e2 >= dy:
			err += dy
			x += sx
		if e2 <= dx:
			err += dx
			y += sy


def rect_points(x0, y0, x1, y1, filled=False):
	"""Rectangle between two corners (any order), both included."""
	xa, xb = sorted((x0, x1))
	ya, yb = sorted((y0, y1))
	if filled:
		return [(x, y) for y in range(ya, yb + 1) for x in range(xa, xb + 1)]
	pts = set()
	for x in range(xa, xb + 1):
		pts.add((x, ya))
		pts.add((x, yb))
	for y in range(ya, yb + 1):
		pts.add((xa, y))
		pts.add((xb, y))
	return sorted(pts, key=lambda p: (p[1], p[0]))


def ellipse_points(x0, y0, x1, y1, filled=False):
	"""Ellipse inscribed in the box between two corners (any order), both included; symmetric on both axes.
	Pixel centers inside the ellipse (filled), or the filled set's edge pixels (outline, 4-connected border)."""
	xa, xb = sorted((x0, x1))
	ya, yb = sorted((y0, y1))
	w, h = xb - xa + 1, yb - ya + 1
	cx, cy = (w - 1) / 2.0, (h - 1) / 2.0
	rx, ry = w / 2.0, h / 2.0
	yy, xx = np.mgrid[0:h, 0:w]
	inside = ((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2 <= 1.0
	if not filled:
		p = np.pad(inside, 1)
		interior = p[:-2, 1:-1] & p[2:, 1:-1] & p[1:-1, :-2] & p[1:-1, 2:]
		inside = inside & ~interior
	ys, xs = np.nonzero(inside)
	return [(int(x) + xa, int(y) + ya) for y, x in zip(ys, xs)]


def mirror_points(pts, w, h, mirror=None):
	"""Add the mirror images of pts across the canvas's vertical (mirror 'x'), horizontal ('y') or both axes."""
	if not mirror:
		return list(pts)
	out = list(pts)
	if mirror in ("x", "both"):
		out += [(w - 1 - x, y) for x, y in pts]
	if mirror in ("y", "both"):
		out += [(x, h - 1 - y) for x, y in pts]
	if mirror == "both":
		out += [(w - 1 - x, h - 1 - y) for x, y in pts]
	if mirror not in ("x", "y", "both"):
		raise ValueError("mirror must be x, y or both")
	return list(dict.fromkeys(out))


def draw_points(img, pts, color, mirror=None, selection=None):
	"""Paint pts with color (replace, not blend; the eraser is color (0, 0, 0, 0)). Points off the canvas are
	ignored; with a selection mask only selected pixels change."""
	out = img.copy()
	h, w = img.shape[:2]
	c = np.asarray(_rgba(color), dtype=img.dtype)
	for x, y in mirror_points(pts, w, h, mirror):
		if 0 <= x < w and 0 <= y < h and (selection is None or selection[y, x]):
			out[y, x] = c
	return out


def pencil(img, x, y, color, prev=None, mirror=None, selection=None):
	"""One pencil step; prev (x, y) joins a drag with a line so fast strokes have no gaps."""
	pts = line_points(prev[0], prev[1], x, y) if prev else [(x, y)]
	return draw_points(img, pts, color, mirror, selection)


def line(img, x0, y0, x1, y1, color, mirror=None, selection=None):
	return draw_points(img, line_points(x0, y0, x1, y1), color, mirror, selection)


def rect(img, x0, y0, x1, y1, color, filled=False, mirror=None, selection=None):
	return draw_points(img, rect_points(x0, y0, x1, y1, filled), color, mirror, selection)


def ellipse(img, x0, y0, x1, y1, color, filled=False, mirror=None, selection=None):
	return draw_points(img, ellipse_points(x0, y0, x1, y1, filled), color, mirror, selection)


# ---------------------------------------------------------------- color matching, fills, eyedropper

def eyedropper(img, x, y):
	"""Color at (x, y) as an int tuple."""
	return tuple(int(round(float(v))) for v in img[y, x])


def color_match(img, color, tolerance=0):
	"""bool HxW: pixels whose every channel (RGBA) is within tolerance of color. tolerance 0 = exact match."""
	c = np.asarray(_rgba(color), dtype=np.int32)
	return (np.abs(img.astype(np.int32) - c) <= tolerance).all(axis=2)


def connected_region(match, x, y, diagonal=False):
	"""bool HxW: the part of match connected to (x, y) (4-connected, or 8 with diagonal)."""
	h, w = match.shape
	region = np.zeros_like(match, dtype=bool)
	if not (0 <= x < w and 0 <= y < h) or not match[y, x]:
		return region
	steps = [(1, 0), (-1, 0), (0, 1), (0, -1)]
	if diagonal:
		steps += [(1, 1), (1, -1), (-1, 1), (-1, -1)]
	region[y, x] = True
	q = deque([(x, y)])
	while q:
		cx, cy = q.popleft()
		for dx, dy in steps:
			nx, ny = cx + dx, cy + dy
			if 0 <= nx < w and 0 <= ny < h and match[ny, nx] and not region[ny, nx]:
				region[ny, nx] = True
				q.append((nx, ny))
	return region


def fill_mask(img, mask, color):
	"""Every pixel of mask set to color."""
	out = img.copy()
	out[mask] = np.asarray(_rgba(color), dtype=img.dtype)
	return out


def bucket_fill(img, x, y, color, tolerance=0, diagonal=False, selection=None):
	"""Contiguous fill from (x, y) over pixels within tolerance of the clicked pixel's color."""
	region = connected_region(color_match(img, img[y, x], tolerance), x, y, diagonal)
	if selection is not None:
		region &= selection
	return fill_mask(img, region, color)


def global_fill(img, x, y, color, tolerance=0, selection=None):
	"""Global bucket: every pixel within tolerance of the clicked pixel's color, connected or not."""
	return replace_color(img, img[y, x], color, tolerance, selection)


def replace_color(img, old, new, tolerance=0, selection=None):
	"""Every pixel within tolerance of old becomes new (image palette strip: bucket on a swatch)."""
	m = color_match(img, old, tolerance)
	if selection is not None:
		m &= selection
	return fill_mask(img, m, new)


# ---------------------------------------------------------------- selections

def select_rect(shape, x0, y0, x1, y1):
	"""bool mask (shape = img.shape or (h, w)) of the rectangle between two corners, clipped to the canvas."""
	h, w = shape[:2]
	m = np.zeros((h, w), dtype=bool)
	xa, xb = sorted((x0, x1))
	ya, yb = sorted((y0, y1))
	m[max(0, ya):max(0, yb + 1), max(0, xa):max(0, xb + 1)] = True
	return m


def magic_wand(img, x, y, tolerance=0, contiguous=True, diagonal=False):
	"""bool mask of pixels like the clicked one (connected to it, or anywhere with contiguous=False)."""
	m = color_match(img, img[y, x], tolerance)
	return connected_region(m, x, y, diagonal) if contiguous else m


def combine_selection(a, b, op="replace"):
	"""op: replace (b), add (a | b), subtract (a & ~b), intersect (a & b). a may be None."""
	if a is None or op == "replace":
		return b.copy()
	if op == "add":
		return a | b
	if op == "subtract":
		return a & ~b
	if op == "intersect":
		return a & b
	raise ValueError("op must be replace, add, subtract or intersect")


def select_all(img):
	return np.ones(img.shape[:2], dtype=bool)


def selection_bbox(mask):
	"""(x0, y0, x1, y1) inclusive bounds of a mask, or None when empty."""
	ys, xs = np.nonzero(mask)
	if not len(ys):
		return None
	return int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())


@dataclass
class Floating:
	"""A lifted piece: pixels (hxwx4, unselected pixels transparent), mask (hxw), and its top-left (x, y) on the
	canvas. Move it with moved(); commit_floating() pastes it."""
	pixels: np.ndarray
	mask: np.ndarray
	x: int = 0
	y: int = 0

	def moved(self, dx, dy):
		return Floating(self.pixels, self.mask, self.x + dx, self.y + dy)

	@property
	def size(self):
		return self.pixels.shape[1], self.pixels.shape[0]


def copy_selection(img, mask):
	"""Floating copy of the selected pixels at their own place, or None for an empty selection."""
	box = selection_bbox(mask)
	if box is None:
		return None
	x0, y0, x1, y1 = box
	m = mask[y0:y1 + 1, x0:x1 + 1].copy()
	px = img[y0:y1 + 1, x0:x1 + 1].copy()
	px[~m] = 0
	return Floating(px, m, x0, y0)


def cut_selection(img, mask, fill=TRANSPARENT):
	"""(image with the selection filled with fill, Floating piece)."""
	return fill_mask(img, mask, fill), copy_selection(img, mask)


def paste(pixels, x=0, y=0):
	"""Floating piece from a clipboard array (all its pixels, transparent ones included, are part of it)."""
	p = np.asarray(pixels)
	return Floating(p.copy(), np.ones(p.shape[:2], dtype=bool), x, y)


def commit_floating(img, floating, blend=False):
	"""Paste floating onto img at its (x, y), clipped at the edges. blend False replaces the masked pixels
	(transparent ones too); blend True draws them over the image (alpha 'over', straight alpha)."""
	out = img.copy()
	h, w = img.shape[:2]
	fh, fw = floating.pixels.shape[:2]
	x, y = floating.x, floating.y
	sx0, sy0 = max(0, -x), max(0, -y)
	dx0, dy0 = max(0, x), max(0, y)
	cw, ch = min(fw - sx0, w - dx0), min(fh - sy0, h - dy0)
	if cw <= 0 or ch <= 0:
		return out
	src = floating.pixels[sy0:sy0 + ch, sx0:sx0 + cw].astype(np.float64)
	m = floating.mask[sy0:sy0 + ch, sx0:sx0 + cw]
	dst = out[dy0:dy0 + ch, dx0:dx0 + cw]
	if blend:
		d = dst.astype(np.float64)
		sa, da = src[..., 3:4] / 255.0, d[..., 3:4] / 255.0
		oa = sa + da * (1 - sa)
		rgb = (src[..., :3] * sa + d[..., :3] * da * (1 - sa)) / np.where(oa == 0, 1, oa)
		src = np.concatenate([rgb, oa * 255.0], axis=2)
		if np.issubdtype(img.dtype, np.integer):
			src = np.clip(np.round(src), 0, 255)
	dst[m] = src[m].astype(img.dtype)
	return out


# ---------------------------------------------------------------- transforms

def flip(img, axis="h"):
	"""axis 'h' mirrors left-right, 'v' top-bottom."""
	if axis == "h":
		return img[:, ::-1].copy()
	if axis == "v":
		return img[::-1].copy()
	raise ValueError("axis must be h or v")


def rotate(img, degrees=90):
	"""Clockwise by 90, 180 or 270 degrees."""
	if degrees % 90:
		raise ValueError("rotate takes 90, 180 or 270")
	return np.rot90(img, -(degrees // 90) % 4).copy()


def resize_canvas(img, w, h, anchor="top_left", fill=TRANSPARENT):
	"""New w x h canvas with img placed at anchor (cropped when smaller); new area filled with fill."""
	if anchor not in ANCHORS:
		raise ValueError("anchor must be one of {0}".format(", ".join(ANCHORS)))
	out = blank(w, h, fill, img.dtype)
	ih, iw = img.shape[:2]
	ax, ay = ANCHORS[anchor]
	x, y = (w - iw) * ax // 2, (h - ih) * ay // 2
	return commit_floating(out, paste(img, x, y))


def scale_int(img, factor):
	"""Nearest-neighbor scale by a whole factor (every pixel becomes a factor x factor block): core/scale.py's
	scale_array, the one implementation the Scale screen shares (P8)."""
	return _scale.scale_array(img, factor)


def downscale_int(img, factor):
	"""Inverse of scale_int: keep the top-left pixel of every factor x factor block (size must divide);
	core/scale.py's downscale_array."""
	return _scale.downscale_array(img, factor)


def shift(img, dx, dy, wrap=True, fill=TRANSPARENT):
	"""Move the image by whole pixels; wrap re-enters pixels on the other side, else the gap is filled."""
	if wrap:
		return np.roll(np.roll(img, dy, axis=0), dx, axis=1)
	out = blank(img.shape[1], img.shape[0], fill, img.dtype)
	return commit_floating(out, paste(img, dx, dy))


def trim_bbox(img):
	"""(x0, y0, x1, y1) inclusive bounds of alpha > 0, or None when fully transparent (core/scale.py alpha_bbox)."""
	return _scale.alpha_bbox(img)


def trim(img):
	"""Crop to the non-transparent pixels (a fully transparent image is returned unchanged, as a copy);
	core/scale.py's trim_array."""
	return _scale.trim_array(img)


def crop(img, x0, y0, x1, y1):
	"""Crop to the inclusive box (clipped to the canvas)."""
	h, w = img.shape[:2]
	xa, xb = sorted((x0, x1))
	ya, yb = sorted((y0, y1))
	return img[max(0, ya):min(h, yb + 1), max(0, xa):min(w, xb + 1)].copy()


# ---------------------------------------------------------------- palettes

def image_palette(img, include_transparent=False):
	"""[(color, count)] of every distinct RGBA color in the image, most used first (ties by color).
	Fully transparent pixels are left out unless include_transparent."""
	px = np.round(img.reshape(-1, 4)).astype(np.int64)
	if not include_transparent:
		px = px[px[:, 3] > 0]
	if not len(px):
		return []
	cols, counts = np.unique(px, axis=0, return_counts=True)
	order = sorted(range(len(cols)), key=lambda i: (-counts[i], tuple(cols[i])))
	return [(tuple(int(v) for v in cols[i]), int(counts[i])) for i in order]


def image_colors(img, include_transparent=False):
	"""Just the colors of image_palette."""
	return [c for c, _ in image_palette(img, include_transparent)]


def _hex(c):
	c = _rgba(c)
	return "{0:02x}{1:02x}{2:02x}".format(*c[:3]) + ("{0:02x}".format(c[3]) if c[3] != 255 else "")


def _parse_hex(t):
	t = t.strip().lstrip("#")
	if len(t) not in (6, 8):
		raise ValueError("bad hex color {0!r}".format(t))
	v = [int(t[i:i + 2], 16) for i in range(0, len(t), 2)]
	return tuple(v + [255] if len(v) == 3 else v)


def export_palette(entries, path, title="Texture Tools palette"):
	"""Write a palette. entries: colors, or (color, name) pairs. Format by extension: .gpl (GIMP, names kept, alpha
	dropped), .hex / .txt (one rrggbb[aa] per line), .json ([{"color": "#rrggbb[aa]", "name": ...}])."""
	rows = [(e[0], e[1]) if len(e) == 2 and isinstance(e[1], str) else (tuple(e), "") for e in entries]
	ext = os.path.splitext(path)[1].lower()
	if ext == ".gpl":
		lines = ["GIMP Palette", "Name: {0}".format(title), "Columns: 8", "#"]
		for c, name in rows:
			c = _rgba(c)
			lines.append("{0:3d} {1:3d} {2:3d}\t{3}".format(c[0], c[1], c[2], name or _hex(c)))
		text = "\n".join(lines) + "\n"
	elif ext in (".hex", ".txt"):
		text = "".join(_hex(c) + "\n" for c, _ in rows)
	elif ext == ".json":
		text = json.dumps([{"color": "#" + _hex(c), "name": n} for c, n in rows], indent=1) + "\n"
	else:
		raise ValueError("palette export takes .gpl, .hex, .txt or .json")
	with open(path, "w", encoding="utf-8") as fh:
		fh.write(text)


def import_palette(path):
	"""[(color, name)] from .gpl, .hex, .txt, .json, or any image (its image_palette colors, names empty)."""
	ext = os.path.splitext(path)[1].lower()
	if ext == ".gpl":
		out = []
		with open(path, encoding="utf-8") as fh:
			for line in fh:
				s = line.strip()
				if not s or s.startswith("#") or s.startswith("GIMP") or ":" in s.split("\t")[0] and not s[0].isdigit():
					continue
				parts = s.split(None, 3)
				if len(parts) < 3 or not all(p.isdigit() for p in parts[:3]):
					continue
				out.append(((int(parts[0]), int(parts[1]), int(parts[2]), 255), parts[3] if len(parts) > 3 else ""))
		return out
	if ext in (".hex", ".txt"):
		with open(path, encoding="utf-8") as fh:
			return [(_parse_hex(l), "") for l in fh if l.strip() and not l.strip().startswith(";")]
	if ext == ".json":
		with open(path, encoding="utf-8") as fh:
			return [(_parse_hex(e["color"]), e.get("name", "")) for e in json.load(fh)]
	with Image.open(path) as im:
		arr = np.array(im.convert("RGBA"))
	return [(c, "") for c in image_colors(arr)]

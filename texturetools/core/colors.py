# SPDX-License-Identifier: GPL-3.0-only
"""Color math for the editor's color panel and the engines (plan_texture_tools_gui.md sections 6, 2).

Colors are 0..255 integer tuples (r, g, b) or (r, g, b, a); alpha passes through every function untouched.
Conversions: HSV and HSL (h in degrees 0..360, s, v, l in 0..1), CIE Lab (D65, sRGB transfer, used only for color
distance and sorting; outputs are never color managed). Scalar functions for the panel, *_arr versions for arrays
(... x 3, any float or int dtype, 0..255).

Panel helpers: hex parse and format, brightness, nudge (lighter, darker, saturation, hue with a step), suggest
(six steps per axis: R, G, B, toward white, toward black, by one delta, clamped) and harmonies (complementary,
triadic, analogous at the same HSL lightness).
"""

import colorsys

import numpy as np

SUGGEST_AXES = ("r", "g", "b", "white", "black")
NUDGES = ("lighter", "darker", "saturate", "desaturate", "hue_left", "hue_right")


def _split(c):
	c = tuple(int(round(v)) for v in c)
	if len(c) not in (3, 4):
		raise ValueError("color must have 3 or 4 channels: {0!r}".format(c))
	return c[:3], c[3:]


def clamp(c):
	"""Round and clamp every channel to 0..255 (keeps 3 or 4 channels)."""
	return tuple(min(255, max(0, int(round(v)))) for v in c)


# ---------------------------------------------------------------- hex

def to_hex(c):
	"""'#rrggbb', or '#rrggbbaa' for a 4-tuple whose alpha is not 255."""
	c = clamp(c)
	if len(c) == 4 and c[3] != 255:
		return "#{0:02x}{1:02x}{2:02x}{3:02x}".format(*c)
	return "#{0:02x}{1:02x}{2:02x}".format(*c[:3])


def from_hex(text):
	"""'#rgb', '#rrggbb' or '#rrggbbaa' (the '#' optional) -> (r, g, b, a)."""
	t = text.strip().lstrip("#")
	if len(t) == 3:
		t = "".join(ch * 2 for ch in t)
	if len(t) not in (6, 8):
		raise ValueError("hex color must be rgb, rrggbb or rrggbbaa: {0}".format(text))
	v = [int(t[i:i + 2], 16) for i in range(0, len(t), 2)]
	return tuple(v + [255] if len(v) == 3 else v)


# ---------------------------------------------------------------- scalar conversions

def rgb_to_hsv(c):
	"""(r, g, b[, a]) -> (h 0..360, s 0..1, v 0..1)."""
	(r, g, b), _ = _split(c)
	h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
	return h * 360.0, s, v


def hsv_to_rgb(h, s, v, alpha=None):
	r, g, b = colorsys.hsv_to_rgb((h % 360.0) / 360.0, min(1.0, max(0.0, s)), min(1.0, max(0.0, v)))
	out = clamp((r * 255, g * 255, b * 255))
	return out + (int(alpha),) if alpha is not None else out


def rgb_to_hsl(c):
	"""(r, g, b[, a]) -> (h 0..360, s 0..1, l 0..1)."""
	(r, g, b), _ = _split(c)
	h, l, s = colorsys.rgb_to_hls(r / 255.0, g / 255.0, b / 255.0)
	return h * 360.0, s, l


def hsl_to_rgb(h, s, l, alpha=None):
	r, g, b = colorsys.hls_to_rgb((h % 360.0) / 360.0, min(1.0, max(0.0, l)), min(1.0, max(0.0, s)))
	out = clamp((r * 255, g * 255, b * 255))
	return out + (int(alpha),) if alpha is not None else out


def rgb_to_lab(c):
	(r, g, b), _ = _split(c)
	return tuple(float(v) for v in rgb_to_lab_arr(np.array([r, g, b], dtype=np.float64)))


def lab_to_rgb(L, a, b, alpha=None):
	out = clamp(lab_to_rgb_arr(np.array([L, a, b], dtype=np.float64)))
	return out + (int(alpha),) if alpha is not None else out


def brightness(c):
	"""Perceived brightness 0..255 (Rec. 601 luma, the weights combine paint uses)."""
	(r, g, b), _ = _split(c)
	return r * 0.299 + g * 0.587 + b * 0.114


def distance(c1, c2):
	"""Euclidean distance in Lab (delta E 1976)."""
	l1, l2 = rgb_to_lab(c1), rgb_to_lab(c2)
	return float(np.sqrt(sum((x - y) ** 2 for x, y in zip(l1, l2))))


# ---------------------------------------------------------------- array conversions

def rgb_to_hsv_arr(rgb):
	"""... x 3 RGB 0..255 -> ... x 3 (h 0..360, s 0..1, v 0..1), float64."""
	a = np.asarray(rgb, dtype=np.float64) / 255.0
	r, g, b = a[..., 0], a[..., 1], a[..., 2]
	mx, mn = a.max(axis=-1), a.min(axis=-1)
	d = mx - mn
	safe = np.where(d == 0, 1, d)
	h = np.where(mx == r, ((g - b) / safe) % 6, np.where(mx == g, (b - r) / safe + 2, (r - g) / safe + 4))
	h = np.where(d == 0, 0, h) * 60.0
	s = np.where(mx == 0, 0, d / np.where(mx == 0, 1, mx))
	return np.stack([h, s, mx], axis=-1)


def hsv_to_rgb_arr(hsv):
	"""... x 3 (h, s, v) -> ... x 3 RGB 0..255 float64 (round and clip before storing)."""
	a = np.asarray(hsv, dtype=np.float64)
	h = (a[..., 0] % 360.0) / 60.0
	s, v = np.clip(a[..., 1], 0, 1), np.clip(a[..., 2], 0, 1)
	i = np.floor(h).astype(int) % 6
	f = h - np.floor(h)
	p, q, t = v * (1 - s), v * (1 - s * f), v * (1 - s * (1 - f))
	choices = [np.stack(x, axis=-1) for x in ((v, t, p), (q, v, p), (p, v, t), (p, q, v), (t, p, v), (v, p, q))]
	out = np.zeros(a.shape, dtype=np.float64)
	for k in range(6):
		out = np.where((i == k)[..., None], choices[k], out)
	return out * 255.0


def rgb_to_hsl_arr(rgb):
	"""... x 3 RGB 0..255 -> ... x 3 (h 0..360, s 0..1, l 0..1)."""
	a = np.asarray(rgb, dtype=np.float64) / 255.0
	mx, mn = a.max(axis=-1), a.min(axis=-1)
	l = (mx + mn) / 2
	d = mx - mn
	den = np.where(d == 0, 1, 1 - np.abs(2 * l - 1))
	s = np.where(d == 0, 0, d / np.where(den == 0, 1, den))
	h = rgb_to_hsv_arr(rgb)[..., 0]
	return np.stack([h, s, l], axis=-1)


def hsl_to_rgb_arr(hsl):
	a = np.asarray(hsl, dtype=np.float64)
	s, l = np.clip(a[..., 1], 0, 1), np.clip(a[..., 2], 0, 1)
	v = l + s * np.minimum(l, 1 - l)
	sv = np.where(v == 0, 0, 2 * (1 - l / np.where(v == 0, 1, v)))
	return hsv_to_rgb_arr(np.stack([a[..., 0], sv, v], axis=-1))


_M = np.array([[0.4124564, 0.3575761, 0.1804375], [0.2126729, 0.7151522, 0.0721750], [0.0193339, 0.1191920, 0.9503041]])
_M_INV = np.linalg.inv(_M)
_WHITE = np.array([0.95047, 1.0, 1.08883])


def rgb_to_lab_arr(rgb):
	"""... x 3 RGB 0..255 -> ... x 3 Lab (L 0..100), D65."""
	c = np.asarray(rgb, dtype=np.float64) / 255.0
	lin = np.where(c <= 0.04045, c / 12.92, ((c + 0.055) / 1.055) ** 2.4)
	xyz = lin @ _M.T / _WHITE
	e = 216 / 24389.0
	f = np.where(xyz > e, np.cbrt(xyz), (24389 / 27.0 * xyz + 16) / 116.0)
	return np.stack([116 * f[..., 1] - 16, 500 * (f[..., 0] - f[..., 1]), 200 * (f[..., 1] - f[..., 2])], axis=-1)


def lab_to_rgb_arr(lab):
	"""... x 3 Lab -> ... x 3 RGB 0..255 float64 (out-of-gamut values clipped)."""
	a = np.asarray(lab, dtype=np.float64)
	fy = (a[..., 0] + 16) / 116.0
	fx, fz = fy + a[..., 1] / 500.0, fy - a[..., 2] / 200.0
	f = np.stack([fx, fy, fz], axis=-1)
	e = 6 / 29.0
	xyz = np.where(f > e, f ** 3, 3 * e * e * (f - 4 / 29.0)) * _WHITE
	lin = np.clip(xyz @ _M_INV.T, 0, 1)
	c = np.where(lin <= 0.0031308, lin * 12.92, 1.055 * lin ** (1 / 2.4) - 0.055)
	return np.clip(c * 255.0, 0, 255)


# ---------------------------------------------------------------- nudge, suggest, harmonies

def nudge(c, how, step=0.05):
	"""One nudge button. how: 'lighter' (each channel moves step of the way to 255), 'darker' (step of the way to
	0), 'saturate' / 'desaturate' (HSV saturation +- step), 'hue_left' / 'hue_right' (hue -+ step * 360 degrees).
	step 0..1 (default 5%). Always in gamut; alpha kept."""
	rgb, alpha = _split(c)
	if how == "lighter":
		out = clamp(v + (255 - v) * step for v in rgb)
	elif how == "darker":
		out = clamp(v * (1 - step) for v in rgb)
	elif how in ("saturate", "desaturate", "hue_left", "hue_right"):
		h, s, v = rgb_to_hsv(rgb)
		if how == "saturate":
			s = min(1.0, s + step)
		elif how == "desaturate":
			s = max(0.0, s - step)
		else:
			h = h + (360.0 * step if how == "hue_right" else -360.0 * step)
		out = hsv_to_rgb(h, s, v)
	else:
		raise ValueError("nudge must be one of {0}".format(", ".join(NUDGES)))
	return out + alpha


def suggest(c, delta=24, count=6):
	"""Autosuggest rows from color c: dict axis -> list of count colors, for axes r, g, b (steps -k..+k of delta in
	that channel only, k = 1..count/2, darkest first), white and black (every channel k * delta toward 255 or 0,
	k = 1..count). Each value differs from c by exactly k * delta in its axis, clamped to 0..255, so neighboring
	options have the same contrast; clamped duplicates are kept (fixed row length), the GUI may gray them."""
	rgb, alpha = _split(c)
	half = count // 2
	ks = [k for k in range(-half, 0)] + [k for k in range(1, count - half + 1)]
	out = {}
	for ch, axis in enumerate(("r", "g", "b")):
		row = []
		for k in ks:
			v = list(rgb)
			v[ch] = v[ch] + k * delta
			row.append(clamp(v) + alpha)
		out[axis] = row
	out["white"] = [clamp(v + k * delta for v in rgb) + alpha for k in range(1, count + 1)]
	out["black"] = [clamp(v - k * delta for v in rgb) + alpha for k in range(1, count + 1)]
	return out


def harmonies(c):
	"""Hue rotations at the same HSL lightness and saturation: complementary (180), triadic (120, 240),
	analogous (-30, +30). dict name -> list of colors."""
	rgb, alpha = _split(c)
	h, s, l = rgb_to_hsl(rgb)
	rot = lambda d: hsl_to_rgb(h + d, s, l) + alpha
	return {
		"complementary": [rot(180)],
		"triadic": [rot(120), rot(240)],
		"analogous": [rot(-30), rot(30)],
	}


def ramp(c1, c2, steps, space="rgb"):
	"""steps colors from c1 to c2 inclusive, interpolated in rgb, hsv or lab."""
	if steps < 2:
		return [clamp(c1)]
	a1, a2 = _split(c1)[1], _split(c2)[1]
	conv = {"rgb": (lambda x: np.asarray(x, float), lambda x: x),
			"hsv": (rgb_to_hsv_arr, hsv_to_rgb_arr), "lab": (rgb_to_lab_arr, lab_to_rgb_arr)}
	if space not in conv:
		raise ValueError("space must be rgb, hsv or lab")
	fwd, back = conv[space]
	p, q = fwd(np.array(c1[:3], float)), fwd(np.array(c2[:3], float))
	if space == "hsv":
		dh = (q[0] - p[0] + 180) % 360 - 180
		q = q.copy()
		q[0] = p[0] + dh
	out = []
	for i in range(steps):
		t = i / (steps - 1.0)
		rgb = clamp(back(p + (q - p) * t))
		alpha = ()
		if a1 or a2:
			alpha = (int(round((a1[0] if a1 else 255) * (1 - t) + (a2[0] if a2 else 255) * t)),)
		out.append(rgb + alpha)
	return out

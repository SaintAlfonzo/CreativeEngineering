# SPDX-License-Identifier: GPL-3.0-only
"""T5 color math tests (plan_texture_tools_gui.md sections 6, 10): every suggestion differs from the base by exactly
k * delta (clamped) in its own axis only; nudges stay in gamut and move the right way; HSV, HSL and Lab round trips
(scalar and array, the arrays checked against colorsys); harmonies keep HSL lightness; hex and ramp.
"""

import colorsys
import os
import sys

import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT)

from texturetools.core import colors as K  # noqa: E402

results = [0, 0]


def check(ok, what):
	results[0] += 1
	if not ok:
		results[1] += 1
		print("FAIL  " + what)


rng = np.random.default_rng(5)


def clampv(v):
	return min(255, max(0, v))


def suggest_tests():
	edge = [(0, 0, 0), (255, 255, 255), (0, 255, 128), (250, 3, 128), (24, 24, 24)]
	cols = edge + [tuple(int(v) for v in rng.integers(0, 256, 3)) for _ in range(300)]
	for c in cols:
		for delta in (24, 1, 10, 60):
			s = K.suggest(c, delta)
			check(list(s) == list(K.SUGGEST_AXES) and all(len(v) == 6 for v in s.values()), "six per axis %r" % (c,))
			for ch, axis in enumerate("rgb"):
				for k, got in zip((-3, -2, -1, 1, 2, 3), s[axis]):
					want = list(c)
					want[ch] = clampv(c[ch] + k * delta)
					check(got == tuple(want), "axis %s k=%d delta=%d %r -> %r" % (axis, k, delta, c, got))
			for k, got in enumerate(s["white"], 1):
				check(got == tuple(clampv(v + k * delta) for v in c), "white k=%d %r" % (k, c))
			for k, got in enumerate(s["black"], 1):
				check(got == tuple(clampv(v - k * delta) for v in c), "black k=%d %r" % (k, c))
			# equal spacing where not clamped
			for axis in "rgb":
				ch = "rgb".index(axis)
				vals = [c[ch] + k * delta for k in (-3, -2, -1, 1, 2, 3)]
				if all(0 <= v <= 255 for v in vals):
					got = [x[ch] for x in s[axis]]
					check(all(b - a == delta for a, b in zip(got, got[1:3])) and got[3] - got[2] == 2 * delta,
						  "even steps %s %r" % (axis, c))
	check(K.suggest((10, 20, 30, 99))["r"][0][3] == 99, "alpha passes through suggest")


def nudge_tests():
	for _ in range(500):
		c = tuple(int(v) for v in rng.integers(0, 256, 3))
		step = float(rng.choice([0.05, 0.01, 0.2, 0.5, 1.0]))
		x = c
		for how in ("lighter", "darker", "lighter", "saturate", "hue_right", "desaturate", "hue_left", "darker"):
			x = K.nudge(x, how, step)
			check(len(x) == 3 and all(isinstance(v, int) and 0 <= v <= 255 for v in x), "gamut %s %r" % (how, c))
		w = K.nudge(c, "lighter", step)
		d = K.nudge(w, "darker", step)
		check(all(a >= b for a, b in zip(w, c)) and all(0 <= v <= 255 for v in d), "toward white then black %r" % (c,))
		check(all(b <= a for a, b in zip(c, K.nudge(c, "darker", step))), "darker never raises %r" % (c,))
		h0, s0, v0 = K.rgb_to_hsv(c)
		check(K.rgb_to_hsv(K.nudge(c, "saturate", step))[1] >= s0 - 0.01, "saturate %r" % (c,))
		check(K.rgb_to_hsv(K.nudge(c, "desaturate", step))[1] <= s0 + 0.01, "desaturate %r" % (c,))
	check(K.nudge((255, 255, 255), "lighter") == (255, 255, 255) and K.nudge((0, 0, 0), "darker") == (0, 0, 0), "ends")
	check(K.nudge((10, 20, 30, 40), "lighter")[3] == 40, "alpha passes through nudge")
	r = K.nudge((200, 50, 50), "hue_right", 1 / 3.0)
	check(r[1] > r[0], "hue right 120 degrees turns red to green")


def conversion_tests():
	arr = rng.integers(0, 256, (400, 3))
	for c in [tuple(int(v) for v in x) for x in arr[:200]] + [(0, 0, 0), (255, 255, 255), (128, 128, 128)]:
		check(K.hsv_to_rgb(*K.rgb_to_hsv(c)) == c, "hsv round trip %r" % (c,))
		check(K.hsl_to_rgb(*K.rgb_to_hsl(c)) == c, "hsl round trip %r" % (c,))
		lab = K.lab_to_rgb(*K.rgb_to_lab(c))
		check(max(abs(a - b) for a, b in zip(lab, c)) <= 1, "lab round trip %r -> %r" % (c, lab))
		check(K.from_hex(K.to_hex(c)) == c + (255,), "hex round trip %r" % (c,))
	hsv = K.rgb_to_hsv_arr(arr)
	ref = np.array([colorsys.rgb_to_hsv(*(arr[i] / 255.0)) for i in range(len(arr))])
	hd = np.abs(((hsv[:, 0] - ref[:, 0] * 360) + 180) % 360 - 180)
	check(np.all(hd[ref[:, 1] > 0] < 1e-6) and np.allclose(hsv[:, 1:], ref[:, 1:]), "hsv array = colorsys")
	check(np.allclose(K.hsv_to_rgb_arr(hsv), arr, atol=1e-6), "hsv array round trip")
	hsl = K.rgb_to_hsl_arr(arr)
	ref = np.array([colorsys.rgb_to_hls(*(arr[i] / 255.0)) for i in range(len(arr))])
	check(np.allclose(hsl[:, 1], ref[:, 2]) and np.allclose(hsl[:, 2], ref[:, 1]), "hsl array = colorsys")
	check(np.allclose(K.hsl_to_rgb_arr(hsl), arr, atol=1e-6), "hsl array round trip")
	check(np.allclose(K.lab_to_rgb_arr(K.rgb_to_lab_arr(arr)), arr, atol=0.5), "lab array round trip")
	check(abs(K.rgb_to_lab((255, 255, 255))[0] - 100) < 0.01 and abs(K.rgb_to_lab((0, 0, 0))[0]) < 0.01, "lab ends")
	check(K.from_hex("#abc") == (170, 187, 204, 255) and K.to_hex((1, 2, 3, 4)) == "#01020304", "hex forms")
	check(abs(K.brightness((255, 255, 255)) - 255) < 1e-9 and K.brightness((0, 0, 0)) == 0, "brightness")
	check(K.distance((1, 2, 3), (1, 2, 3)) == 0 and K.distance((0, 0, 0), (255, 255, 255)) > 99, "distance")


def harmony_tests():
	for _ in range(300):
		c = tuple(int(v) for v in rng.integers(0, 256, 3))
		h, s, l = K.rgb_to_hsl(c)
		hm = K.harmonies(c)
		check(len(hm["complementary"]) == 1 and len(hm["triadic"]) == 2, "harmony counts")
		for name, vals in hm.items():
			for v in vals:
				check(abs(K.rgb_to_hsl(v)[2] - l) <= 1 / 255.0 + 1e-9, "%s keeps lightness %r -> %r" % (name, c, v))
		if s > 0.2 and 0.2 < l < 0.8:
			hc = K.rgb_to_hsl(hm["complementary"][0])[0]
			check(abs(((hc - h) % 360) - 180) < 3, "complementary hue %r" % (c,))


def ramp_tests():
	for space in ("rgb", "hsv", "lab"):
		r = K.ramp((10, 20, 30), (200, 100, 0), 5, space)
		check(len(r) == 5 and max(abs(a - b) for a, b in zip(r[0], (10, 20, 30))) <= 1 and
			  max(abs(a - b) for a, b in zip(r[-1], (200, 100, 0))) <= 1, "ramp ends %s" % space)
	check(K.ramp((0, 0, 0, 0), (255, 255, 255, 255), 3)[1] == (128, 128, 128, 128), "ramp alpha")


def main():
	suggest_tests()
	nudge_tests()
	conversion_tests()
	harmony_tests()
	ramp_tests()
	print("{0}  T5 colors: {1} checks, {2} failed".format("PASS" if not results[1] else "FAIL", *results))
	return 1 if results[1] else 0


if __name__ == "__main__":
	sys.exit(main())

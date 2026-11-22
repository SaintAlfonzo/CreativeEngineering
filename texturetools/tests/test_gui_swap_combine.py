# SPDX-License-Identifier: GPL-3.0-only
"""P6 tests: Palette Swap and Combine screens, bulk jobs, Copy as CLI, dev-mode provenance
(plan_texture_tools_gui.md sections 3, 4, 8 and 10).

Part A (no display): core/provenance.py on a temp copy of sources.csv (the real file is hashed before and after and
must not change), swap_jobs and the "swap" batch kind, palette_swap.py's colors: palette and P6 bulk flags (dry run
writes nothing), combine_tex.py's inline --stack.
Part B (xvfb-run child, as test_gui_smoke.py; the owner's DISPLAY is never used): the swap and combine screens build;
a params dict gives a preview equal to the engine output on synthetic arrays; the eyedropper and swatch strip; bulk
dry run writes nothing; preview-all grid and an unticked tile left out of Run; preset round trip; the Copy as CLI string
re-parses (through the CLI's own parser) to the same options; combine layer list, nudge and drag; the cartesian confirm;
stack JSON round trip; the provenance popup appends only on confirm, to the temp copy.
Every image is a synthetic array in a temp dir; no texture is drawn or changed.

Usage: python3 texturetools/tests/test_gui_swap_combine.py [-v]
"""
import contextlib
import hashlib
import io
import os
import shlex
import shutil
import subprocess
import sys
import tempfile
import time

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
if ROOT not in sys.path:
	sys.path.insert(0, ROOT)

VERBOSE = "-v" in sys.argv
CHILD_FLAG = "--gui-child"
FAILS = []
REAL_CSV = os.path.join(ROOT, "feature_writeup", "texture_sources", "sources.csv")


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)
	elif VERBOSE:
		print("ok   " + what)


def sha(path):
	with open(path, "rb") as f:
		return hashlib.sha1(f.read()).hexdigest()


def synthetic(tmp):
	"""Synthetic RGBA arrays saved as PNGs in tmp; returns a dict of paths and arrays."""
	import numpy as np
	from PIL import Image
	d = os.path.join(tmp, "img")
	os.makedirs(d, exist_ok=True)
	out = {"dir": d}

	def save(name, arr, sub=None):
		folder = os.path.join(d, sub) if sub else d
		os.makedirs(folder, exist_ok=True)
		p = os.path.join(folder, name)
		Image.fromarray(arr, "RGBA").save(p)
		return p

	src = np.zeros((8, 8, 4), dtype=np.uint8)
	for y in range(8):
		for x in range(8):
			v = 40 + 25 * ((x + y) // 2)
			src[y, x] = (v, v // 2, 20 + x * 5, 255)
	src[0, 0] = (0, 0, 0, 0)
	src[7, 7] = (10, 10, 10, 255)  # an "outline" color
	out["src"], out["src_arr"] = save("src.png", src), src
	rng = np.random.default_rng(66)
	pal = rng.integers(0, 256, size=(4, 4, 4), dtype=np.uint8)
	pal[:, :, 3] = 255
	pal[0, 0, 3] = 100
	out["pal"], out["pal_arr"] = save("pal.png", pal), pal
	out["pals"] = [save("p{0}.png".format(i), np.roll(pal, i, axis=1) // (i + 1) + 10, "pals") for i in range(3)]
	for p in out["pals"]:
		a = np.array(Image.open(p))
		a[:, :, 3] = 255
		Image.fromarray(a, "RGBA").save(p)
	out["srcs"] = [save("s{0}.png".format(i), np.roll(src, i, axis=0), "srcs") for i in range(3)]
	over = np.zeros((8, 8, 4), dtype=np.uint8)
	over[2:6, 2:6] = (200, 50, 50, 180)
	out["over"] = save("over.png", over)
	small = np.zeros((4, 4, 4), dtype=np.uint8)
	small[1:3, :] = (20, 200, 90, 255)
	out["small"] = save("small.png", small)
	mask = np.zeros((8, 8, 4), dtype=np.uint8)
	mask[:, :4] = (255, 255, 255, 255)
	out["mask"] = save("mask.png", mask)
	out["overs"] = [save("o{0}.png".format(i), np.roll(over, i, axis=1), "overs") for i in range(3)]
	out["lids"] = [save("l{0}.png".format(i), np.roll(small, i, axis=0), "lids") for i in range(3)]
	return out


def listing(path):
	res = []
	for dp, dns, fns in os.walk(path):
		res.extend(os.path.join(dp, f) for f in fns)
	return sorted(res)


# ---------------- part A ----------------

def part_a(tmp):
	import numpy as np
	from texturetools.core import batch, provenance
	from texturetools.core.swap import SwapOptions, palette_array, swap_image, swap_jobs, colors_spec

	real_before = sha(REAL_CSV)
	img = synthetic(tmp)

	# provenance core
	repo = os.path.join(tmp, "repo")
	os.makedirs(os.path.join(repo, "feature_writeup", "texture_sources"))
	csv_copy = os.path.join(repo, provenance.CSV_REL)
	shutil.copy(REAL_CSV, csv_copy)
	check(provenance.find_repo_root(os.path.join(repo, "a", "b.png")) == repo, "provenance: repo root found upward")
	check(provenance.find_repo_root(img["dir"]) is None, "provenance: no repo outside a checkout")
	outs = [os.path.join(repo, "tex", "item", "jam_{0}.png".format(n)) for n in ("a", "b")]
	check(provenance.output_pattern(outs, repo) == "tex/item/jam_*.png", "provenance: glob over several outputs")
	row = provenance.suggest_row(outs[:1], [os.path.join(repo, "src", "a.png"), "colors:#ff0000"], "tool cmd",
		"Jam items", root=repo)
	check(row["pattern"] == "tex/item/jam_a.png" and row["source"] == "src/a.png; colors:#ff0000"
		and row["origin"] == "owned-tool" and row["status"] == "confirm", "provenance: suggested row ({0})".format(row))
	before = open(csv_copy, encoding="utf-8").read()
	line = provenance.append_row(csv_copy, row)
	after = open(csv_copy, encoding="utf-8").read()
	check(after == before + ("" if before.endswith("\n") else "\n") + line, "provenance: one line appended")
	import csv as csvmod
	rows = [r for r in csvmod.reader(l for l in after.splitlines() if not l.startswith("#"))]
	check(rows[-1][0] == "tex/item/jam_a.png" and len(rows[-1]) == 7, "provenance: row parses as 7 CSV columns")
	bad = os.path.join(tmp, "bad.csv")
	with open(bad, "w") as f:
		f.write("a,b\n")
	try:
		provenance.append_row(bad, row)
		check(False, "provenance: wrong header refused")
	except ValueError:
		check(open(bad).read() == "a,b\n", "provenance: wrong header refused, file unchanged")

	# swap kind and jobs
	jobs = swap_jobs(img["src"], os.path.join(img["dir"], "pals"), False, SwapOptions(mode="even"),
		"{shape}_{palette}.png")
	check([j.output_name() for j in jobs] == ["src_p0.png", "src_p1.png", "src_p2.png"], "swap_jobs: names")
	arr = batch.get_worker("swap")(jobs[1])
	from PIL import Image
	exp = swap_image(img["src_arr"], np.array(Image.open(img["pals"][1]).convert("RGBA")), SwapOptions(mode="even"))
	check(np.array_equal(arr, exp), "swap kind equals swap_image")
	rj = swap_jobs(colors_spec(["#102030", "#a0b0c0"]), os.path.join(img["dir"], "srcs"), True, None,
		"{name}_{palette}.png", include="s[01].png")
	check([j.output_name() for j in rj] == ["s0_swatches.png", "s1_swatches.png"], "swap_jobs: reverse, glob")
	out_dir = os.path.join(tmp, "a_out")
	rep = batch.run_batch(jobs, out_dir, dry_run=True)
	check(not os.path.exists(out_dir) and len(rep.by_status("dry")) == 3, "swap bulk dry run writes nothing")

	# CLI: colors: palette and bulk flags
	from texturetools.cli import palette_swap as ps
	o1 = os.path.join(tmp, "cli_single.png")
	with contextlib.redirect_stdout(io.StringIO()):
		ps.main([img["src"], "colors:#ff0000,#00ff00,#0000ff", o1, "--mode", "ramp"])
	exp = swap_image(img["src_arr"], palette_array(["#ff0000", "#00ff00", "#0000ff"]), SwapOptions(mode="ramp"))
	check(np.array_equal(np.array(Image.open(o1).convert("RGBA")), exp), "cli: colors: palette equals the engine")
	o2 = os.path.join(tmp, "cli_bulk")
	buf = io.StringIO()
	with contextlib.redirect_stdout(buf):
		ps.main([img["src"], "--bulk", "--in-dir", os.path.join(img["dir"], "pals"), "--out-dir", o2, "--dry-run",
			"--pattern", "{palette}.png"])
	check(not os.path.exists(o2) and "3 would be written" in buf.getvalue(), "cli: bulk --dry-run writes nothing")
	with contextlib.redirect_stdout(io.StringIO()):
		ps.main([img["pal"], "--bulk", "--reverse", "--in-dir", os.path.join(img["dir"], "srcs"), "--out-dir", o2,
			"--pattern", "r_{name}.png"])
	check(sorted(os.listdir(o2)) == ["r_s0.png", "r_s1.png", "r_s2.png"], "cli: bulk --reverse writes per source")

	# combine inline --stack
	import json
	from texturetools.cli import combine_tex as ct
	from texturetools.core.combine import Layer, combine_stack, stack_to_dict
	layers = [Layer(img["src"]), Layer(img["over"], mode="multiply", offset=(1, 2))]
	o3 = os.path.join(tmp, "stack.png")
	with contextlib.redirect_stdout(io.StringIO()):
		ct.main(["--stack", json.dumps(stack_to_dict(layers)), o3])
	got = np.array(Image.open(o3).convert("RGBA"))
	check(np.array_equal(got, np.clip(combine_stack(layers), 0, 255).astype(np.uint8)), "cli: inline --stack")
	check(sha(REAL_CSV) == real_before, "part A: the real sources.csv is unchanged")


# ---------------- part B ----------------

def part_b(tmp):
	from test_gui_smoke import _child_env  # same env and DISPLAY guard as the P5 smoke test
	me = os.path.abspath(__file__)
	attempts = []
	if shutil.which("xvfb-run"):
		attempts.append(("xvfb-run", ["xvfb-run", "-a", "-s", "-screen 0 1280x900x24", sys.executable, me, CHILD_FLAG],
			"x11"))
	attempts.append(("sdl-offscreen", [sys.executable, me, CHILD_FLAG], "offscreen"))
	for label, cmd, driver in attempts:
		env = _child_env(tmp, driver)
		if driver == "offscreen":
			env.pop("DISPLAY", None)
		try:
			r = subprocess.run(cmd + (["-v"] if VERBOSE else []), env=env, capture_output=True, text=True, timeout=180)
		except subprocess.TimeoutExpired:
			print("{0}: timeout".format(label))
			continue
		out = (r.stdout or "") + (r.stderr or "")
		if "GUI_P6_RESULT" in out:
			res = [ln for ln in out.splitlines() if ln.startswith(("FAIL", "GUI_P6_RESULT", "ok "))]
			if VERBOSE:
				print("\n".join(res))
			n_ok = sum(1 for ln in res if ln.startswith("ok "))
			for ln in res:
				if ln.startswith("FAIL"):
					FAILS.append(ln)
			if any(ln.startswith("FAIL") for ln in res) and not VERBOSE:
				print(out[-3000:])
			print("part B backend: {0} -> {1}".format(label, res[-1] if res else "?"))
			if r.returncode != 0 and not any(ln.startswith("FAIL") for ln in res):
				FAILS.append("child exit {0}".format(r.returncode))
				print(out[-3000:])
			return True
		print("{0}: no display backend (exit {1}): {2}".format(label, r.returncode, out.strip().splitlines()[-1:]))
	print("SKIP part B: no display backend available")
	return False


def child():
	global VERBOSE
	VERBOSE = True  # the parent filters the lines
	parent = os.environ.get("TT_SMOKE_PARENT_DISPLAY", "")
	if os.environ.get("SDL_VIDEODRIVER") == "x11" and (not os.environ.get("DISPLAY")
			or os.environ.get("DISPLAY") == parent):
		print("refusing to open a window on the parent's DISPLAY")
		return 3
	tmp = os.environ["TT_SMOKE_TMP"]
	work = os.path.join(tmp, "child")
	os.makedirs(work, exist_ok=True)
	import numpy as np
	from PIL import Image
	img = synthetic(work)
	inputs_before = listing(img["dir"])
	real_before = sha(REAL_CSV)

	from texturetools.gui.app import TextureToolsApp
	from kivy.base import ExceptionHandler, ExceptionManager
	from kivy.clock import Clock
	from texturetools.core.swap import SwapOptions, palette_array, swap_image
	from texturetools.core.combine import Layer, combine_stack, layer_to_dict
	from texturetools.gui.widgets.preview import to_rgba_u8
	from texturetools.gui.screens import swap as swap_mod, combine as comb_mod

	class Record(ExceptionHandler):
		def handle_exception(self, inst):
			import traceback
			check(False, "exception in loop: {0!r}\n{1}".format(inst, traceback.format_exc()))
			a = TextureToolsApp.get_running_app()
			if a:
				a.stop()
			return ExceptionManager.PASS

	ExceptionManager.add_handler(Record())
	app = TextureToolsApp(start_screen="swap")

	def u8(a):
		return to_rgba_u8(a)

	def wait_for(cond, timeout=8):
		t0 = time.time()
		while not cond() and time.time() - t0 < timeout:
			yield 0.05

	def preview_after(scr, values):
		scr.preview.set_array(None)
		scr.update_params(values)
		yield from wait_for(lambda: scr.preview.array is not None)

	def run_and_wait(scr):
		scr.run()
		yield 0.1
		yield from wait_for(lambda: not scr.run_button.disabled)
		yield 0.1

	def parse_swap_cli(argv):
		"""Run the argv through palette_swap.py's own parser; capture args instead of running."""
		from texturetools.cli import palette_swap as ps
		got = {}
		saved = ps.run_single, ps.run_bulk_batch
		ps.run_single = lambda args: got.update(args=args, kind="single")
		ps.run_bulk_batch = lambda args, src, out: got.update(args=args, kind="bulk", src=src, out=out)
		try:
			ps.main(shlex.split(" ".join(shlex.quote(a) for a in argv))[3:])
		finally:
			ps.run_single, ps.run_bulk_batch = saved
		return got

	def parse_combine_cli(argv):
		from texturetools.cli import combine_tex as ct
		got = {}

		def capture(layers, return_layout=False, **opts):
			got.update(layers=layers, opts=opts)
			raise SystemExit(0)

		def capture_jobs(layers, vary, opts=None, pattern="", include=None, exclude=None, recursive=False):
			got.update(layers=layers, opts=dict(opts or {}), vary=vary, pattern=pattern)
			raise SystemExit(0)

		saved = ct.combine_stack, ct.stack_jobs
		ct.combine_stack, ct.stack_jobs = capture, capture_jobs
		try:
			with contextlib.redirect_stdout(io.StringIO()):
				ct.main(shlex.split(" ".join(shlex.quote(a) for a in argv))[3:])
		except SystemExit:
			pass
		finally:
			ct.combine_stack, ct.stack_jobs = saved
		return got

	def norm_layers(layers):
		out = []
		for l in layers:
			d = layer_to_dict(l)
			d.pop("name")
			d["offset"] = [int(v) for v in d["offset"]]
			out.append(d)
		return out

	def steps():
		yield 0.3
		check("swap" in app.screens and "combine" in app.screens, "swap and combine screens constructed")
		scr = app.switch_to("swap")
		check(type(scr).__name__ == "SwapScreen" and not scr.placeholder, "swap: real screen replaced the placeholder")
		yield 0.1
		pal_u8 = np.array(Image.open(img["pal"]).convert("RGBA"))

		# --- params -> preview == engine
		base = {"source": img["src"], "palette": img["pal"]}
		yield from preview_after(scr, base)
		exp = swap_image(img["src_arr"], pal_u8, SwapOptions())
		check(scr.preview.array is not None and np.array_equal(scr.preview.array, exp),
			"swap: default preview equals swap_image")
		variants = [
			{"mode": "even", "interpolate": "always", "interp_space": "lab"},
			{"mode": "role", "invert": True, "hue_shift": 30.0},
			{"mode": "nearest", "stretch": True, "preserve": 0.25, "alpha": "source"},
			{"mode": "ramp", "interpolate": "smaller", "interp_amount": 0.5, "dither": "ordered"},
			{"mode": "rank", "palette_alpha": 255, "drop_darkest": 1, "drop_lightest": 1, "sat_shift": -0.3,
				"val_shift": 0.1, "drop_brown": True, "drop_white": True},
			{"palette_exclude": ["#{0:02x}{1:02x}{2:02x}".format(*pal_u8[1, 1, :3])], "palette_exclude_tol": 4,
				"source_keep": ["#0a0a0a"], "source_keep_tol": 0},
		]
		for v in variants:
			p = dict(swap_mod.default_params(), **base)
			p.update(v)
			opts = swap_mod.options_from_params(p)
			res = scr.compute_preview(p)
			check(np.array_equal(res["preview"], swap_image(img["src_arr"], pal_u8, opts)),
				"swap: compute_preview equals the engine for {0}".format(sorted(v)))
		v = dict(variants[1])
		yield from preview_after(scr, v)
		exp = swap_image(img["src_arr"], pal_u8, swap_mod.options_from_params(scr.params))
		check(np.array_equal(scr.preview.array, exp), "swap: worker preview after a params change equals the engine")
		check(scr.preview._tex.mag_filter == "nearest", "swap: preview nearest filter")
		from texturetools.core.palette import extract_palette
		n_pal = len(extract_palette(pal_u8, alpha_min=int(scr.params["palette_alpha"]))[0])   # opaque only by default
		check(len(scr.exclude_list.colors) == n_pal and len(scr.keep_list.colors) > 3,
			"swap: swatch lists filled ({0} of {1}, {2})".format(len(scr.exclude_list.colors), n_pal,
			len(scr.keep_list.colors)))
		scr.update_params(dict((k, swap_mod.default_params()[k]) for k in v))

		# --- eyedropper and swatch list
		scr._pick_pal(scr.thumb_pal, 2, 1)
		want = "#{0:02x}{1:02x}{2:02x}".format(*pal_u8[1, 2, :3])
		check(scr.params["palette_exclude"] == [want], "swap: eyedropper on the palette adds an exclusion")
		check(scr.exclude_list.boxes[want].active, "swap: exclusion swatch ticked")
		scr.exclude_list.boxes[want].active = False
		check(scr.params["palette_exclude"] == [], "swap: unticking the swatch removes it")
		scr._pick_src(scr.thumb_src, 7, 7)
		check(scr.params["source_keep"] == ["#0a0a0a"], "swap: eyedropper on the source adds a keep color")
		scr.update_params({"source_keep": []})

		# --- swatch strip
		scr.strip.add_text("#ff0000, #00ff00,#0000ff")
		scr.update_params({"palette_from": "swatches"})
		yield from preview_after(scr, {"mode": "ramp"})
		exp = swap_image(img["src_arr"], palette_array(["#ff0000", "#00ff00", "#0000ff"]), SwapOptions(mode="ramp"))
		check(np.array_equal(scr.preview.array, exp), "swap: swatch strip palette equals the engine")
		check(scr.slots["palette"].disabled, "swap: palette file slot disabled while swatches are used")

		# --- Copy as CLI re-parses to the same options
		scr.update_params({"interpolate": "always", "interp_space": "hsv", "hue_shift": -12.0, "preserve": 0.4,
			"palette_exclude": ["#00ff00"], "palette_exclude_tol": 3, "drop_white": True,
			"output": os.path.join(work, "single.png")})
		got = parse_swap_cli(scr.cli_argv(scr.params))
		from texturetools.cli.palette_swap import options_from_args
		check(got.get("kind") == "single", "swap cli: single form")
		if got.get("args"):
			a = got["args"]
			check(options_from_args(a) == swap_mod.options_from_params(scr.params),
				"swap cli: re-parsed options equal the screen's")
			check((a.source, a.palette, a.output) == (img["src"], "colors:#ff0000,#00ff00,#0000ff",
				os.path.join(work, "single.png")), "swap cli: positionals")
		check(scr.copy_cli() is not None, "swap: Copy as CLI")

		# --- preset round trip
		snapshot = dict(scr.params)
		scr.save_preset("p6 swap")
		scr.update_params(swap_mod.default_params())
		check(scr.params != snapshot, "swap: params reset before the preset load")
		scr.apply_preset("p6 swap")
		check(scr.params == snapshot, "swap: preset round trip restores every param")
		check(list(scr.strip.colors) == snapshot["swatches"] and scr.param_controls["mode"].get_value() == "ramp",
			"swap: preset restores the swatch strip and controls")

		# --- single Run writes the output, equal to the engine
		yield from run_and_wait(scr)
		out1 = os.path.join(work, "single.png")
		check(os.path.isfile(out1) and np.array_equal(np.array(Image.open(out1).convert("RGBA")),
			swap_image(img["src_arr"], palette_array(snapshot["swatches"]), swap_mod.options_from_params(snapshot))),
			"swap: Run output equals the engine")

		# --- bulk: dry run, preview all, untick, run
		scr.update_params(dict(swap_mod.default_params(), source=img["src"], bulk=True,
			folder=os.path.join(img["dir"], "pals"), out_dir=os.path.join(work, "swap_bulk"), dry_run=True,
			pattern="{shape}_{palette}.png", mode="even"))
		yield 0.3
		check(scr.validate_run(scr.params) is None, "swap bulk: runnable")
		yield from run_and_wait(scr)
		check(not os.path.exists(os.path.join(work, "swap_bulk")), "swap bulk: dry run writes nothing")
		check("would write 3" in scr.status.text, "swap bulk: dry run reports 3 ({0})".format(scr.status.text))
		got = parse_swap_cli(scr.cli_argv(scr.params))
		check(got.get("kind") == "bulk" and got.get("src") == os.path.join(img["dir"], "pals")
			and got["args"].dry_run and got["args"].pattern == "{shape}_{palette}.png"
			and options_from_args(got["args"]) == swap_mod.options_from_params(scr.params),
			"swap cli: bulk form re-parses")
		scr.preview_all()
		yield from wait_for(lambda: scr.grid_items is not None)
		check(scr.grid_items is not None and len(scr.grid_items) == 3, "swap: preview all computes 3 tiles")
		check(scr._grid_shown, "swap: grid shown")
		if scr.grid_items:
			first = scr.grid_items[0]
			exp = swap_image(img["src_arr"], np.array(Image.open(img["pals"][0]).convert("RGBA")),
				SwapOptions(mode="even"))
			check(np.array_equal(first["array"], exp), "swap: grid tile equals the engine")
			scr._open_tile(first)
			check(not scr._grid_shown and np.array_equal(scr.preview.array, exp), "swap: click enlarges a tile")
			scr.grid.boxes[scr.grid_items[1]["key"]].active = False
			check(scr.run_params()["_skip"] == ["p1.png"], "swap: unticked tile goes to run params")
			check(not os.path.exists(os.path.join(work, "swap_bulk")), "swap: preview all writes nothing")
		scr.update_params({"dry_run": False})
		yield from run_and_wait(scr)
		written = sorted(os.listdir(os.path.join(work, "swap_bulk"))) if os.path.isdir(
			os.path.join(work, "swap_bulk")) else []
		check(written == ["src_p0.png", "src_p2.png"], "swap bulk: Run writes the ticked results only ({0})".format(
			written))
		scr.update_params({"bulk": False})

		# ================= combine
		cs = app.switch_to("combine")
		check(type(cs).__name__ == "CombineScreen" and not cs.placeholder, "combine: real screen")
		yield 0.1
		L = comb_mod.new_layer
		layers = [L("base", image=img["src"]),
			L("tint", image=img["over"], mode="multiply", opacity=0.6, offset=[1, -1]),
			L("dot", image=img["small"], mode="screen", resize="scale", scale=2, anchor="center", mask=img["mask"],
				mask_invert=True)]
		yield from preview_after(cs, {"layers": layers})
		exp = combine_stack([Layer(img["src"]), Layer(img["over"], mode="multiply", opacity=0.6, offset=(1, -1)),
			Layer(img["small"], mode="screen", scale=2, anchor="center", mask=img["mask"], mask_invert=True)])
		check(cs.preview.array is not None and np.array_equal(cs.preview.array, u8(exp)),
			"combine: 3-layer preview equals combine_stack")
		check(len(cs.layer_box.children) == 3, "combine: layer list shows 3 rows")
		for v in ({"canvas": "fixed", "canvas_w": 12, "canvas_h": 10, "background": "203040ff"},
				  {"canvas": "largest", "lcm": True}, {"background": "10,200,30"}):
			p = dict(cs.params)
			p.update(v)
			lay, opts = comb_mod.preview_stack(p)
			res = cs.compute_preview(p)
			check(np.array_equal(res["preview"], combine_stack(lay, **opts)),
				"combine: compute_preview equals the engine for {0}".format(sorted(v)))
		paint = [L("base", image=img["src"]), L("paint", image=img["over"], mode="paint", strength=0.5, inset=1,
			clip=True, mask=img["mask"])]
		res = cs.compute_preview(dict(cs.params, layers=paint))
		check(np.array_equal(res["preview"], combine_stack([Layer(img["src"]), Layer(img["over"], mode="paint",
			strength=0.5, inset=1, clip=True, mask=img["mask"])])), "combine: paint options equal the engine")

		# --- layer list ops, nudge, drag
		cs.select_layer(1)
		cs.nudge(1, 0)
		check(cs.params["layers"][1]["offset"] == [2, -1], "combine: nudge right")
		cs.move_layer(1, 1)
		check([d["name"] for d in cs.params["layers"]] == ["base", "dot", "tint"] and cs.sel == 2,
			"combine: move up keeps the selection on the moved layer")
		cs.move_layer(2, -1)
		cs.select_layer(1)
		cs.lc["opacity"].slider.value = 0.3
		check(abs(cs.params["layers"][1]["opacity"] - 0.3) < 1e-9, "combine: opacity control writes the layer")
		cs._on_preview_pixel(cs.preview, 2, 2, "left", "down")
		cs._on_preview_pixel(cs.preview, 5, 3, "left", "move")
		cs._on_preview_pixel(cs.preview, 5, 3, "left", "up")
		check(cs.params["layers"][1]["offset"] == [5, 0], "combine: drag moves the layer ({0})".format(
			cs.params["layers"][1]["offset"]))
		check(cs.lc["offset_x"].get_value() == 5, "combine: drag updates the offset control")
		cs.add_layer()
		check(len(cs.params["layers"]) == 4 and cs.sel == 2, "combine: add layer above the selection")
		cs.remove_layer(2)
		check(len(cs.params["layers"]) == 3, "combine: remove layer")
		cs.set_layer_field(2, "visible", False)
		lay, _ = comb_mod.preview_stack(cs.params)
		check(not lay[2].visible, "combine: eye hides the layer")
		cs.set_layer_field(2, "visible", True)

		# --- Copy as CLI: flag form (2 layers) and stack form (3 layers)
		two = [L("base", image=img["src"]), L("top", image=img["over"], mode="overlay", opacity=0.75, offset=[2, 1],
			offset_mode="wrap", anchor="center", strength=0.5)]
		p2 = dict(cs.params, layers=two, canvas="fixed", canvas_w=10, canvas_h=9, background="ff000080", fit="any",
			output=os.path.join(work, "c2.png"))
		argv = comb_mod.cli_argv(p2)
		check("--stack" not in argv, "combine cli: two plain layers use the flag form")
		got = parse_combine_cli(argv)
		lay, opts, _ = comb_mod.build_stack(p2)
		check(got.get("layers") is not None and norm_layers(got["layers"]) == norm_layers(lay)
			and got["opts"] == opts, "combine cli: flag form re-parses to the same stack ({0})".format(
				got.get("opts")))
		# add / multiply respect alpha by default (owner ruling R8); the layer toggle keeps the old blend
		for legacy in (False, True):
			twom = [L("base", image=img["src"]), L("top", image=img["over"], mode="multiply", legacy_blend=legacy)]
			pm = dict(cs.params, layers=twom, output=os.path.join(work, "cm.png"))
			argv = comb_mod.cli_argv(pm)
			got = parse_combine_cli(argv)
			lay, _, _ = comb_mod.build_stack(pm)
			check(("--legacy-blend" in argv) == legacy and got.get("layers") is not None
				and norm_layers(got["layers"]) == norm_layers(lay),
				"combine cli: legacy_blend={0} round trips through the flag form".format(legacy))
		check(L("x")["legacy_blend"] is False, "combine: a new layer respects alpha (legacy_blend off)")
		sd = swap_mod.default_params()
		check((sd["palette_alpha"], sd["alpha"], sd["interpolate"]) == (255, "source", "smaller"),
			"swap: GUI defaults follow the engine (255, source, smaller)")
		p3 = dict(cs.params, output=os.path.join(work, "c3.png"))
		argv = comb_mod.cli_argv(p3)
		check("--stack" in argv, "combine cli: three layers use the inline stack form")
		got = parse_combine_cli(argv)
		lay, opts, _ = comb_mod.build_stack(p3)
		check(got.get("layers") is not None and norm_layers(got["layers"]) == norm_layers(lay)
			and got["opts"] == opts, "combine cli: stack form re-parses to the same stack")

		# --- stack JSON round trip and preset round trip
		doc = comb_mod.stack_doc(cs.params)
		back = comb_mod.params_from_stack_doc(doc, None, cs.params)
		check(norm_layers(comb_mod.build_stack(back)[0]) == norm_layers(comb_mod.build_stack(cs.params)[0])
			and comb_mod.stack_options(back) == comb_mod.stack_options(cs.params), "combine: stack JSON round trip")
		snapshot = dict(cs.params)
		cs.save_preset("p6 combine")
		cs.update_params(comb_mod.default_params())
		check(len(cs.params["layers"]) == 2, "combine: reset before preset load")
		cs.apply_preset("p6 combine")
		check(cs.params == snapshot and len(cs.layer_box.children) == 3, "combine: preset round trip")

		# --- Run single
		cs.update_params({"output": os.path.join(work, "comb.png")})
		yield from run_and_wait(cs)
		lay, opts, _ = comb_mod.build_stack(cs.params)
		check(os.path.isfile(os.path.join(work, "comb.png")) and np.array_equal(
			np.array(Image.open(os.path.join(work, "comb.png")).convert("RGBA")), u8(combine_stack(lay, **opts))),
			"combine: Run output equals the engine")

		# --- bulk: one varying slot (dry run), then cartesian with confirm
		bl = [L("base", image=img["src"]), L("over", image=os.path.join(img["dir"], "overs"), varies=True),
			L("lid", image=img["small"], mode="alpha")]
		outd = os.path.join(work, "comb_bulk")
		yield from preview_after(cs, {"layers": bl, "out_dir": outd, "dry_run": True, "pattern": "{n}_{name}.png"})
		lay = [Layer(img["src"]), Layer(img["overs"][0]), Layer(img["small"])]
		check(np.array_equal(cs.preview.array, u8(combine_stack(lay))), "combine bulk: preview uses the first file")
		check("3 output" in cs.count_label.text, "combine bulk: count 3 ({0})".format(cs.count_label.text))
		yield from run_and_wait(cs)
		check(not os.path.exists(outd), "combine bulk: dry run writes nothing")
		check("would write 3" in cs.status.text, "combine bulk: dry run reports 3 ({0})".format(cs.status.text))
		got = parse_combine_cli(comb_mod.cli_argv(cs.params))
		check(got.get("vary") is not None and list(got["vary"]) == [1], "combine cli: bulk stack form re-parses")
		bl[2] = L("lid", image=os.path.join(img["dir"], "lids"), varies=True)
		cs.update_params({"layers": bl})
		yield 0.1
		check("cartesian: 9 outputs" in cs.count_label.text and not cs.confirm_button.disabled,
			"combine cartesian: count 9 and a Confirm button ({0})".format(cs.count_label.text))
		msg = cs.validate_run(cs.params)
		check(msg is not None and "Confirm 9" in msg, "combine cartesian: Run blocked until confirmed")
		cs.run()
		check(not cs.run_button.disabled and "Confirm" in cs.status.text, "combine cartesian: Run says why it stops")
		yield 0.3
		check(not os.path.exists(outd),
			"combine cartesian: Run without confirm stops ({0})".format(cs.status.text))
		cs.confirm()
		check(cs.validate_run(cs.params) is None and cs.confirm_button.disabled, "combine cartesian: confirmed")
		cs.update_params({"include": "*0.png"})
		yield 0.05
		check(cs.validate_run(cs.params) is not None, "combine cartesian: a changed count needs a new confirm")
		cs.update_params({"include": ""})
		cs.confirm()
		cs.preview_all()
		yield from wait_for(lambda: cs.grid_items is not None)
		check(cs.grid_items is not None and len(cs.grid_items) == 9, "combine: preview all 9 tiles")
		yield from run_and_wait(cs)
		check(not os.path.exists(outd) and "would write 9" in cs.status.text, "combine cartesian: dry run writes nothing")
		cs.update_params({"dry_run": False})
		yield from run_and_wait(cs)
		check(os.path.isdir(outd) and len(os.listdir(outd)) == 9, "combine cartesian: confirmed Run writes 9")

		# --- dev-mode provenance (temp copy only)
		rp = os.path.join(work, "repo")
		os.makedirs(os.path.join(rp, "feature_writeup", "texture_sources"))
		csv_copy = os.path.join(rp, "feature_writeup", "texture_sources", "sources.csv")
		shutil.copy(REAL_CSV, csv_copy)
		app.settings.set("provenance_csv", csv_copy)
		app.settings.set("dev_mode", False)
		cs.after_run(cs.params, [os.path.join(work, "comb.png")])
		check(app.provenance_popup is None, "provenance: nothing offered with dev mode off")
		app.settings.set("dev_mode", True)
		size0 = sha(csv_copy)
		cs.after_run(cs.params, [os.path.join(work, "comb.png")])
		pop = app.provenance_popup
		check(pop is not None, "provenance: offered in dev mode")
		if pop is not None:
			check(sha(csv_copy) == size0, "provenance: nothing written before confirm")
			pop.inputs["used_for"].text = "test use"
			pop.dismiss()
			yield 0.1
			check(sha(csv_copy) == size0, "provenance: Not now writes nothing")
			cs.after_run(cs.params, [os.path.join(work, "comb.png")])
			pop = app.provenance_popup
			pop.inputs["used_for"].text = "test use"
			check(pop.confirm(), "provenance: confirm appends")
			last = open(csv_copy, encoding="utf-8").read().splitlines()[-1]
			check("owned-tool" in last and "test use" in last and "comb.png" in last and "combine_tex" in last,
				"provenance: row has output, tool, use ({0})".format(last[:120]))
			check("texture_provenance.py --write" in pop.message.text and "texture_provenance.py" in cs.status.text,
				"provenance: reminder shown")
			pop.dismiss()
		check(sha(REAL_CSV) == real_before, "the real sources.csv is unchanged")
		check(listing(img["dir"]) == inputs_before, "no file written next to the inputs")

	gen = steps()

	def tick(dt):
		try:
			wait = next(gen)
		except StopIteration:
			app.stop()
			return
		except Exception as e:  # a failed step
			import traceback
			check(False, "step raised {0!r}\n{1}".format(e, traceback.format_exc()))
			app.stop()
			return
		Clock.schedule_once(tick, wait)

	Clock.schedule_once(tick, 0.2)
	Clock.schedule_once(lambda dt: (check(False, "timeout"), app.stop()), 150)
	app.run()
	print("GUI_P6_RESULT {0}".format("FAIL" if FAILS else "PASS"))
	return 1 if FAILS else 0


def main():
	if CHILD_FLAG in sys.argv:
		return child()
	tmp = tempfile.mkdtemp(prefix="tt_gui_p6_")
	try:
		part_a(tmp)
		part_b(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("texturetools gui swap/combine: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	if HERE not in sys.path:
		sys.path.insert(0, HERE)
	sys.exit(main())

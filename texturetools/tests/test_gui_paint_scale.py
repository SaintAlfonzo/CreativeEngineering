# SPDX-License-Identifier: GPL-3.0-only
"""P8 tests: Texture Painter and Scale screens, Open in Editor from every screen, Copy as CLI
(plan_texture_tools_gui.md sections 5, 7, 8 and 10).

Part A (no display): cli/paint_tex.py (defaults equal paint() defaults, a bare run equals combine_paint, every flag
round-trips through cli_args, bulk dry run writes nothing), the "paint" and "upscale" batch kinds, core/scale.py's
scale_pipeline against scale_tex.py, upscale_plan against upscale_sprites.py, and the editor's scale and trim being
core/scale.py's array functions (one implementation).
Part B (xvfb-run child, as test_gui_swap_combine.py; the owner's DISPLAY is never used): the painter preview equals
the engine for every PaintOptions control; the eyedropper fills the keep and paint-only lists; bulk dry run writes
nothing, Preview all and an unticked tile; Run equals the engine and the Copy as CLI command (re-parsed and run)
writes the same bytes; presets; the Scale screen in single mode (factor, size, fit canvas, trim, anchor) and folder mode
against scale_tex.py and upscale_sprites.py run from its own Copy as CLI string; editor -> Scale -> editor handoff;
Open in Editor from swap, combine, paint and scale.
Every image is a synthetic array in a temp dir; no texture is drawn or changed.

Usage: python3 texturetools/tests/test_gui_paint_scale.py [-v]
"""
import contextlib
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
RESULT = "GUI_P8_RESULT"


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)
	elif VERBOSE:
		print("ok   " + what)


def listing(path):
	res = []
	for dp, dns, fns in os.walk(path):
		res.extend(os.path.join(dp, f) for f in fns)
	return sorted(res)


def read(path):
	with open(path, "rb") as f:
		return f.read()


def synthetic(tmp):
	"""Synthetic RGBA arrays saved as PNGs in tmp; returns a dict of paths and arrays."""
	import numpy as np
	from PIL import Image
	d = os.path.join(tmp, "img")
	os.makedirs(d, exist_ok=True)
	out = {"dir": d}

	def save(name, arr, sub=None, mode="RGBA"):
		folder = os.path.join(d, sub) if sub else d
		os.makedirs(folder, exist_ok=True)
		p = os.path.join(folder, name)
		Image.fromarray(arr, mode).save(p)
		return p

	shape = np.zeros((10, 10, 4), dtype=np.uint8)
	for y in range(10):
		for x in range(10):
			if 1 <= x <= 8 and 2 <= y <= 9:
				v = 60 + 12 * x + 6 * y
				shape[y, x] = (v, v - 20, v // 2, 255)
	shape[2, 1:9] = (20, 20, 20, 255)     # a dark rim row
	shape[5, 4] = shape[5, 5] = (250, 250, 250, 255)  # "eyes"
	out["shape"], out["shape_arr"] = save("shape.png", shape), shape
	rng = np.random.default_rng(88)
	mat = rng.integers(0, 256, size=(5, 4, 4), dtype=np.uint8)
	mat[:, :, 3] = 255
	mat[0, 0, 3] = 0
	out["mat"], out["mat_arr"] = save("mat.png", mat), mat
	big = rng.integers(0, 256, size=(12, 11, 4), dtype=np.uint8)
	out["big"] = save("big.png", big)
	mask = np.zeros((10, 10, 4), dtype=np.uint8)
	mask[:6, :, 3] = 255
	out["mask"] = save("mask.png", mask)
	out["mats"] = [save("m{0}.png".format(i), np.roll(mat, i, axis=0) // (i + 1) + 5, "mats") for i in range(3)]
	out["shapes"] = [save("s{0}.png".format(i), np.roll(shape, i, axis=1), "shapes") for i in range(2)]
	icon = np.zeros((6, 6, 4), dtype=np.uint8)
	icon[1:5, 2:5] = rng.integers(0, 256, size=(4, 3, 4), dtype=np.uint8)
	icon[1:5, 2:5, 3] = 255
	out["icon"], out["icon_arr"] = save("icon.png", icon), icon
	out["rgb"] = save("rgb.png", rng.integers(0, 256, size=(3, 4, 3), dtype=np.uint8), mode="RGB")
	out["sprites"] = os.path.join(d, "sprites")
	save("a.png", rng.integers(0, 256, size=(4, 4, 4), dtype=np.uint8), "sprites")
	save("b.png", rng.integers(0, 256, size=(3, 5, 4), dtype=np.uint8), "sprites/sub")
	# swap and combine inputs (Open in Editor from those screens)
	pal = rng.integers(0, 256, size=(4, 4, 4), dtype=np.uint8)
	pal[:, :, 3] = 255
	out["pal"] = save("pal.png", pal)
	return out


def quiet(fn, *a, **kw):
	buf = io.StringIO()
	with contextlib.redirect_stdout(buf), contextlib.redirect_stderr(buf):
		try:
			fn(*a, **kw)
			return 0, buf.getvalue()
		except SystemExit as e:
			return e.code or 0, buf.getvalue()


# ---------------------------------------------------------------- part A (no display)

def part_a(tmp):
	import numpy as np
	from PIL import Image
	from texturetools.cli import paint_tex as pt, scale_tex as st, upscale_sprites as us
	from texturetools.core import batch, combine, edit, scale
	from texturetools.core.image_io import load_rgba_array
	from texturetools.core.paint import PaintOptions, paint, paint_jobs
	img = synthetic(os.path.join(tmp, "a"))
	shape = load_rgba_array(img["shape"])
	mat = load_rgba_array(img["mat"])

	def u8(a):
		return np.clip(a, 0, 255).astype(np.uint8)

	# paint_tex defaults
	args = pt.build_parser().parse_args(["s.png", "m.png", "o.png"])
	check(pt.options_from_args(args) == PaintOptions(), "paint_tex: no flags = PaintOptions() defaults")
	check(pt.cli_args(PaintOptions()) == [], "paint_tex: default options emit no flags")
	o1 = os.path.join(tmp, "a_paint.png")
	code, _ = quiet(pt.main, [img["shape"], img["big"], o1])
	check(code == 0 and np.array_equal(np.array(Image.open(o1)), u8(combine.combine_paint(shape,
		load_rgba_array(img["big"])))), "paint_tex: bare run equals combine_paint")
	code, _ = quiet(pt.main, [img["shape"], img["mat"], o1])
	check(code == 0 and np.array_equal(np.array(Image.open(o1)), u8(paint(shape, mat))),
		"paint_tex: bare run with a smaller material equals paint()")
	# every flag round-trips
	full = PaintOptions(mode="projected", offset_x=-2, offset_y=3, scale=2, tile=True, flip_h=True, flip_v=True,
		rotate=270, inset=1, strength=0.35, outline="keep", mask_invert=True, only_colors=["#3c2814", "#aabbcc"],
		only_tol=4, keep_colors=["#141414"], keep_tol=2, material_alpha="skip", fit="cover", clip=True).validate()
	argv = [img["shape"], img["mat"], o1] + pt.cli_args(full)
	check(pt.options_from_args(pt.build_parser().parse_args(argv)) == full, "paint_tex: every flag round-trips")
	o2 = os.path.join(tmp, "a_paint2.png")
	code, _ = quiet(pt.main, [img["shape"], img["mat"], o2, "--mask", img["mask"]] + pt.cli_args(full))
	exp = paint(shape, mat, full, load_rgba_array(img["mask"]))
	check(code == 0 and np.array_equal(np.array(Image.open(o2)), u8(exp)), "paint_tex: flags + mask equal paint()")
	code, _ = quiet(pt.main, [img["shape"], img["mat"]])
	check(code == 2, "paint_tex: wrong positional count is a usage error")
	# bulk: dry run writes nothing, then writes; reverse
	outd = os.path.join(tmp, "a_bulk")
	code, text = quiet(pt.main, [img["shape"], "--bulk", "--in-dir", os.path.dirname(img["mats"][0]), "--out-dir",
		outd, "--dry-run", "--pattern", "{shape}_{material}.png"])
	check(code == 0 and not os.path.exists(outd) and "3 would be written" in text, "paint_tex: bulk dry run writes nothing")
	code, text = quiet(pt.main, [img["shape"], "--bulk", "--in-dir", os.path.dirname(img["mats"][0]), "--out-dir",
		outd, "--pattern", "{shape}_{material}.png", "--strength", "0.5"])
	exp = u8(paint(shape, load_rgba_array(img["mats"][1]), PaintOptions(strength=0.5)))
	check(code == 0 and sorted(os.listdir(outd)) == ["shape_m0.png", "shape_m1.png", "shape_m2.png"]
		and np.array_equal(np.array(Image.open(os.path.join(outd, "shape_m1.png"))), exp), "paint_tex: bulk writes")
	jobs = paint_jobs(img["mat"], os.path.dirname(img["shapes"][0]), reverse=True)
	check([j.inputs["shape"] for j in jobs] == img["shapes"] and all(j.inputs["material"] == img["mat"] for j in jobs)
		and jobs[0].kind == "paint", "paint_jobs: reverse = one material, many shapes")
	got = batch.get_worker("paint")(jobs[0])
	check(np.array_equal(got, paint(load_rgba_array(img["shapes"][0]), mat)), "batch kind paint = paint()")
	bad = paint_jobs(img["shape"], [img["icon"]], reverse=True, mask=img["mask"])  # 6x6 shape, 10x10 mask
	rep = batch.run_batch(bad, os.path.join(tmp, "a_bad"), dry_run=True)
	check(len(rep.skipped) == 1 and "mask" in rep.skipped[0].reason, "batch kind paint: mask size mismatch skips")

	# scale_pipeline == scale_tex.py
	cases = ([img["icon"], "--factor", "3"], [img["icon"], "--trim", "--size", "12x16"],
		[img["icon"], "--trim", "--canvas", "20", "--anchor", "center"], [img["rgb"], "--size", "8x6"],
		[img["icon"], "--factor", "2", "--canvas", "15x13"])
	for i, argv in enumerate(cases):
		out_cli = os.path.join(tmp, "a_sc{0}.png".format(i))
		code, _ = quiet(st.main, [argv[0], out_cli] + argv[1:])
		ns = {"factor": None, "size": None, "trim": False, "canvas": None, "anchor": "topleft"}
		rest = argv[1:]
		k = 0
		while k < len(rest):
			flag = rest[k]
			if flag == "--trim":
				ns["trim"] = True
				k += 1
				continue
			val = rest[k + 1]
			ns[flag[2:]] = int(val) if flag == "--factor" else val
			k += 2
		src = Image.open(argv[0])
		src.load()
		out, _ = scale.scale_pipeline(src, ns["factor"], scale.parse_size(ns["size"]) if ns["size"] else None,
			ns["trim"], scale.parse_size(ns["canvas"]) if ns["canvas"] else None, ns["anchor"])
		check(code == 0 and np.array_equal(np.array(Image.open(out_cli).convert("RGBA")), scale.pil_to_rgba(out)),
			"scale_pipeline equals scale_tex.py {0}".format(" ".join(argv[1:])))
	try:
		scale.scale_pipeline(Image.open(img["icon"]), None, (13, 12))
		check(False, "scale_pipeline: uneven size refused")
	except ValueError as e:
		check("whole-number" in str(e), "scale_pipeline: uneven size refused with scale_tex.py's reason")

	# upscale_plan == upscale_sprites.py
	ud = os.path.join(tmp, "a_up")
	for flat in (False, True):
		out_d = ud + ("_flat" if flat else "")
		code, _ = quiet(us.main, [img["sprites"], out_d, "--factors", "3", "2", "--recursive"] + (["--flat"] if flat else []))
		plan = scale.upscale_plan(img["sprites"], out_d, scale.upscale_factors([3, 2]), True, flat)
		check(code == 0 and sorted(str(p[3]) for p in plan) == listing(out_d),
			"upscale_plan lists what upscale_sprites.py writes (flat={0})".format(flat))
	jobs = scale.upscale_jobs(img["sprites"], ud, [2], recursive=True)
	got = batch.get_worker("upscale")(jobs[0])
	check(len(jobs) == 2 and np.array_equal(got, np.array(Image.open(os.path.join(ud, "x2", "a.png")).convert("RGBA")))
		and jobs[1].output_name() == os.path.join("x2", "sub", "b.png"), "batch kind upscale = upscale_sprites output")
	check(str(scale.upscale_output_dir(img["sprites"])) == img["sprites"] + "_upscaled", "upscale default output dir")

	# one implementation: editor scale and trim are core/scale.py's
	a = np.array(img["icon_arr"])
	check(np.array_equal(edit.scale_int(a, 3), scale.scale_array(a, 3)) and np.array_equal(edit.scale_int(a, 3),
		scale.pil_to_rgba(scale.scale_image(Image.fromarray(a, "RGBA"), 3))), "editor scale_int = scale_array = scale_image")
	check(np.array_equal(edit.downscale_int(scale.scale_array(a, 2), 2), a), "editor downscale_int = downscale_array")
	check(np.array_equal(edit.trim(a), scale.pil_to_rgba(scale.trim(Image.fromarray(a, "RGBA")))),
		"editor trim = scale.trim_array = scale_tex trim")
	check(edit.trim_bbox(np.zeros((3, 3, 4), dtype=np.uint8)) is None, "editor trim_bbox of an empty image is None")


# ---------------------------------------------------------------- part B (xvfb child)

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
		if RESULT in out:
			res = [ln for ln in out.splitlines() if ln.startswith(("FAIL", RESULT, "ok "))]
			if VERBOSE:
				print("\n".join(res))
			for ln in res:
				if ln.startswith("FAIL"):
					FAILS.append(ln)
			if any(ln.startswith("FAIL") for ln in res) and not VERBOSE:
				print(out[-3000:])
			print("part B backend: {0} -> {1} ({2} checks)".format(label, res[-1] if res else "?",
				sum(1 for ln in res if ln.startswith(("ok ", "FAIL")))))
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

	from texturetools.gui.app import TextureToolsApp
	from kivy.base import ExceptionHandler, ExceptionManager
	from kivy.clock import Clock
	from texturetools.cli import paint_tex as pt, scale_tex as st, upscale_sprites as us
	from texturetools.core import scale
	from texturetools.core.image_io import load_rgba_array
	from texturetools.core.paint import PaintOptions, paint
	from texturetools.gui.widgets.preview import to_rgba_u8
	from texturetools.gui.screens import paint as paint_mod, scale as scale_mod, combine as comb_mod

	class Record(ExceptionHandler):
		def handle_exception(self, inst):
			import traceback
			check(False, "exception in loop: {0!r}\n{1}".format(inst, traceback.format_exc()))
			a = TextureToolsApp.get_running_app()
			if a:
				a.stop()
			return ExceptionManager.PASS

	ExceptionManager.add_handler(Record())
	app = TextureToolsApp(start_screen="paint")
	shape = load_rgba_array(img["shape"])
	mat = load_rgba_array(img["mat"])
	mask = load_rgba_array(img["mask"])

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
		done = []
		orig = scr._run_done

		def hooked(outputs):
			orig(outputs)
			done.append(outputs)

		scr._run_done = hooked
		try:
			scr.run()
			yield from wait_for(lambda: bool(done))
		finally:
			scr._run_done = orig
		yield 0.05

	def cli_tail(argv):
		"""argv after "python3 -m module", through a shell quote round trip (what the owner pastes)."""
		return shlex.split(" ".join(shlex.quote(a) for a in argv))[3:]

	def editor_has(arr):
		ed = app.screens["editor"]
		return app.manager.current == "editor" and ed.image.shape == u8(arr).shape and np.array_equal(ed.image, u8(arr))

	def steps():
		yield 0.3
		ps = app.switch_to("paint")
		check(type(ps).__name__ == "PaintScreen" and not ps.placeholder and ps.tool_name == "paint",
			"paint: real screen replaced the placeholder")
		check(type(app.screens["scale"]).__name__ == "ScaleScreen" and not app.screens["scale"].placeholder,
			"scale: real screen replaced the placeholder")

		# --- painter preview == engine for every control
		base = {"shape": img["shape"], "material": img["mat"]}
		yield from preview_after(ps, base)
		check(np.array_equal(ps.preview.array, u8(paint(shape, mat))), "paint: default preview equals paint()")
		check(ps.thumb_shape.array is not None and ps.thumb_mat.array is not None, "paint: shape and material thumbnails")
		cases = [
			{"mode": "projected", "fit": "contain"}, {"mode": "projected", "fit": "cover", "offset_x": 1},
			{"offset_x": 2, "offset_y": -1}, {"scale": 2, "tile": True}, {"flip_h": True, "flip_v": True},
			{"rotate": "90"}, {"inset": 1}, {"strength": 0.25}, {"outline": "keep"}, {"material_alpha": "skip"},
			{"clip": True}, {"only_colors": ["#141414"], "only_tol": 3}, {"keep_colors": ["#fafafa"], "keep_tol": 0},
		]
		defaults = paint_mod.default_params()
		for case in cases:
			p = dict(defaults, **base)
			p.update(case)
			yield from preview_after(ps, p)
			exp = paint(shape, mat, paint_mod.options_from_params(p))
			check(np.array_equal(ps.preview.array, u8(exp)), "paint: preview equals paint() for {0}".format(case))
		p = dict(defaults, mask=img["mask"], mask_invert=True, **base)
		yield from preview_after(ps, p)
		check(np.array_equal(ps.preview.array, u8(paint(shape, mat, PaintOptions(mask_invert=True), mask))),
			"paint: loaded mask with invert")
		check(paint_mod.options_from_params(dict(defaults, rotate="180")).rotate == 180, "paint: rotate choice is an int")
		ps.update_params({"mode": "projected"})
		proj = (ps.param_controls["fit"].disabled, ps.param_controls["scale"].disabled)
		ps.update_params({"mode": "inplace"})
		check(proj == (False, True) and ps.param_controls["fit"].disabled and not ps.param_controls["scale"].disabled,
			"paint: fit only in projected mode, scale only in place")

		# --- eyedropper on the shape thumbnail
		yield from preview_after(ps, dict(defaults, **base))
		c = ps.pick_shape(4, 5)
		check(c == (250, 250, 250) and ps.params["keep_colors"] == ["#fafafa"], "paint: eyedropper adds to keep")
		yield 0.3
		check(np.array_equal(ps.preview.array, u8(paint(shape, mat, PaintOptions(keep_colors=["#fafafa"])))),
			"paint: kept color stays the shape's")
		ps.update_params({"pick_target": "only"})
		ps.pick_shape(3, 2)
		check(ps.params["only_colors"] == ["#141414"], "paint: eyedropper adds to paint-only")
		check(ps.pick_shape(0, 0) is None, "paint: eyedropper ignores transparent pixels")
		ps.pick_shape(4, 5)
		check(ps.params["keep_colors"] == ["#fafafa"] and ps.params["only_colors"] == ["#141414", "#fafafa"],
			"paint: second pick toggles the only list, keep list unchanged")
		ps.update_params({"keep_colors": [], "only_colors": [], "pick_target": "keep"})

		# --- Run single, Copy as CLI equals
		out1 = os.path.join(work, "paint_gui.png")
		run_p = dict(defaults, output=out1, mask=img["mask"], strength=0.4, outline="keep", rotate="90", tile=True,
			only_colors=["#3c2814"], only_tol=40, **base)
		yield from preview_after(ps, run_p)
		yield from run_and_wait(ps)
		opts = paint_mod.options_from_params(ps.params)
		check(os.path.isfile(out1) and np.array_equal(np.array(Image.open(out1)), u8(paint(shape, mat, opts, mask))),
			"paint: Run output equals the engine")
		argv = ps.cli_argv(ps.params)
		check(argv[:3] == ["python3", "-m", "texturetools.cli.paint_tex"], "paint: Copy as CLI names paint_tex")
		args = pt.build_parser().parse_args(cli_tail(argv))
		check(pt.options_from_args(args) == opts and args.mask == img["mask"] and args.inputs[2] == out1,
			"paint cli: re-parses to the same options and mask")
		out2 = os.path.join(work, "paint_cli.png")
		tail = cli_tail(argv)
		tail[2] = out2
		code, _ = quiet(pt.main, tail)
		check(code == 0 and read(out1) == read(out2), "paint cli: the command writes the same bytes as Run")
		check(ps.copy_cli() is not None, "paint: copy_cli gives a command")

		# --- bulk: dry run, preview all, untick, reverse
		outd = os.path.join(work, "paint_bulk")
		mats_dir = os.path.dirname(img["mats"][0])
		bp = dict(defaults, shape=img["shape"], material="", bulk=True, folder=mats_dir, out_dir=outd, dry_run=True,
			pattern="{shape}_{material}.png")
		yield from preview_after(ps, bp)
		check(np.array_equal(ps.preview.array, u8(paint(shape, load_rgba_array(img["mats"][0])))),
			"paint bulk: preview uses the first material")
		yield from run_and_wait(ps)
		check(not os.path.exists(outd) and "would write 3" in ps.status.text,
			"paint bulk: dry run writes nothing ({0})".format(ps.status.text))
		args = pt.build_parser().parse_args(cli_tail(ps.cli_argv(ps.params)))
		check(args.bulk and args.in_dir == mats_dir and args.out_dir == outd and args.dry_run and not args.reverse
			and args.pattern == "{shape}_{material}.png" and args.inputs == [img["shape"]], "paint cli: bulk form re-parses")
		ps.preview_all()
		yield from wait_for(lambda: ps.grid_items is not None)
		check(ps.grid_items is not None and len(ps.grid_items) == 3, "paint: preview all 3 tiles")
		ps.grid.set_unticked(["m1.png"])
		ps.update_params({"dry_run": False})
		yield from run_and_wait(ps)
		check(sorted(os.listdir(outd)) == ["shape_m0.png", "shape_m2.png"], "paint bulk: unticked tile left out of Run")
		check(np.array_equal(np.array(Image.open(os.path.join(outd, "shape_m2.png"))),
			u8(paint(shape, load_rgba_array(img["mats"][2])))), "paint bulk: output equals the engine")
		ps.grid.set_unticked([])
		outr = os.path.join(work, "paint_rev")
		yield from preview_after(ps, {"reverse_roles": True, "material": img["mat"], "folder": os.path.dirname(
			img["shapes"][0]), "out_dir": outr, "dry_run": False})
		yield from run_and_wait(ps)
		check(sorted(os.listdir(outr)) == ["s0_mat.png", "s1_mat.png"] and np.array_equal(
			np.array(Image.open(os.path.join(outr, "s1_mat.png"))), u8(paint(load_rgba_array(img["shapes"][1]), mat))),
			"paint bulk: reverse roles = one material, many shapes")
		check(ps.folder_slot.label == "Shapes folder", "paint: reverse renames the folder slot")

		# --- presets
		ps.update_params({"keep_colors": ["#141414"], "strength": 0.7})
		snap = dict(ps.params)
		ps.save_preset("p8 paint")
		ps.update_params(paint_mod.default_params())
		ps.apply_preset("p8 paint")
		check(ps.params == snap and ps.keep_list.checked() == ["#141414"], "paint: preset round trip")

		# --- Open in Editor from paint
		yield from preview_after(ps, dict(defaults, **base))
		res = ps.result_array
		ps.open_in_editor()
		yield 0.1
		check(editor_has(res), "open in editor: from paint")

		# --- Scale screen, single
		sc = app.switch_to("scale")
		sdef = scale_mod.default_params()
		icon_img = Image.open(img["icon"])
		icon_img.load()
		singles = [({"by": "factor", "factor": 3}, (3, None, False, None, "topleft")),
			({"by": "size", "size": "12x16", "trim": True}, (None, (12, 16), True, None, "topleft")),
			({"by": "fit canvas", "canvas": "20", "anchor": "center", "trim": True}, (None, None, True, (20, 20), "center")),
			({"by": "factor", "factor": 2, "canvas": "15x13"}, (2, None, False, (15, 13), "topleft"))]
		for i, (case, pipe) in enumerate(singles):
			p = dict(sdef, source=img["icon"], **case)
			yield from preview_after(sc, p)
			out, _ = scale.scale_pipeline(icon_img.copy(), *pipe)
			check(np.array_equal(sc.preview.array, scale.pil_to_rgba(out)), "scale: preview equals scale_pipeline {0}".format(
				case))
			gui_out = os.path.join(work, "sc_gui{0}.png".format(i))
			sc.update_params({"output": gui_out})
			yield from run_and_wait(sc)
			tail = cli_tail(sc.cli_argv(sc.params))
			check(sc.cli_argv(sc.params)[2] == "texturetools.cli.scale_tex" and tail[1] == gui_out,
				"scale cli: scale_tex form {0}".format(" ".join(tail[2:])))
			cli_out = os.path.join(work, "sc_cli{0}.png".format(i))
			tail[1] = cli_out
			code, _ = quiet(st.main, tail)
			check(code == 0 and os.path.isfile(gui_out) and read(gui_out) == read(cli_out),
				"scale cli: scale_tex.py writes the same bytes as Run ({0})".format(" ".join(tail[2:])))
		yield from preview_after(sc, dict(sdef, source=img["rgb"], by="size", size="8x6"))
		check(np.array_equal(sc.preview.array, scale.pil_to_rgba(scale.scale_image(Image.open(img["rgb"]), 2))),
			"scale: RGB source previews")
		sc.update_params(dict(sdef, source=img["icon"], by="size", size="13x12"))
		yield 0.4
		check("cannot scale" in sc.status.text and "whole-number" in sc.status.text,
			"scale: uneven size explained ({0})".format(sc.status.text))
		check(sc.validate(dict(sdef, source=img["icon"], by="fit canvas")) is not None, "scale: fit canvas needs a canvas")
		# default output next to the input, as the CLI: copy the input into a scratch folder first
		dflt_dir = os.path.join(work, "dflt")
		os.makedirs(dflt_dir)
		dsrc = os.path.join(dflt_dir, "icon.png")
		shutil.copy(img["icon"], dsrc)
		yield from preview_after(sc, dict(sdef, source=dsrc, factor=2, output=""))
		yield from run_and_wait(sc)
		check(os.path.isfile(os.path.join(dflt_dir, "icon_12.png")), "scale: blank output writes <stem>_<size>.png")

		# --- Scale screen, folder
		up_gui = os.path.join(work, "up_gui")
		fp = dict(sdef, mode="folder", folder=img["sprites"], out_dir=up_gui, factors="3 2", recursive=True)
		yield from preview_after(sc, fp)
		with Image.open(os.path.join(img["sprites"], "a.png")) as a_img:
			exp = scale.pil_to_rgba(scale.upscale_pil(a_img, 2))
		check(np.array_equal(sc.preview.array, exp) and "2 image(s)" in sc.status.text,
			"scale folder: preview is the first image at the first factor ({0})".format(sc.status.text))
		sc.preview_all()
		yield from wait_for(lambda: sc.grid_items is not None)
		check(sc.grid_items is not None and len(sc.grid_items) == 4, "scale folder: preview all 4 tiles")
		check(not os.path.exists(up_gui), "scale folder: preview all writes nothing")
		yield from run_and_wait(sc)
		argv = sc.cli_argv(sc.params)
		check(argv[2] == "texturetools.cli.upscale_sprites" and "--recursive" in argv and argv[-3:] == ["3", "2",
			"--recursive"], "scale cli: upscale_sprites form ({0})".format(" ".join(argv[3:])))
		up_cli = os.path.join(work, "up_cli")
		tail = cli_tail(argv)
		tail[1] = up_cli
		code, _ = quiet(us.main, tail)
		rel_g = [os.path.relpath(p, up_gui) for p in listing(up_gui)]
		rel_c = [os.path.relpath(p, up_cli) for p in listing(up_cli)]
		check(code == 0 and rel_g == rel_c and len(rel_g) == 4 and all(read(os.path.join(up_gui, r)) == read(
			os.path.join(up_cli, r)) for r in rel_g), "scale folder: Run writes what upscale_sprites.py writes")
		up_flat = os.path.join(work, "up_flat")
		sc.update_params({"flat": True, "out_dir": up_flat, "recursive": False})
		sc.grid_items = None
		sc.preview_all()
		yield from wait_for(lambda: sc.grid_items is not None)
		sc.grid.set_unticked(["a.png x3"])
		yield from run_and_wait(sc)
		check([os.path.basename(p) for p in listing(up_flat)] == ["a_x2.png"], "scale folder: flat, unticked left out")
		check("--flat" in sc.cli_argv(sc.params) and "--recursive" not in sc.cli_argv(sc.params), "scale cli: --flat")
		sc.grid.set_unticked([])

		# --- editor -> Scale -> editor
		ed = app.switch_to("editor")
		ed.load_array(img["icon_arr"], "icon.png")
		yield 0.1
		check(np.array_equal(ed.op_scale(2), scale.scale_array(u8(img["icon_arr"]), 2)), "editor: Scale Up = scale_array")
		check(np.array_equal(ed.op_scale(-2), u8(img["icon_arr"])), "editor: Scale Down = downscale_array")
		check(np.array_equal(ed.op_trim(), scale.trim_array(u8(img["icon_arr"]))), "editor: Trim = trim_array")
		got = ed.open_in_scale()
		check(got is sc and app.manager.current == "scale" and sc.params["mode"] == "single" and not sc.params["source"],
			"editor -> scale: handoff switches screens")
		sc.update_params({"by": "fit canvas", "canvas": "16", "anchor": "center", "trim": False})
		yield 0.5
		yield from wait_for(lambda: sc.preview.array is not None and sc.preview.array.shape[0] == 16)
		exp, _ = scale.scale_pipeline(Image.fromarray(scale.trim_array(u8(img["icon_arr"])), "RGBA"), None, None, False,
			(16, 16), "center")
		check(np.array_equal(sc.preview.array, scale.pil_to_rgba(exp)), "editor -> scale: preview scales the editor image")
		check(sc.validate_run(sc.params) is not None, "editor -> scale: Run asks for an output file")
		sc.open_in_editor()
		yield 0.1
		check(editor_has(scale.pil_to_rgba(exp)), "open in editor: from scale (back to the editor)")

		# --- Open in Editor from swap and combine
		sw = app.switch_to("swap")
		yield from preview_after(sw, {"source": img["shape"], "palette": img["pal"]})
		res = sw.result_array
		sw.open_in_editor()
		yield 0.1
		check(res is not None and editor_has(res), "open in editor: from swap")
		cs = app.switch_to("combine")
		L = comb_mod.new_layer
		yield from preview_after(cs, {"layers": [L("base", image=img["shape"]), L("over", image=img["mat"],
			mode="multiply")]})
		res = cs.result_array
		cs.open_in_editor()
		yield 0.1
		check(res is not None and editor_has(res), "open in editor: from combine")
		check(all(s.copy_cli() is not None for n, s in app.screens.items() if n in ("swap", "combine", "paint", "scale")),
			"every tool screen has Copy as CLI")

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
	print("{0} {1}".format(RESULT, "FAIL" if FAILS else "PASS"))
	return 1 if FAILS else 0


def main():
	if CHILD_FLAG in sys.argv:
		return child()
	tmp = tempfile.mkdtemp(prefix="tt_gui_p8_")
	try:
		n0 = len(FAILS)
		part_a(tmp)
		print("part A: {0} failed".format(len(FAILS) - n0))
		part_b(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("texturetools gui paint/scale: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	if HERE not in sys.path:
		sys.path.insert(0, HERE)
	sys.exit(main())

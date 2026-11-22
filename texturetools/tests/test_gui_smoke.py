# SPDX-License-Identifier: GPL-3.0-only
"""T6 GUI smoke test and shell unit tests (plan_texture_tools_gui.md section 10, P5).

Part A (no display, always runs): Settings store round trip, presets save/load/list/delete and the CLI string builder,
Worker latest-wins and in-order behavior, screen registry import.

Part B (needs a display backend): re-runs this file as a child under `xvfb-run -a` (a private X server; the owner's
DISPLAY is never used: the child refuses to start when DISPLAY equals the parent's). The child builds the app, then for
every registered screen: switches to it, loads a synthetic RGBA PNG through the params dict (debounced worker preview),
checks the preview shows it, zooms to 64x and back, maps a pixel. Also: a desktop drop routed to the slot under the
cursor, a preset save and apply, Open in Editor. Any exception fails. KIVY_HOME and the settings dir are temp dirs; the
only files written are the synthetic input, settings and the one preset, all in the temp dir.
When xvfb-run is missing, SDL's offscreen video driver is tried; when neither works, part B prints SKIP and exits 0.

Usage: python3 texturetools/tests/test_gui_smoke.py [-v]
"""
import os
import shutil
import subprocess
import sys
import tempfile
import threading
import time

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
if ROOT not in sys.path:
	sys.path.insert(0, ROOT)

VERBOSE = "-v" in sys.argv
CHILD_FLAG = "--gui-child"
FAILS = []


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)
	elif VERBOSE:
		print("ok   " + what)


# ---------------- part A ----------------

def part_a(tmp):
	from texturetools.gui.settings import Settings
	from texturetools.core import presets
	from texturetools.gui.worker import Worker

	d = os.path.join(tmp, "cfg")
	s = Settings(d)
	check(s.dev_mode is False, "settings: dev mode off by default")
	check(not os.path.exists(s.path), "settings: nothing written before a set")
	folder = os.path.join(tmp, "imgs")
	os.makedirs(folder)
	s.add_recent(os.path.join(folder, "a.png"))
	s.set("project_folder", folder)
	s2 = Settings(d)
	check(s2.recent_folders() == [folder], "settings: recent folder round trip")
	check(s2.project_folder == folder and s2.start_folder() == folder, "settings: project folder and start folder")

	base = os.path.join(tmp, "presets")
	params = {"source": "/x/a.png", "tol": 3, "pal": (1, 2), "flag": True}
	presets.save_preset(base, "swap", "my/../preset", params)
	names = presets.list_presets(base, "swap")
	check(names == ["my_.._preset"], "presets: name sanitized ({0})".format(names))
	got = presets.load_preset(base, "swap", names[0])
	check(got == {"source": "/x/a.png", "tol": 3, "pal": [1, 2], "flag": True}, "presets: round trip")
	try:
		presets.load_preset(base, "combine", names[0])
		check(False, "presets: other screen's preset refused")
	except (OSError, ValueError):
		check(True, "presets: other screen's preset refused")
	check(presets.delete_preset(base, "swap", names[0]) and not presets.list_presets(base, "swap"), "presets: delete")
	check(presets.cli_string("nosuch", {}) is None, "cli: no builder -> None")
	presets.register_cli_builder("t_demo", lambda p: presets.argv_from_spec(
		"texturetools.cli.palette_swap", p, positional=("src", "pal"), flags={"even": "--even", "out": "-o",
			"drops": "--drop", "off": "--off"}))
	cli = presets.cli_string("t_demo", {"src": "a b.png", "pal": "p.png", "even": True, "off": False, "out": "o.png",
		"drops": ["#000", "#fff"]})
	check(cli == "python3 -m texturetools.cli.palette_swap 'a b.png' p.png --even -o o.png --drop '#000' '#fff'",
		"cli: argv_from_spec quoting ({0})".format(cli))

	# Worker: latest wins, in-order lane, errors, progress
	got = []
	gate = threading.Event()

	def slow(p):
		gate.wait(2)
		return p["n"]

	w = Worker(latest_only=True, dispatch=lambda f: f())
	w.submit(slow, {"n": 1}, on_done=got.append)
	time.sleep(0.05)
	for n in (2, 3, 4):
		w.submit(slow, {"n": n}, on_done=got.append)
	gate.set()
	w.wait_idle(3)
	check(got == [4], "worker: latest only keeps the newest result ({0})".format(got))
	w.close()
	got2, errs, prog = [], [], []
	w = Worker(latest_only=False, dispatch=lambda f: f())

	def with_progress(p, progress):
		progress(0.5, "half")
		return p["n"]

	for n in (1, 2, 3):
		w.submit(lambda p: p["n"], {"n": n}, on_done=got2.append)
	w.submit(lambda p: 1 / 0, {}, on_error=lambda e, tb: errs.append(type(e).__name__))
	w.submit(with_progress, {"n": 9}, on_done=got2.append, on_progress=lambda f, m: prog.append((f, m)))
	w.wait_idle(3)
	check(got2 == [1, 2, 3, 9], "worker: in-order lane runs every job ({0})".format(got2))
	check(errs == ["ZeroDivisionError"], "worker: error handed to on_error")
	check(prog == [(0.5, "half")], "worker: progress forwarded")
	src = {"a": [1]}
	seen = []
	w.submit(lambda p: p, src, on_done=seen.append)
	src["a"].append(2)
	w.wait_idle(3)
	check(seen == [{"a": [1]}], "worker: params deep-copied ({0})".format(seen))
	w.close()

	from texturetools.gui import registry
	check(callable(registry.register_screen), "registry imports without Kivy")


# ---------------- part B ----------------

def _child_env(tmp, driver):
	env = dict(os.environ)
	env.pop("WAYLAND_DISPLAY", None)
	env["TT_SMOKE_PARENT_DISPLAY"] = os.environ.get("DISPLAY", "")
	env["KIVY_HOME"] = os.path.join(tmp, "kivyhome")
	env["TEXTURETOOLS_CONFIG_DIR"] = os.path.join(tmp, "ttcfg")
	env["TT_SMOKE_TMP"] = tmp
	env["KIVY_NO_ARGS"] = "1"
	env["KIVY_NO_CONSOLELOG"] = "0" if VERBOSE else "1"
	env["KIVY_WINDOW"] = "sdl2"
	env["SDL_VIDEODRIVER"] = driver
	env["SDL_AUDIODRIVER"] = "dummy"
	return env


def part_b(tmp):
	me = os.path.abspath(__file__)
	attempts = []
	if shutil.which("xvfb-run"):
		attempts.append(("xvfb-run", ["xvfb-run", "-a", "-s", "-screen 0 1280x800x24", sys.executable, me, CHILD_FLAG],
			"x11"))
	attempts.append(("sdl-offscreen", [sys.executable, me, CHILD_FLAG], "offscreen"))
	for label, cmd, driver in attempts:
		env = _child_env(tmp, driver)
		if driver == "offscreen":
			env.pop("DISPLAY", None)
		try:
			r = subprocess.run(cmd + (["-v"] if VERBOSE else []), env=env, capture_output=True, text=True, timeout=120)
		except subprocess.TimeoutExpired:
			print("{0}: timeout".format(label))
			continue
		out = (r.stdout or "") + (r.stderr or "")
		if "GUI_SMOKE_RESULT" in out:
			res = [ln for ln in out.splitlines() if ln.startswith(("FAIL", "GUI_SMOKE_RESULT"))]
			if VERBOSE:
				print(out[-4000:])
			for ln in res:
				if ln.startswith("FAIL"):
					FAILS.append(ln)
			print("part B backend: {0} -> {1}".format(label, res[-1] if res else "?"))
			if r.returncode != 0 and not any(ln.startswith("FAIL") for ln in res):
				FAILS.append("child exit {0}".format(r.returncode))
			return True
		print("{0}: no display backend (exit {1}): {2}".format(label, r.returncode, out.strip().splitlines()[-1:]))
	print("SKIP part B: no display backend available")
	return False


def child():
	parent = os.environ.get("TT_SMOKE_PARENT_DISPLAY", "")
	if os.environ.get("SDL_VIDEODRIVER") == "x11" and (not os.environ.get("DISPLAY")
			or os.environ.get("DISPLAY") == parent):
		print("refusing to open a window on the parent's DISPLAY")
		return 3
	tmp = os.environ["TT_SMOKE_TMP"]
	import numpy as np
	from PIL import Image
	img_dir = os.path.join(tmp, "images")
	os.makedirs(img_dir, exist_ok=True)
	rng = np.random.default_rng(6)
	arr = rng.integers(0, 256, size=(16, 24, 4), dtype=np.uint8)
	arr[0, 0] = (255, 0, 0, 255)
	arr[:, :, 3][arr[:, :, 3] < 60] = 0
	src = os.path.join(img_dir, "synthetic.png")
	Image.fromarray(arr, "RGBA").save(src)
	src2 = os.path.join(img_dir, "synthetic2.png")
	Image.fromarray(arr[:8, :8].copy(), "RGBA").save(src2)
	before = sorted(os.listdir(img_dir))

	from texturetools.gui.app import TextureToolsApp
	from kivy.base import ExceptionHandler, ExceptionManager
	from kivy.clock import Clock
	from kivy.core.window import Window
	from texturetools.gui.screens.base import ToolScreen

	class Record(ExceptionHandler):
		def handle_exception(self, inst):
			import traceback
			check(False, "exception in loop: {0!r}\n{1}".format(inst, traceback.format_exc()))
			App_ = TextureToolsApp.get_running_app()
			if App_:
				App_.stop()
			return ExceptionManager.PASS

	ExceptionManager.add_handler(Record())
	app = TextureToolsApp(start_screen="swap")

	def steps():
		yield 0.3
		names = list(app.screens)
		check(set(["swap", "combine", "paint", "editor", "scale", "settings"]) <= set(names),
			"every screen constructed ({0})".format(names))
		for name in names:
			scr = app.switch_to(name)
			yield 0.1
			check(app.manager.current == name, "switch to " + name)
			if not isinstance(scr, ToolScreen):
				continue
			if "source" in scr.slots and scr.placeholder:  # real screens: test_gui_swap_combine.py
				scr.slots["source"].set_paths([src])
				t0 = time.time()
				while scr.preview.array is None and time.time() - t0 < 5:
					yield 0.05
				check(scr.preview.array is not None and scr.preview.array.shape == (16, 24, 4),
					name + ": preview from params via worker")
				if scr.preview.array is not None:
					check(np.array_equal(scr.preview.array, arr), name + ": preview pixels equal the source")
			scr.preview.set_array(arr.astype(np.float32), fit=True)
			yield 0.05
			pv = scr.preview
			check(pv._tex.mag_filter == "nearest" and pv._tex.min_filter == "nearest", name + ": nearest filter")
			pv.set_zoom(64, anchor=pv.to_window(*pv.center))
			check(pv.zoom == 64, name + ": zoom 64")
			pv.set_zoom(500)
			check(pv.zoom == 64, name + ": zoom clamped at 64")
			pv.fit()
			check(1 <= pv.zoom <= 64, name + ": fit zoom {0}".format(pv.zoom))
			ox, oy = pv._origin()
			p = pv.pixel_at(*pv.to_window(ox + 0.5 * pv.zoom, oy + 16 * pv.zoom - 0.5 * pv.zoom))
			check(p == (0, 0), name + ": top-left pixel maps to (0, 0) ({0})".format(p))
		# drop routing onto the swap source slot
		scr = app.switch_to("swap")
		yield 0.2
		slot = scr.slots["source"]
		cx, cy = slot.to_window(*slot.center)
		Window.dispatch("on_drop_begin", cx, Window.height - cy)
		Window.dispatch("on_drop_file", src2.encode(), cx, Window.height - cy)
		Window.dispatch("on_drop_end", cx, Window.height - cy)
		yield 0.4
		check(slot.path == src2, "drop routed to the slot under the cursor ({0})".format(slot.path))
		check(scr.params.get("source") == src2, "drop updated the params dict")
		check(app.settings.recent_folders() == [img_dir], "drop added the folder to recent folders")
		# preset save + apply
		scr.save_preset("smoke")
		scr.slots["source"].set_paths([src])
		yield 0.05
		scr.apply_preset("smoke")
		yield 0.05
		check(scr.params["source"] == src2 and slot.path == src2, "preset restores params and slots")
		check(scr.copy_cli() is not None, "swap: copy as CLI gives a command")
		ph = [n for n, x in app.screens.items() if getattr(x, "placeholder", False)]
		if ph:
			check(app.screens[ph[0]].copy_cli() is None, "copy as CLI without a builder says so")
		# open in editor
		scr.result_array = arr
		scr.open_in_editor()
		yield 0.1
		check(app.manager.current == "editor" and app.screens["editor"].preview.array is not None,
			"open in editor hands the array over")
		# About popup on the settings screen shows the version
		from texturetools import __version__
		pop = app.switch_to("settings").show_about()
		yield 0.1
		txt = pop.content.children[-1].text
		check(__version__ in txt and "GPL-3.0-only" in txt, "about popup shows version and license")
		pop.dismiss()
		check(sorted(os.listdir(img_dir)) == before, "no file written next to the inputs")

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
	Clock.schedule_once(lambda dt: (check(False, "timeout"), app.stop()), 60)
	app.run()
	print("GUI_SMOKE_RESULT {0}".format("FAIL" if FAILS else "PASS"))
	return 1 if FAILS else 0


def main():
	if CHILD_FLAG in sys.argv:
		return child()
	tmp = tempfile.mkdtemp(prefix="tt_gui_smoke_")
	try:
		part_a(tmp)
		part_b(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("texturetools gui smoke: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	sys.exit(main())

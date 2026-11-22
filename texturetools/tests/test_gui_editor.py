# SPDX-License-Identifier: GPL-3.0-only
"""P7 tests: the Texture Editor screen (plan_texture_tools_gui.md sections 6 and 10).

Part A (no display): UndoStack.replace_current.
Part B (xvfb-run child, as test_gui_smoke.py; the owner's DISPLAY is never used): tool strokes sent as synthetic
PixelPreview on_pixel events equal the core/edit.py result (pencil with and without mirror, eraser, line, rect,
ellipse, bucket, global fill, picker and alt-click, rect select, wand, shift-add, drawing limited to a selection,
copy, cut, floating paste moved by a drag and committed by a click away, Esc, keyboard shortcuts); image operations;
undo and redo through Ctrl+Z / Ctrl+Y and the history strip; nudge buttons, the step knob, suggest rows, the delta
knob and harmonies equal core/colors.py; hex, sliders and the SV box; recent colors; saved slots persist in
settings.json and reload in a new panel, shift-click saves, palette export and import round trip, add image colors,
load palette from image; the image palette strip (select, bucket-held replace); autosave into the config dir and
restore in a new editor; Save writes only the chosen path; the Open in Editor handoff; no file is written next to
the inputs and the opened image is unchanged.
Every image is a synthetic array in a temp dir; no texture is drawn or changed.

Usage: python3 texturetools/tests/test_gui_editor.py [-v]
"""
import hashlib
import json
import os
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


def check(cond, what):
	if not cond:
		FAILS.append(what)
		print("FAIL " + what)
	elif VERBOSE:
		print("ok   " + what)


def sha(path):
	with open(path, "rb") as f:
		return hashlib.sha1(f.read()).hexdigest()


def listing(path):
	res = []
	for dp, dns, fns in os.walk(path):
		res.extend(os.path.join(dp, f) for f in fns)
	return sorted(res)


# ---------------- part A ----------------

def part_a():
	import numpy as np
	from texturetools.core.undo import UndoStack
	a = np.zeros((2, 2, 4), np.uint8)
	b = a + 1
	s = UndoStack((a, "x"), "open")
	s.push((b, "x"), "edit")
	s.replace_current((b, "y"))
	check(len(s) == 2 and s.current[1] == "y" and np.array_equal(s.current[0], b), "replace_current keeps the step")
	check(s.undo()[1] == "x" and s.redo()[1] == "y", "replace_current survives undo and redo")


# ---------------- part B ----------------

def part_b(tmp):
	from test_gui_smoke import _child_env  # same env and DISPLAY guard as the P5 smoke test
	me = os.path.abspath(__file__)
	attempts = []
	if shutil.which("xvfb-run"):
		attempts.append(("xvfb-run", ["xvfb-run", "-a", "-s", "-screen 0 1400x900x24", sys.executable, me, CHILD_FLAG],
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
		if "GUI_P7_RESULT" in out:
			res = [ln for ln in out.splitlines() if ln.startswith(("FAIL", "GUI_P7_RESULT", "ok "))]
			if VERBOSE:
				print("\n".join(res))
			for ln in res:
				if ln.startswith("FAIL"):
					FAILS.append(ln)
			if any(ln.startswith("FAIL") for ln in res) and not VERBOSE:
				print(out[-3000:])
			n_ok = sum(1 for ln in res if ln.startswith("ok "))
			print("part B backend: {0} -> {1} ({2} checks ok)".format(label, res[-1] if res else "?", n_ok))
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
	cfg = os.environ["TEXTURETOOLS_CONFIG_DIR"]
	import numpy as np
	from PIL import Image
	img_dir = os.path.join(tmp, "images")
	out_dir = os.path.join(tmp, "out")
	os.makedirs(img_dir, exist_ok=True)
	os.makedirs(out_dir, exist_ok=True)
	rng = np.random.default_rng(77)
	src_arr = np.zeros((12, 12, 4), dtype=np.uint8)
	src_arr[:, :] = (30, 60, 90, 255)
	src_arr[2:6, 2:6] = (200, 180, 40, 255)
	src_arr[8:11, 1:4] = (200, 182, 40, 255)  # near the block color, not connected
	src_arr[0, 11] = (0, 0, 0, 0)
	src_arr[6:12, 7:12] = rng.integers(0, 256, size=(6, 5, 4), dtype=np.uint8)
	src_arr[6:12, 7:12, 3] = 255
	src = os.path.join(img_dir, "src.png")
	Image.fromarray(src_arr, "RGBA").save(src)
	inputs_before = listing(img_dir)
	src_sha = sha(src)

	from texturetools.gui.app import TextureToolsApp
	from kivy.base import ExceptionHandler, ExceptionManager
	from kivy.clock import Clock
	from kivy.core.window import Window
	from texturetools.core import colors as C, edit as E
	from texturetools.gui.settings import Settings
	from texturetools.gui.widgets.colorpanel import ColorPanel

	class Record(ExceptionHandler):
		def handle_exception(self, inst):
			import traceback
			check(False, "exception in loop: {0!r}\n{1}".format(inst, traceback.format_exc()))
			a = TextureToolsApp.get_running_app()
			if a:
				a.stop()
			return ExceptionManager.PASS

	ExceptionManager.add_handler(Record())
	app = TextureToolsApp(start_screen="editor")

	def px(ed, c, r, kind):
		ed.preview.dispatch("on_pixel", c, r, "left", kind)

	def stroke(ed, pts):
		px(ed, pts[0][0], pts[0][1], "down")
		for p in pts[1:]:
			px(ed, p[0], p[1], "move")
		px(ed, pts[-1][0], pts[-1][1], "up")

	def mods(*m):
		Window._modifiers = list(m)

	def key(cp, code, *m):
		Window.dispatch("on_key_down", code, 0, cp, list(m))

	def eq(a, b):
		return a is not None and b is not None and a.shape == b.shape and np.array_equal(a, b)

	def steps():
		yield 0.4
		ed = app.screens["editor"]
		check(app.manager.current == "editor" and not ed.placeholder, "editor screen is the real one")
		check(not ed.restored and ed.image.shape == (16, 16, 4) and not ed.is_dirty(), "fresh start: blank 16x16, clean")
		check(ed.preview.report_outside and ed.preview.checker, "canvas reports off-image drags, checkerboard on")
		check(ed.open_path(src), "open")
		yield 0.1
		check(eq(ed.image, src_arr) and ed.path == os.path.abspath(src) and not ed.is_dirty(), "opened image equals the file")
		check(eq(ed.preview.array, src_arr), "canvas shows the image")
		check(ed.preview.zoom >= 4, "fit zoom {0}".format(ed.preview.zoom))
		check(len(ed._overlay.children) > 0, "overlay drawn (grid)")
		states = [ed.image.copy()]
		red = (220, 20, 30, 255)

		# pencil
		ed.panel.set_color(red)
		ed.set_tool("pencil")
		exp = E.pencil(ed.image, 1, 1, red)
		exp = E.pencil(exp, 4, 3, red, prev=(1, 1))
		exp = E.pencil(exp, 6, 6, red, prev=(4, 3))
		stroke(ed, [(1, 1), (4, 3), (6, 6)])
		check(eq(ed.image, exp), "pencil stroke equals core pencil with joined drags")
		check(ed.is_dirty() and ed.panel.recent[0] == red, "pencil: dirty, color in recent")
		states.append(ed.image.copy())
		# pencil off the canvas edge
		exp = E.pencil(ed.image, 10, 0, red)
		exp = E.pencil(exp, 14, -2, red, prev=(10, 0))
		stroke(ed, [(10, 0), (14, -2)])
		check(eq(ed.image, exp), "pencil dragged off the canvas clips like core")
		states.append(ed.image.copy())
		# mirror
		ed.set_param("mirror", "both")
		exp = E.pencil(ed.image, 2, 9, red, mirror="both")
		exp = E.pencil(exp, 3, 10, red, prev=(2, 9), mirror="both")
		stroke(ed, [(2, 9), (3, 10)])
		check(eq(ed.image, exp), "mirror both pencil equals core")
		ed.set_param("mirror", "x")
		blue = (10, 40, 230, 255)
		ed.panel.set_color(blue)
		ed.set_tool("line")
		exp = E.line(ed.image, 0, 0, 5, 2, blue, mirror="x")
		before = ed.image.copy()
		px(ed, 0, 0, "down")
		px(ed, 5, 2, "move")
		check(eq(ed.image, before) and eq(ed.preview.array, exp), "line drag: preview only, image unchanged")
		px(ed, 5, 2, "up")
		check(eq(ed.image, exp), "mirror x line equals core")
		ed.set_param("mirror", "off")
		states.append(ed.image.copy())
		# rect and ellipse
		ed.set_tool("rect")
		ed.set_param("filled", True)
		exp = E.rect(ed.image, 7, 1, 10, 4, blue, filled=True)
		stroke(ed, [(7, 1), (9, 3), (10, 4)])
		check(eq(ed.image, exp), "filled rect equals core")
		ed.set_tool("ellipse")
		ed.set_param("filled", False)
		exp = E.ellipse(ed.image, 1, 6, 9, 11, blue)
		stroke(ed, [(1, 6), (9, 11)])
		check(eq(ed.image, exp), "outline ellipse equals core")
		states.append(ed.image.copy())
		# eraser
		ed.set_tool("eraser")
		exp = E.pencil(ed.image, 3, 3, E.TRANSPARENT)
		exp = E.pencil(exp, 5, 3, E.TRANSPARENT, prev=(3, 3))
		stroke(ed, [(3, 3), (5, 3)])
		check(eq(ed.image, exp), "eraser equals core pencil with transparent")
		# bucket and global with tolerance
		green = (30, 200, 60, 255)
		ed.panel.set_color(green)
		ed.set_tool("bucket")
		ed.set_param("tolerance", 3)
		exp = E.bucket_fill(ed.image, 2, 2, green, 3)
		px(ed, 2, 2, "down")
		px(ed, 2, 2, "up")
		check(eq(ed.image, exp), "bucket (tolerance 3) equals core")
		ed.set_tool("global")
		exp = E.global_fill(ed.image, 2, 9, green, 3)
		px(ed, 2, 9, "down")
		px(ed, 2, 9, "up")
		check(eq(ed.image, exp), "global fill (tolerance 3) equals core")
		ed.set_param("tolerance", 0)
		states.append(ed.image.copy())
		# picker and alt-click
		ed.set_tool("picker")
		px(ed, 8, 8, "down")
		check(ed.color == E.eyedropper(ed.image, 8, 8), "picker tool sets the color")
		ed.set_tool("pencil")
		before = ed.image.copy()
		mods("alt")
		px(ed, 4, 4, "down")
		px(ed, 4, 4, "up")
		mods()
		check(ed.color == E.eyedropper(before, 4, 4) and eq(ed.image, before), "alt-click picks without drawing")

		# selection
		ed.set_tool("select")
		stroke(ed, [(1, 1), (3, 2), (4, 4)])
		check(eq(ed.selection, E.select_rect(ed.image.shape, 1, 1, 4, 4)), "rect select equals core")
		ed.set_tool("wand")
		mods("shift")
		px(ed, 9, 1, "down")
		px(ed, 9, 1, "up")
		mods()
		w = E.magic_wand(ed.image, 9, 1, 0, True)
		check(eq(ed.selection, E.select_rect(ed.image.shape, 1, 1, 4, 4) | w), "shift-click wand adds (equals core)")
		ed.set_param("sel_op", "subtract")
		px(ed, 9, 1, "down")
		px(ed, 9, 1, "up")
		ed.set_param("sel_op", "replace")
		rect_sel = E.select_rect(ed.image.shape, 1, 1, 4, 4)
		check(eq(ed.selection, E.combine_selection(rect_sel | w, w, "subtract")), "wand subtract equals core")
		sel = ed.selection.copy()
		ed.set_tool("pencil")
		ed.panel.set_color(red)
		exp = E.pencil(ed.image, 0, 2, red, selection=sel)
		exp = E.pencil(exp, 6, 2, red, prev=(0, 2), selection=sel)
		stroke(ed, [(0, 2), (6, 2)])
		check(eq(ed.image, exp), "pencil limited to the selection equals core")
		states.append(ed.image.copy())
		# copy, paste, move, commit by a click away
		ed.set_tool("select")
		stroke(ed, [(1, 1), (3, 3)])
		key("c", 99, "ctrl")
		check(ed.clipboard is not None and ed.clipboard.size == (3, 3), "Ctrl+C copies the selection")
		piece = E.copy_selection(ed.image, E.select_rect(ed.image.shape, 1, 1, 3, 3))
		key("v", 118, "ctrl")
		check(ed.floating is not None and (ed.floating.x, ed.floating.y) == (1, 1), "Ctrl+V pastes floating in place")
		before = ed.image.copy()
		stroke(ed, [(2, 2), (6, 7), (8, 9)])
		check((ed.floating.x, ed.floating.y) == (7, 8) and eq(ed.image, before), "drag inside moves the floating piece")
		check(eq(ed.preview.array, E.commit_floating(before, E.Floating(piece.pixels, piece.mask, 7, 8))),
			"canvas shows the floating piece")
		key("", 275)
		check((ed.floating.x, ed.floating.y) == (8, 8), "arrow nudges the floating piece")
		px(ed, 0, 15, "down")
		px(ed, 0, 15, "up")
		check(ed.floating is None and eq(ed.image, E.commit_floating(before, E.Floating(piece.pixels, piece.mask, 8, 8))),
			"click away commits the paste (equals core)")
		states.append(ed.image.copy())
		key("v", 118, "ctrl")
		key("", 27)
		check(ed.floating is None and eq(ed.image, states[-1]), "Esc drops a floating paste")
		ed.set_tool("select")
		stroke(ed, [(10, 10), (12, 12)])
		img_before = ed.image.copy()
		key("x", 120, "ctrl")
		m = E.select_rect(img_before.shape, 10, 10, 12, 12)
		check(eq(ed.image, E.cut_selection(img_before, m)[0]), "Ctrl+X cuts (equals core)")
		key("", 127)
		check(eq(ed.image, E.fill_mask(E.cut_selection(img_before, m)[0], m, E.TRANSPARENT)), "Delete clears the selection")
		key("d", 100, "ctrl")
		check(ed.selection is None, "Ctrl+D deselects")
		key("l", 108)
		check(ed.params["tool"] == "line" and ed.tool_buttons["line"].state == "down", "tool key L picks the line tool")
		states.append(ed.image.copy())

		# image operations
		cur = ed.image.copy()
		for fn, core in ((lambda: ed.op_flip("h"), lambda a: E.flip(a, "h")),
				(lambda: ed.op_flip("v"), lambda a: E.flip(a, "v")),
				(lambda: ed.op_rotate(90), lambda a: E.rotate(a, 90)),
				(lambda: ed.op_rotate(270), lambda a: E.rotate(a, 270)),
				(lambda: ed.op_resize(20, 14, "center"), lambda a: E.resize_canvas(a, 20, 14, "center")),
				(lambda: ed.op_scale(2), lambda a: E.scale_int(a, 2)),
				(lambda: ed.op_scale(-2), lambda a: E.downscale_int(a, 2)),
				(lambda: ed.op_shift(3, -1), lambda a: E.shift(a, 3, -1, True)),
				(lambda: ed.op_trim(), E.trim)):
			cur = core(cur)
			fn()
			check(eq(ed.image, cur), "image op equals core ({0})".format(ed.history.history()[-1][0]))
		ed.set_param("shift_wrap", False)
		cur = E.shift(cur, -2, 1, False)
		ed.op_shift(-2, 1)
		check(eq(ed.image, cur), "shift without wrap equals core")
		bad = [f for f in (3, 5, 7) if cur.shape[0] % f or cur.shape[1] % f][0]
		check(ed.op_scale(-bad) is None and eq(ed.image, cur), "scale down by a non-divisor refused, image kept")

		# undo / redo
		n = len(ed.history)
		after_ops = ed.image.copy()
		n_ops = len(ed.history) - [lbl for lbl, _ in ed.history.history()].index("flip h")
		for _ in range(n_ops):
			key("z", 122, "ctrl")
		check(eq(ed.image, states[-1]) and n_ops == 10, "Ctrl+Z x{0} back to before the image operations".format(n_ops))
		key("y", 121, "ctrl")
		check(eq(ed.image, E.flip(states[-1], "h")), "Ctrl+Y redoes one step")
		key("z", 122, "ctrl", "shift")
		check(eq(ed.image, E.flip(E.flip(states[-1], "h"), "v")), "Ctrl+Shift+Z redoes")
		hb = ed.history_buttons
		check(len(ed.history) == n and len(hb) == n, "history strip has every step ({0})".format(len(hb)))
		hb[-1].dispatch("on_release")
		check(eq(ed.image, after_ops), "history strip click jumps to the last step")
		first = [b for b in ed.history_buttons if b.text.startswith("open ")]
		check(bool(first), "open is a history step")
		if first:
			first[-1].dispatch("on_release")
			check(eq(ed.image, src_arr) and not ed.is_dirty(), "history jump to open restores the file state, clean")
		ed.history_buttons[-1].dispatch("on_release")
		check(eq(ed.image, after_ops) and ed.is_dirty(), "and back")
		yield 0.05

		# color panel equals core/colors.py
		p = ed.panel
		base = (120, 80, 200, 255)
		for how in C.NUDGES:
			p.set_color(base)
			p.nudge_buttons[how].dispatch("on_release")
			check(p.color == C.nudge(base, how, 0.05), "nudge {0} equals core at 5%".format(how))
		p.step_slider.value = 12
		p.set_color(base)
		p.nudge_buttons["lighter"].dispatch("on_release")
		check(p.color == C.nudge(base, "lighter", 0.12), "step knob 12% equals core")
		p.set_color(base)
		sug = C.suggest(base, 24, 6)
		ok = all(b.tt_color == c for axis in sug for b, c in zip(p.suggest_buttons[axis], sug[axis]))
		check(ok, "suggest rows equal core at delta 24")
		p.delta_slider.value = 40
		sug = C.suggest(base, 40, 6)
		ok = all(b.tt_color == c for axis in sug for b, c in zip(p.suggest_buttons[axis], sug[axis]))
		check(ok and p.delta == 40, "delta knob 40: suggest rows equal core")
		hm = C.harmonies(base)
		check([b.tt_color for b in p.suggest_buttons["harmony"]] == hm["complementary"] + hm["triadic"] + hm["analogous"],
			"harmony row equals core")
		p.suggest_buttons["white"][1].dispatch("on_release")
		check(p.color == sug["white"][1], "click on a suggestion sets the color")
		p.hex_input.text = "#102030"
		p.hex_input.dispatch("on_text_validate")
		check(p.color == (16, 32, 48, 255) and p.sliders["g"].value == 32, "hex entry")
		check(p.apply_hex("zz") is None and p.color == (16, 32, 48, 255), "bad hex refused")
		p.sliders["g"].value = 99
		check(p.color == (16, 99, 48, 255), "G slider")
		p.sliders["a"].value = 128
		check(p.color[3] == 128 and p.hex_input.text == "#10633080", "alpha slider and hex with alpha")
		p.set_color((255, 0, 0, 255))
		p.svbox.on_pick(0.5, 0.5)
		check(p.color == C.hsv_to_rgb(0, 0.5, 0.5) + (255,), "SV box equals core hsv_to_rgb")
		p.hue_slider.value = 120
		check(p.color == C.hsv_to_rgb(120, 0.5, 0.5) + (255,), "hue slider keeps S and V")

		# saved slots
		for i in range(32):
			p.clear_slot(i)
		p.set_color((1, 2, 3, 255))
		p.slot_buttons[0].dispatch("on_release")
		check(p.slots[0][0] == "#010203", "click on an empty slot stores the color")
		p.set_color((9, 9, 9, 255))
		p.slot_buttons[0].dispatch("on_release")
		check(p.color == (1, 2, 3, 255), "click on a filled slot uses it")
		p.set_color((50, 60, 70, 255))
		mods("shift")
		p.slot_buttons[0].dispatch("on_release")
		p.suggest_buttons["r"][0].dispatch("on_release")
		mods()
		check(p.slots[0][0] == "#323c46", "shift-click on a filled slot overwrites it")
		check(p.slots[1][0] == C.to_hex(C.suggest((50, 60, 70, 255), 40)["r"][0]), "shift-click on a suggestion saves it")
		p.rename_slot(0, "slate")
		with open(os.path.join(cfg, "settings.json"), encoding="utf-8") as f:
			stored = json.load(f)
		check(stored.get("editor_slots", [None])[0] == ["#323c46", "slate"], "slots persisted in settings.json")
		p2 = ColorPanel(settings=Settings(cfg))
		check(p2.slots == p.slots and p2.recent == p.recent and p2.delta == 40 and p2.step_percent == 12,
			"a new panel reloads slots, recent, delta and step")
		pal_path = ed.export_palette_path(os.path.join(out_dir, "slots"))
		check(pal_path and pal_path.endswith(".gpl") and os.path.isfile(pal_path), "palette export (.gpl added)")
		saved_slots = [list(s) for s in p.slots]
		for i in range(32):
			p.clear_slot(i)
		ed.import_palette_path(pal_path)
		check([s[0] for s in p.slots] == [s[0] for s in saved_slots] and p.slots[0][1] == "slate",
			"palette import restores the slots")
		jpath = ed.export_palette_path(os.path.join(out_dir, "slots.json"))
		check(E.import_palette(jpath)[0] == ((50, 60, 70, 255), "slate"), "json export keeps names")
		added, left = ed.add_image_colors_to_slots()
		want = [C.to_hex(c) for c in E.image_colors(ed.image) if C.to_hex(c) not in [s[0] for s in saved_slots if s[0]]]
		check(added == min(len(want), 30) and left == max(0, len(want) - 30), "add image colors fills empty slots "
			"({0} added, {1} left)".format(added, left))
		ed.import_palette_path(src)
		src_cols = [C.to_hex(c) for c in E.image_colors(src_arr)][:32]
		check([s[0] for s in p.slots if s[0]] == src_cols, "load palette from image")

		# recent colors and image palette strip
		ed.panel.set_color((5, 250, 5, 255))
		ed.set_tool("pencil")
		stroke(ed, [(0, 0)])
		check(ed.panel.recent[0] == (5, 250, 5, 255) and len(ed.panel.recent) <= 16, "recent: newest first, max 16")
		check([b.tt_color for b in ed.palette_buttons] == E.image_colors(ed.image)[:256], "image palette strip")
		target = ed.palette_buttons[0].tt_color
		ed.palette_click(target)
		check(ed.color == target, "palette strip click selects")
		ed.panel.set_color((7, 7, 7, 255))
		ed.set_tool("bucket")
		before = ed.image.copy()
		ed.palette_buttons[0].dispatch("on_release")
		check(eq(ed.image, E.replace_color(before, target, (7, 7, 7, 255))), "bucket held: palette click replaces "
			"everywhere (equals core)")

		# autosave and restore
		yield 1.3
		ad = ed.autosave_dir()
		check(ad.startswith(cfg) and os.path.isfile(os.path.join(ad, "image.png")), "autosave written to the config dir")
		ed.autosave_now()
		ed2 = type(ed)(name="editor_restore_test")
		check(ed2.restored and eq(ed2.image, ed.image) and ed2.path == ed.path and ed2.is_dirty(),
			"a new editor restores the autosave (image, path, still unsaved)")
		ed2.shutdown()
		# save
		out_png = os.path.join(out_dir, "edited.png")
		ed.write(out_png)
		check(os.path.isfile(out_png) and eq(np.array(Image.open(out_png).convert("RGBA")), ed.image),
			"Save as writes the image (raw RGBA)")
		check(not ed.is_dirty() and ed.path == out_png, "saved: clean, path follows")
		check(not os.path.exists(os.path.join(ad, "image.png")), "save clears the autosave")
		ed.set_tool("pencil")
		stroke(ed, [(1, 0)])
		check(ed.save() == out_png, "Save (Ctrl+S path) writes to the document path")
		# handoff
		saved_img = ed.image.copy()
		sw = app.screens["swap"]
		hand = rng.integers(0, 256, size=(5, 7, 4), dtype=np.uint8)
		sw.result_array = hand
		sw.result_name = "swapped.png"
		app.switch_to("swap")
		yield 0.05
		sw.open_in_editor()
		yield 0.05
		check(app.manager.current == "editor" and eq(ed.image, hand) and ed.path == "" and ed.is_dirty(),
			"Open in Editor handoff: image loaded as an unsaved document")
		check(ed.history.history()[-1][0].startswith("from "), "handoff is a history step")
		key("z", 122, "ctrl")
		check(eq(ed.image, saved_img) and ed.path == out_png and not ed.is_dirty(),
			"undo after the handoff returns to the saved file")

		check(listing(img_dir) == inputs_before, "no file written next to the inputs")
		check(sha(src) == src_sha, "the opened image is unchanged")
		written = set(listing(out_dir))
		check(written == {out_png, pal_path, jpath}, "only the chosen paths were written ({0})".format(
			sorted(os.path.basename(x) for x in written)))

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
	print("GUI_P7_RESULT {0}".format("FAIL" if FAILS else "PASS"))
	return 1 if FAILS else 0


def main():
	if CHILD_FLAG in sys.argv:
		return child()
	tmp = tempfile.mkdtemp(prefix="tt_gui_p7_")
	try:
		part_a()
		part_b(tmp)
	finally:
		shutil.rmtree(tmp, ignore_errors=True)
	print("texturetools gui editor: {0} failed".format(len(FAILS)))
	return 1 if FAILS else 0


if __name__ == "__main__":
	if HERE not in sys.path:
		sys.path.insert(0, HERE)
	sys.exit(main())

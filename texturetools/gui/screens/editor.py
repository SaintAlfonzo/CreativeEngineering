# SPDX-License-Identifier: GPL-3.0-only
"""Texture Editor screen (plan_texture_tools_gui.md section 6, P7): a zoomed-pixel sprite editor over core/edit.py,
core/colors.py and core/undo.py. The editor is the owner's tool (plan R4); it writes a file only on Save / Save as
and its autosave goes to the config dir, never next to an image.

Canvas: the shared PixelPreview (zoom 1-64 on the wheel, pan with middle or right drag, double click fits) with
report_outside on, plus an overlay: grid (zoom 4 and up), selection tint, floating piece outline, mirror axes and a
one-pixel hover cursor. Left button draws with the current tool; Alt + left click picks the color with any tool.
Tools (keys): pencil B, eraser E, line L, rect R, ellipse O (Filled toggle), bucket G (contiguous, tolerance,
diagonal), global fill F (every pixel within tolerance of the clicked color), picker I, select M (rect drag; a click
without a drag deselects), wand W (contiguous toggle); selections combine by the Selection op (shift = add). Drawing
and fills stay inside the selection. Mirror X, Y or both mirrors the pencil, eraser and shapes.
Clipboard: Ctrl+C copy, Ctrl+X cut, Ctrl+V paste as a floating piece (drag inside it to move, arrows nudge, a click
outside it or Enter commits, Esc drops it), Delete clears the selection, Ctrl+A select all, Ctrl+D deselect.
Image operations: flip, rotate, resize canvas with anchor, integer scale up and down (nearest), shift with wrap, trim.
Undo/redo: one UndoStack of (image, path) states with a clickable history strip; Open, New and the Open in Editor
handoff are history steps too, so nothing is lost by opening another image.
Color panel: widgets/colorpanel.py (slots and recent colors persist in the settings store). The image palette strip
under the canvas lists every color of the image: a click selects it, a click with the bucket or global fill tool
held replaces it everywhere (within the selection) with the current color.

Test-facing API: image (uint8 HxWx4), path, selection, floating, clipboard, history (UndoStack), panel, color,
canvas_event(col, row, kind, button='left') (same path as a preview on_pixel event), set_tool(name),
open_path(path), new_image(w, h), write(path), save(), autosave_now(), autosave_dir(), restore_autosave(),
is_dirty(), copy(), cut(), paste(), commit_floating(), cancel_floating(), delete_selection(), select_all(),
deselect(), op_*(...) image operations, history_buttons, palette_buttons, palette_click(color).
"""
import json
import os
import time

import numpy as np
from kivy.app import App
from kivy.clock import Clock
from kivy.core.window import Window
from kivy.graphics import Color, InstructionGroup, Line, Rectangle
from kivy.graphics.texture import Texture
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.scrollview import ScrollView
from kivy.uix.spinner import Spinner
from kivy.uix.textinput import TextInput
from kivy.uix.togglebutton import ToggleButton

from ...core import edit
from ...core.image_io import load_rgba_array, save_raw
from ...core.undo import UndoStack
from ..registry import register_screen
from ..widgets.colorpanel import ColorPanel, color_button
from ..widgets.filebrowser import open_browser
from ..widgets.preview import PixelPreview, to_rgba_u8
from .base import BAR_H, ToolScreen

TOOLS = (("pencil", "Pencil", "b"), ("eraser", "Eraser", "e"), ("line", "Line", "l"), ("rect", "Rect", "r"),
	("ellipse", "Ellipse", "o"), ("bucket", "Bucket", "g"), ("global", "Global fill", "f"), ("picker", "Picker", "i"),
	("select", "Select", "m"), ("wand", "Wand", "w"))
TOOL_KEYS = {k: name for name, _, k in TOOLS}
DRAW_TOOLS = ("pencil", "eraser")
SHAPE_TOOLS = ("line", "rect", "ellipse")
FILL_TOOLS = ("bucket", "global")
SEL_OPS = ("replace", "add", "subtract", "intersect")
PALETTE_FILTERS = ("*.gpl", "*.hex", "*.txt", "*.json", "*.png")
AUTOSAVE_DIR = "editor_autosave"
AUTOSAVE_DELAY = 1.0
HISTORY_SHOWN = 80
NEW_SIZE = 16
SEL_TINT = (80, 160, 255, 110)


def _small_label(text, width):
	lbl = Label(text=text, size_hint_x=None, width=width, font_size="12sp")
	return lbl


def _int_input(value, width=44):
	return TextInput(text=str(value), multiline=False, size_hint_x=None, width=width, input_filter="int",
		font_size="12sp")


def _int(field, default):
	try:
		return int(field.text)
	except (TypeError, ValueError):
		return default


@register_screen
class EditorScreen(ToolScreen):
	tool_name = "editor"
	title = "Editor"
	order = 40

	def __init__(self, **kw):
		self.image = edit.blank(NEW_SIZE, NEW_SIZE)
		self.path = ""
		self.selection = None
		self.floating = None
		self.clipboard = None
		self.history = UndoStack((self.image, self.path), label="new {0}x{0}".format(NEW_SIZE))
		self._saved = {self.path: self.image.copy()}  # path -> image as last opened or saved ("" = new canvas)
		self._stroke = None
		self._hover_px = None
		self._sel_preview = None
		self._sel_tex = None
		self.tool_buttons = {}
		self.history_buttons = []
		self.palette_buttons = []
		self._palette_cache = None
		self._autosave_ev = None
		self._opened = ""
		self.restored = False
		super().__init__(**kw)
		self.preview.report_outside = True
		self.preview.pick_buttons = ["left"]
		self.preview.bind(on_pixel=lambda pv, c, r, b, k: self.canvas_event(c, r, k, b))
		self.preview.checker = bool(self.params.get("checker", True))
		self._overlay = InstructionGroup()
		self.preview.canvas.add(self._overlay)
		self.preview.bind(pos=self._draw_overlay, size=self._draw_overlay, zoom=self._draw_overlay,
			pan=self._draw_overlay, revision=self._draw_overlay)
		self.result_name = "untitled.png"
		if not self.restore_autosave():
			self._show()
			self._refresh_history()
			self._refresh_palette()
		self._update_title()

	# ---- params ----

	def default_params(self):
		return {"tool": "pencil", "filled": False, "tolerance": 0, "diagonal": False, "contiguous": True,
			"mirror": "off", "sel_op": "replace", "paste_blend": False, "grid": True, "checker": True,
			"shift_wrap": True}

	def set_param(self, key, value):
		if self.params.get(key) == value:
			return
		self.params[key] = value
		if key == "tool":
			self._end_stroke()
			if self.floating is not None:
				self.commit_floating()
			for name, b in self.tool_buttons.items():
				b.state = "down" if name == value else "normal"
		elif key == "checker":
			self.preview.checker = bool(value)
		self._draw_overlay()

	def update_params(self, values):
		super().update_params(values)
		for name, b in self.tool_buttons.items():
			b.state = "down" if name == self.params.get("tool") else "normal"
		self.preview.checker = bool(self.params.get("checker", True))
		self._draw_overlay()

	def request_preview(self):
		self._draw_overlay()

	def set_tool(self, name):
		self.set_param("tool", name)

	@property
	def color(self):
		return self.panel.color

	# ---- layout ----

	def _settings(self):
		app = App.get_running_app()
		return getattr(app, "settings", None)

	def _build_layout(self):
		root = BoxLayout(orientation="horizontal", spacing=6, padding=6)
		left = BoxLayout(orientation="vertical", size_hint_x=0.22, spacing=4)
		scroll = ScrollView(do_scroll_x=False)
		self.controls = BoxLayout(orientation="vertical", size_hint_y=None, spacing=4)
		self.controls.bind(minimum_height=self.controls.setter("height"))
		scroll.add_widget(self.controls)
		left.add_widget(scroll)
		left.add_widget(self._preset_bar())
		root.add_widget(left)

		center = BoxLayout(orientation="vertical", spacing=4)
		center.add_widget(self._file_bar())
		self.preview_area = BoxLayout(orientation="vertical")
		self.preview = PixelPreview()
		self.preview_area.add_widget(self.preview)
		center.add_widget(self.preview_area)
		center.add_widget(self._zoom_bar())
		hs = ScrollView(do_scroll_y=False, size_hint_y=None, height=30)
		self.history_row = BoxLayout(size_hint_x=None, spacing=2)
		self.history_row.bind(minimum_width=self.history_row.setter("width"))
		hs.add_widget(self.history_row)
		self.history_scroll = hs
		center.add_widget(hs)
		ps = ScrollView(do_scroll_y=False, size_hint_y=None, height=28)
		self.palette_row = BoxLayout(size_hint_x=None, spacing=1)
		self.palette_row.bind(minimum_width=self.palette_row.setter("width"))
		ps.add_widget(self.palette_row)
		center.add_widget(ps)
		self.status = Label(text="", size_hint_y=None, height=24, halign="left", valign="middle")
		self.status.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		center.add_widget(self.status)
		root.add_widget(center)

		right = ScrollView(do_scroll_x=False, size_hint_x=0.28)
		self.panel = ColorPanel(on_color=self._color_changed, settings=self._settings(), on_status=self.set_status)
		right.add_widget(self.panel)
		root.add_widget(right)
		self.add_widget(root)
		self.panel.add_slot_action("Add image colors", self.add_image_colors_to_slots)
		self.panel.add_slot_action("Load palette from image", lambda: self.import_palette_dialog(images_only=True))
		self.panel.add_slot_action("Import palette", self.import_palette_dialog)
		self.panel.add_slot_action("Export palette", self.export_palette_dialog)
		self.run_button = Button(disabled=True)  # the editor has no Run; base.run() never reaches it

	def _file_bar(self):
		bar = BoxLayout(size_hint_y=None, height=BAR_H, spacing=4)
		self.doc_label = Label(text="", halign="left", valign="middle")
		self.doc_label.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		for text, fn in (("Open", self.open_dialog), ("Save", self.save), ("Save as", self.save_as)):
			bar.add_widget(self._button(text, fn, 80))
		bar.add_widget(_small_label("New", 34))
		self.new_w = _int_input(NEW_SIZE)
		self.new_h = _int_input(NEW_SIZE)
		bar.add_widget(self.new_w)
		bar.add_widget(_small_label("x", 12))
		bar.add_widget(self.new_h)
		bar.add_widget(self._button("New", lambda: self.new_image(_int(self.new_w, NEW_SIZE),
			_int(self.new_h, NEW_SIZE)), 50))
		bar.add_widget(self.doc_label)
		return bar

	def _zoom_bar(self):
		bar = super()._zoom_bar()
		bar.add_widget(self._button("Undo", self.undo, 60))
		bar.add_widget(self._button("Redo", self.redo, 60))
		self.cursor_label = Label(text="", size_hint_x=None, width=150, font_size="12sp")
		bar.add_widget(self.cursor_label)
		return bar

	def _row(self, *widgets, height=None):
		row = BoxLayout(size_hint_y=None, height=height or BAR_H - 4, spacing=3)
		for w in widgets:
			row.add_widget(w)
		self.controls.add_widget(row)
		return row

	def _btn(self, text, fn, width=None):
		b = Button(text=text, font_size="12sp", size_hint_x=None if width else 1, width=width or 10)
		b.bind(on_release=lambda *a: fn())
		return b

	def build_controls(self):
		self.add_heading("Tools")
		grid = GridLayout(cols=2, size_hint_y=None, height=5 * 30 + 8, spacing=2)
		for name, text, key in TOOLS:
			b = ToggleButton(text="{0} ({1})".format(text, key.upper()), group="tt-editor-tool", font_size="12sp",
				allow_no_selection=False, state="down" if self.params["tool"] == name else "normal")
			b.bind(on_release=lambda inst, n=name: self.set_tool(n))
			self.tool_buttons[name] = b
			grid.add_widget(b)
		self.controls.add_widget(grid)
		self.add_toggle("filled", "Filled shapes")
		self.add_choice("mirror", "Mirror", ["off", "x", "y", "both"])
		self.add_number("tolerance", "Tolerance", 0, 255, 1)
		self.add_toggle("diagonal", "Diagonal fill")
		self.add_toggle("contiguous", "Wand contiguous")

		self.add_heading("Selection")
		self.add_choice("sel_op", "Combine", list(SEL_OPS))
		self._row(self._btn("All", self.select_all), self._btn("None", self.deselect),
			self._btn("Delete", self.delete_selection))
		self._row(self._btn("Copy", self.copy), self._btn("Cut", self.cut), self._btn("Paste", self.paste))
		self._row(self._btn("Commit", self.commit_floating), self._btn("Drop", self.cancel_floating))
		self.add_toggle("paste_blend", "Paste over")

		self.add_heading("View")
		self.add_toggle("grid", "Grid")
		self.add_toggle("checker", "Checkerboard")

		self.add_heading("Image")
		self._row(self._btn("Flip H", lambda: self.op_flip("h")), self._btn("Flip V", lambda: self.op_flip("v")),
			self._btn("Trim", self.op_trim))
		self._row(self._btn("Rot 90", lambda: self.op_rotate(90)), self._btn("Rot 180", lambda: self.op_rotate(180)),
			self._btn("Rot 270", lambda: self.op_rotate(270)))
		self.resize_w = _int_input(NEW_SIZE, 40)
		self.resize_h = _int_input(NEW_SIZE, 40)
		self.anchor = Spinner(text="center", values=list(edit.ANCHORS), font_size="11sp")
		self._row(_small_label("Canvas", 46), self.resize_w, self.resize_h, self.anchor)
		self._row(self._btn("Resize canvas", lambda: self.op_resize(_int(self.resize_w, 0), _int(self.resize_h, 0),
			self.anchor.text)))
		self.scale_factor = Spinner(text="2", values=["2", "3", "4", "8"], size_hint_x=None, width=46)
		self._row(_small_label("Scale", 40), self.scale_factor,
			self._btn("Up", lambda: self.op_scale(int(self.scale_factor.text))),
			self._btn("Down", lambda: self.op_scale(-int(self.scale_factor.text))))
		self.shift_dx = _int_input(1, 36)
		self.shift_dy = _int_input(0, 36)
		self._row(_small_label("Shift", 40), self.shift_dx, self.shift_dy,
			self._btn("Shift", lambda: self.op_shift(_int(self.shift_dx, 0), _int(self.shift_dy, 0))))
		self.add_toggle("shift_wrap", "Shift wraps")

	# ---- document state ----

	def is_dirty(self):
		img = self._saved.get(self.path)
		return img is None or img.shape != self.image.shape or not np.array_equal(img, self.image)

	def _update_title(self):
		name = os.path.basename(self.path) if self.path else (self.result_name or "untitled.png")
		h, w = self.image.shape[:2]
		self.doc_label.text = "{0}{1}  {2}x{3}".format(name, " *" if self.is_dirty() else "", w, h)

	def _show(self):
		arr = self.image
		if self.floating is not None:
			arr = edit.commit_floating(arr, self.floating, blend=bool(self.params.get("paste_blend")))
		self.result_array = self.image
		self.preview.set_array(arr)
		self._draw_overlay()

	def _push(self, label):
		self.history.push((self.image, self.path), label)
		self._after_change()

	def _after_change(self):
		self.result_array = self.image
		self._show()
		self._refresh_history()
		self._refresh_palette()
		self._update_title()
		self._schedule_autosave()

	def commit(self, img, label, record_color=None):
		"""New image as one history step."""
		if self.selection is not None and self.selection.shape != img.shape[:2]:
			self.selection = None
			self._sel_tex = None
		self.image = img
		self._push(label)
		if record_color is not None:
			self.panel.add_recent(record_color)

	def _restore(self, state):
		img, path = state
		self.image = to_rgba_u8(img)
		self.path = path
		if self.path:
			self.result_name = os.path.basename(self.path)
		if self.selection is not None and self.selection.shape != self.image.shape[:2]:
			self.selection = None
			self._sel_tex = None
		self._after_change()

	def undo(self):
		self._end_stroke()
		if self.floating is not None:
			self.cancel_floating()
			return
		state = self.history.undo()
		if state is not None:
			self._restore(state)
			self.set_status("undo")

	def redo(self):
		self._end_stroke()
		if self.floating is not None:
			self.commit_floating()
		state = self.history.redo()
		if state is not None:
			self._restore(state)
			self.set_status("redo")

	def jump(self, i):
		self._end_stroke()
		self.floating = None
		self._restore(self.history.jump(i))

	def _refresh_history(self):
		self.history_row.clear_widgets()
		self.history_buttons = []
		hist = self.history.history()
		start = max(0, len(hist) - HISTORY_SHOWN)
		if start:
			self.history_row.add_widget(Label(text="+{0}".format(start), size_hint_x=None, width=40,
				font_size="11sp"))
		for i in range(start, len(hist)):
			label, cur = hist[i]
			b = Button(text=label, size_hint_x=None, width=max(54, 7 * len(label) + 12), font_size="11sp",
				background_color=[0.3, 0.55, 0.9, 1] if cur else [1, 1, 1, 1])
			b.tt_index = i
			b.bind(on_release=lambda inst: self.jump(inst.tt_index))
			self.history_row.add_widget(b)
			self.history_buttons.append(b)
		Clock.schedule_once(lambda dt: setattr(self.history_scroll, "scroll_x", 1), 0)

	def _refresh_palette(self):
		pal = edit.image_colors(self.image)[:256]
		if pal == self._palette_cache:
			return
		self._palette_cache = pal
		self.palette_row.clear_widgets()
		self.palette_buttons = []
		for c in pal:
			b = color_button(c, lambda inst: self.palette_click(inst.tt_color))
			b.size_hint_x = None
			b.width = 24
			self.palette_row.add_widget(b)
			self.palette_buttons.append(b)

	def palette_click(self, color):
		"""Image palette strip: select the color, or with a bucket tool held replace it everywhere."""
		if self.params.get("tool") in FILL_TOOLS:
			if self.floating is not None:
				self.commit_floating()
			new = self.color
			img = edit.replace_color(self.image, color, new, int(self.params.get("tolerance", 0)), self.selection)
			self.commit(img, "replace", record_color=new)
			self.set_status("replaced {0} with {1}".format(_hex(color), _hex(new)))
		else:
			self.panel.set_color(color)

	def _color_changed(self, c):
		self._draw_overlay()

	# ---- canvas input ----

	def _mods(self):
		return set(Window.modifiers or [])

	def _in(self, c, r):
		return edit.in_bounds(self.image, c, r)

	def _mirror(self):
		m = self.params.get("mirror", "off")
		return None if m in ("off", "", None) else m

	def _end_stroke(self):
		"""Drop an unfinished stroke (tool switch, undo): a pencil stroke in progress is reverted."""
		s = self._stroke
		self._stroke = None
		self._sel_preview = None
		if s and s.get("kind") == "pencil":
			self.image = s["base"]
			self._show()

	def _in_floating(self, c, r):
		f = self.floating
		if f is None:
			return False
		fw, fh = f.size
		return f.x <= c < f.x + fw and f.y <= r < f.y + fh

	def canvas_event(self, col, row, kind, button="left"):
		"""One pointer event on the canvas (col, row may be off the image); kind 'down' | 'move' | 'up'."""
		if button != "left":
			return
		if kind == "down":
			self._down(col, row)
		elif self._stroke is not None:
			self._drag(col, row)
			if kind == "up":
				self._up(col, row)

	def _down(self, c, r):
		self._stroke = None
		tool = self.params.get("tool", "pencil")
		mods = self._mods()
		if self.floating is not None:
			if self._in_floating(c, r):
				self._stroke = {"kind": "move", "start": (c, r), "f0": self.floating}
			else:
				self.commit_floating()
				self._stroke = {"kind": "none"}
			return
		if "alt" in mods or tool == "picker":
			self._stroke = {"kind": "pick"}
			self._pick(c, r)
			return
		sel = self.selection
		if tool in DRAW_TOOLS:
			color = self._draw_color(tool)
			self._stroke = {"kind": "pencil", "base": self.image, "prev": (c, r), "tool": tool, "color": color}
			self.image = edit.pencil(self.image, c, r, color, None, self._mirror(), sel)
			self._show()
		elif tool in SHAPE_TOOLS:
			self._stroke = {"kind": "shape", "start": (c, r), "cur": (c, r), "tool": tool}
			self._show_shape()
		elif tool in FILL_TOOLS:
			self._stroke = {"kind": "none"}
			if not self._in(c, r):
				return
			tol = int(self.params.get("tolerance", 0))
			color = self.color
			if tool == "bucket":
				img = edit.bucket_fill(self.image, c, r, color, tol, bool(self.params.get("diagonal")), sel)
			else:
				img = edit.global_fill(self.image, c, r, color, tol, sel)
			self.commit(img, "bucket" if tool == "bucket" else "global fill", record_color=color)
		elif tool == "select":
			self._stroke = {"kind": "select", "start": (c, r), "cur": (c, r), "op": self._sel_op(mods)}
			self._sel_preview = (c, r, c, r)
			self._draw_overlay()
		elif tool == "wand":
			self._stroke = {"kind": "none"}
			if self._in(c, r):
				m = edit.magic_wand(self.image, c, r, int(self.params.get("tolerance", 0)),
					bool(self.params.get("contiguous", True)), bool(self.params.get("diagonal")))
				self._set_selection(edit.combine_selection(self.selection, m, self._sel_op(mods)))

	def _drag(self, c, r):
		s = self._stroke
		k = s["kind"]
		if k == "pick":
			self._pick(c, r)
		elif k == "pencil":
			if (c, r) != s["prev"]:
				self.image = edit.pencil(self.image, c, r, s["color"], s["prev"], self._mirror(), self.selection)
				s["prev"] = (c, r)
				self._show()
		elif k == "shape":
			if (c, r) != s["cur"]:
				s["cur"] = (c, r)
				self._show_shape()
		elif k == "select":
			s["cur"] = (c, r)
			self._sel_preview = s["start"] + (c, r)
			self._draw_overlay()
		elif k == "move":
			f0 = s["f0"]
			self.floating = f0.moved(c - s["start"][0], r - s["start"][1])
			self._show()

	def _up(self, c, r):
		s = self._stroke
		self._stroke = None
		k = s["kind"]
		if k == "pencil":
			self.commit(self.image, s["tool"], record_color=s["color"] if s["tool"] == "pencil" else None)
		elif k == "shape":
			img = self._shape_image(s)
			self.commit(img, s["tool"], record_color=self.color)
		elif k == "select":
			self._sel_preview = None
			(x0, y0), (x1, y1) = s["start"], s["cur"]
			if (x0, y0) == (x1, y1) and s["op"] == "replace":
				self.deselect()
			else:
				m = edit.select_rect(self.image.shape, x0, y0, x1, y1)
				self._set_selection(edit.combine_selection(self.selection, m, s["op"]))

	def _draw_color(self, tool):
		return edit.TRANSPARENT if tool == "eraser" else self.color

	def _sel_op(self, mods):
		return "add" if "shift" in mods else self.params.get("sel_op", "replace")

	def _pick(self, c, r):
		if self._in(c, r):
			self.panel.set_color(edit.eyedropper(self.image, c, r))

	def _shape_image(self, s):
		(x0, y0), (x1, y1) = s["start"], s["cur"]
		fn = getattr(edit, s["tool"])
		args = (self.image, x0, y0, x1, y1, self.color)
		if s["tool"] == "line":
			return fn(*args, mirror=self._mirror(), selection=self.selection)
		return fn(*args, filled=bool(self.params.get("filled")), mirror=self._mirror(), selection=self.selection)

	def _show_shape(self):
		self.preview.set_array(self._shape_image(self._stroke))
		self._draw_overlay()

	# ---- selection and clipboard ----

	def _set_selection(self, mask):
		if mask is not None and not mask.any():
			mask = None
		self.selection = mask
		self._sel_tex = None
		self._draw_overlay()
		n = 0 if mask is None else int(mask.sum())
		self.set_status("selection: {0} px".format(n) if n else "no selection")

	def select_all(self):
		self._set_selection(edit.select_all(self.image))

	def deselect(self):
		self._set_selection(None)

	def delete_selection(self):
		if self.selection is None:
			self.set_status("nothing selected")
			return
		self.commit(edit.fill_mask(self.image, self.selection, edit.TRANSPARENT), "delete")

	def copy(self):
		if self.floating is not None:
			f = self.floating
			self.clipboard = edit.Floating(f.pixels.copy(), f.mask.copy(), f.x, f.y)
		elif self.selection is not None:
			self.clipboard = edit.copy_selection(self.image, self.selection)
		else:
			self.clipboard = edit.paste(self.image, 0, 0)
		if self.clipboard is not None:
			w, h = self.clipboard.size
			self.set_status("copied {0}x{1}".format(w, h))
		return self.clipboard

	def cut(self):
		if self.selection is None:
			self.set_status("select something to cut")
			return None
		img, piece = edit.cut_selection(self.image, self.selection)
		self.clipboard = piece
		self.commit(img, "cut")
		return piece

	def paste(self):
		if self.clipboard is None:
			self.set_status("clipboard is empty")
			return None
		if self.floating is not None:
			self.commit_floating()
		clip = self.clipboard
		h, w = self.image.shape[:2]
		cw, ch = clip.size
		x, y = (clip.x, clip.y) if (0 <= clip.x and 0 <= clip.y and clip.x + cw <= w and clip.y + ch <= h) else (0, 0)
		self.floating = edit.Floating(clip.pixels.copy(), clip.mask.copy(), x, y)
		self.selection = None
		self._sel_tex = None
		self._show()
		self.set_status("floating: drag inside to move, arrows nudge, click outside or Enter commits, Esc drops")
		return self.floating

	def commit_floating(self):
		if self.floating is None:
			return
		img = edit.commit_floating(self.image, self.floating, blend=bool(self.params.get("paste_blend")))
		self.floating = None
		self.commit(img, "paste")

	def cancel_floating(self):
		if self.floating is None:
			return
		self.floating = None
		self._show()
		self.set_status("floating piece dropped")

	def nudge_floating(self, dx, dy):
		if self.floating is not None:
			self.floating = self.floating.moved(dx, dy)
			self._show()

	# ---- image operations ----

	def _op(self, fn, label):
		self._end_stroke()
		if self.floating is not None:
			self.commit_floating()
		try:
			img = fn(self.image)
		except ValueError as e:
			self.set_status(str(e))
			return None
		self.commit(img, label)
		return img

	def op_flip(self, axis):
		return self._op(lambda a: edit.flip(a, axis), "flip " + axis)

	def op_rotate(self, deg):
		return self._op(lambda a: edit.rotate(a, deg), "rotate {0}".format(deg))

	def op_resize(self, w, h, anchor="center"):
		if w < 1 or h < 1 or w > 4096 or h > 4096:
			self.set_status("canvas size must be 1 to 4096")
			return None
		return self._op(lambda a: edit.resize_canvas(a, w, h, anchor), "canvas {0}x{1}".format(w, h))

	def op_scale(self, factor):
		"""factor > 0 scales up by it, < 0 scales down by -factor (nearest)."""
		if factor > 0:
			h, w = self.image.shape[:2]
			if w * factor > 4096 or h * factor > 4096:
				self.set_status("result would be larger than 4096")
				return None
			return self._op(lambda a: edit.scale_int(a, factor), "scale x{0}".format(factor))
		return self._op(lambda a: edit.downscale_int(a, -factor), "scale /{0}".format(-factor))

	def op_shift(self, dx, dy):
		wrap = bool(self.params.get("shift_wrap", True))
		return self._op(lambda a: edit.shift(a, dx, dy, wrap), "shift {0},{1}".format(dx, dy))

	def op_trim(self):
		return self._op(edit.trim, "trim")

	# ---- files ----

	def _load_doc(self, arr, path, label, name=None, saved=True):
		self._end_stroke()
		self.floating = None
		self.selection = None
		self._sel_tex = None
		self.image = to_rgba_u8(arr)
		self.path = path
		self.result_name = name or (os.path.basename(path) if path else "untitled.png")
		if saved:
			self._saved[path] = self.image.copy()
		self.history.push((self.image, self.path), label)
		self.preview.set_array(self.image, fit=True)
		self._after_change()
		h, w = self.image.shape[:2]
		self.resize_w.text, self.resize_h.text = str(w), str(h)

	def open_dialog(self):
		open_browser("file", lambda paths: self.open_path(paths[0]), title="Open image")

	def open_path(self, path):
		try:
			arr = load_rgba_array(path)
		except (OSError, ValueError) as e:
			self.set_status("not opened: {0}".format(e))
			return False
		self._load_doc(arr, os.path.abspath(path), "open " + os.path.basename(path))
		self._opened = os.path.abspath(path)
		s = self._settings()
		if s is not None:
			try:
				s.add_recent(path)
			except OSError:
				pass
		self.set_status("opened {0}".format(path))
		return True

	def new_image(self, w, h):
		if w < 1 or h < 1 or w > 4096 or h > 4096:
			self.set_status("size must be 1 to 4096")
			return
		self._load_doc(edit.blank(w, h), "", "new {0}x{1}".format(w, h))
		self._opened = ""
		self.set_status("new {0}x{1} canvas".format(w, h))

	def load_array(self, arr, name=""):
		"""Open in Editor handoff from another screen: a new unsaved document (one history step)."""
		self._load_doc(arr, "", "from " + (name or "screen"), name=name or "untitled.png", saved=False)
		self._opened = ""
		self.set_status("loaded {0} (unsaved)".format(name or "image"))

	def write(self, path):
		"""Write the image (a floating piece is committed first). The only file write besides autosave."""
		if self.floating is not None:
			self.commit_floating()
		if not path.lower().endswith(".png"):
			path += ".png"
		path = os.path.abspath(path)
		save_raw(self.image, path)
		if path != self.path:
			self.path = path
			self.history.replace_current((self.image, self.path))
		self.result_name = os.path.basename(path)
		self._saved[path] = self.image.copy()
		self.clear_autosave()
		self._update_title()
		self.set_status("saved {0}".format(path))
		self.after_run(self.params, [path])
		return path

	def save(self):
		if not self.path:
			self.save_as()
			return None
		return self.write(self.path)

	def save_shortcut(self):
		self.save()

	def save_as(self):
		open_browser("save", lambda paths: self.write(paths[0]), title="Save image",
			default_name=os.path.basename(self.path) if self.path else self.result_name or "untitled.png")

	def provenance_inputs(self, params):
		return [self._opened] if self._opened else []

	def cli_argv(self, params):
		return None

	# ---- autosave ----

	def autosave_dir(self):
		s = self._settings()
		if s is not None:
			base = s.directory
		else:
			from ..settings import config_dir
			base = config_dir()
		return os.path.join(base, AUTOSAVE_DIR)

	def _schedule_autosave(self):
		if self._autosave_ev is not None:
			self._autosave_ev.cancel()
		self._autosave_ev = Clock.schedule_once(lambda dt: self.autosave_now(), AUTOSAVE_DELAY)

	def autosave_now(self):
		"""Unsaved state -> config dir (image.png + state.json); a clean document removes the autosave."""
		self._autosave_ev = None
		if not self.is_dirty():
			self.clear_autosave()
			return None
		d = self.autosave_dir()
		try:
			os.makedirs(d, exist_ok=True)
			img_path = os.path.join(d, "image.png")
			tmp = os.path.join(d, "image.tmp.png")
			save_raw(self.image, tmp)
			os.replace(tmp, img_path)
			state = {"path": self.path, "name": self.result_name, "saved_at": time.strftime("%Y-%m-%d %H:%M"),
				"size": [int(self.image.shape[1]), int(self.image.shape[0])]}
			with open(os.path.join(d, "state.json.tmp"), "w", encoding="utf-8") as f:
				json.dump(state, f, indent="\t")
			os.replace(os.path.join(d, "state.json.tmp"), os.path.join(d, "state.json"))
		except OSError as e:
			self.set_status("autosave failed: {0}".format(e))
			return None
		return img_path

	def clear_autosave(self):
		d = self.autosave_dir()
		for n in ("image.png", "state.json"):
			try:
				os.remove(os.path.join(d, n))
			except OSError:
				pass

	def restore_autosave(self):
		"""On start: an autosave left by an unsaved session comes back as the current document (still unsaved)."""
		d = self.autosave_dir()
		img_path, st_path = os.path.join(d, "image.png"), os.path.join(d, "state.json")
		if not (os.path.isfile(img_path) and os.path.isfile(st_path)):
			return False
		try:
			arr = load_rgba_array(img_path)
			with open(st_path, encoding="utf-8") as f:
				state = json.load(f)
		except (OSError, ValueError) as e:
			self.set_status("autosave not restored: {0}".format(e))
			return False
		path = state.get("path") or ""
		self.image = to_rgba_u8(arr)
		self.path = path
		self.result_name = state.get("name") or "untitled.png"
		self.history.clear((self.image, self.path), "restored autosave")
		self._saved = {}
		if path and os.path.isfile(path):
			try:
				self._saved[path] = to_rgba_u8(load_rgba_array(path))
			except (OSError, ValueError):
				pass
		self.restored = True
		self.preview.set_array(self.image, fit=True)
		self._after_change()
		self.set_status("restored unsaved work from {0}".format(state.get("saved_at", "?")))
		return True

	# ---- palette file actions ----

	def add_image_colors_to_slots(self):
		added, left = self.panel.add_image_colors(edit.image_colors(self.image))
		self.set_status("added {0} image colors to slots{1}".format(added,
			", {0} did not fit".format(left) if left else ""))
		return added, left

	def import_palette_path(self, path):
		try:
			entries = edit.import_palette(path)
		except (OSError, ValueError, KeyError) as e:
			self.set_status("palette not loaded: {0}".format(e))
			return None
		n, left = self.panel.load_slots(entries)
		self.set_status("loaded {0} colors into slots{1}".format(n, ", {0} left out".format(left) if left else ""))
		return n

	def import_palette_dialog(self, images_only=False):
		open_browser("file", lambda paths: self.import_palette_path(paths[0]),
			filters=("*.png",) if images_only else PALETTE_FILTERS,
			title="Load palette from image" if images_only else "Import palette")

	def export_palette_path(self, path):
		if os.path.splitext(path)[1].lower() not in (".gpl", ".hex", ".txt", ".json"):
			path += ".gpl"
		try:
			edit.export_palette(self.panel.slot_entries(), path)
		except (OSError, ValueError) as e:
			self.set_status("palette not written: {0}".format(e))
			return None
		self.set_status("exported {0} slots to {1}".format(len(self.panel.slot_entries()), path))
		return path

	def export_palette_dialog(self):
		open_browser("save", lambda paths: self.export_palette_path(paths[0]), filters=PALETTE_FILTERS[:-1],
			title="Export saved slots", default_name="palette.gpl")

	# ---- overlay ----

	def _selection_texture(self):
		if self._sel_tex is None and self.selection is not None:
			h, w = self.selection.shape
			buf = np.zeros((h, w, 4), dtype=np.uint8)
			buf[self.selection] = SEL_TINT
			tex = Texture.create(size=(w, h), colorfmt="rgba")
			tex.mag_filter = tex.min_filter = "nearest"
			tex.blit_buffer(np.ascontiguousarray(np.flipud(buf)).tobytes(), colorfmt="rgba", bufferfmt="ubyte")
			self._sel_tex = tex
		return self._sel_tex

	def _px_rect(self, x0, y0, x1, y1):
		"""Widget rect of the inclusive pixel box (rows top-down)."""
		ox, oy, sw, sh = self.preview.image_rect()
		z = self.preview.zoom
		h = self.image.shape[0]
		xa, xb = sorted((x0, x1))
		ya, yb = sorted((y0, y1))
		return ox + xa * z, oy + (h - 1 - yb) * z, (xb - xa + 1) * z, (yb - ya + 1) * z

	def _draw_overlay(self, *a):
		g = getattr(self, "_overlay", None)
		if g is None:
			return
		g.clear()
		if self.preview.array is None:
			return
		ox, oy, sw, sh = self.preview.image_rect()
		z = self.preview.zoom
		h, w = self.preview.array.shape[:2]
		if self.params.get("grid") and z >= 4:
			g.add(Color(0, 0, 0, 0.28))
			pts = []
			for i in range(w + 1):
				x = ox + i * z
				pts += [x, oy, x, oy + sh] if i % 2 == 0 else [x, oy + sh, x, oy]
			g.add(Line(points=pts, width=1))
			pts = []
			for j in range(h + 1):
				y = oy + j * z
				pts += [ox, y, ox + sw, y] if j % 2 == 0 else [ox + sw, y, ox, y]
			g.add(Line(points=pts, width=1))
		m = self._mirror()
		if m:
			g.add(Color(1, 0.4, 0.8, 0.7))
			if m in ("x", "both"):
				g.add(Line(points=[ox + sw / 2.0, oy, ox + sw / 2.0, oy + sh], width=1))
			if m in ("y", "both"):
				g.add(Line(points=[ox, oy + sh / 2.0, ox + sw, oy + sh / 2.0], width=1))
		tex = self._selection_texture()
		if tex is not None and self.selection.shape == (h, w):
			g.add(Color(1, 1, 1, 1))
			g.add(Rectangle(texture=tex, pos=(ox, oy), size=(sw, sh)))
			box = edit.selection_bbox(self.selection)
			if box:
				g.add(Color(0.3, 0.6, 1, 0.9))
				g.add(Line(rectangle=self._px_rect(*box), width=1, dash_length=4, dash_offset=4))
		if self._sel_preview is not None:
			g.add(Color(0.3, 0.6, 1, 1))
			g.add(Line(rectangle=self._px_rect(*self._sel_preview), width=1))
		if self.floating is not None:
			f = self.floating
			fw, fh = f.size
			g.add(Color(1, 0.8, 0.2, 1))
			g.add(Line(rectangle=self._px_rect(f.x, f.y, f.x + fw - 1, f.y + fh - 1), width=1.2))
		p = self._hover_px
		if p is not None and 0 <= p[0] < w and 0 <= p[1] < h:
			g.add(Color(1, 1, 1, 0.9))
			g.add(Line(rectangle=self._px_rect(p[0], p[1], p[0], p[1]), width=1))
			g.add(Color(0, 0, 0, 0.9))
			x, y, ww, hh = self._px_rect(p[0], p[1], p[0], p[1])
			g.add(Line(rectangle=(x - 1, y - 1, ww + 2, hh + 2), width=1))

	# ---- window hooks ----

	def on_enter(self, *a):
		Window.bind(on_key_down=self._key, mouse_pos=self._hover)

	def on_leave(self, *a):
		Window.unbind(on_key_down=self._key, mouse_pos=self._hover)
		self._hover_px = None

	def _hover(self, win, pos):
		p = self.preview.pixel_at(*pos) if self.preview.collide_point(*self.preview.to_widget(*pos)) else None
		if p != self._hover_px:
			self._hover_px = p
			self._draw_overlay()
			inside = p is not None and self._in(p[0], p[1])
			self.cursor_label.text = "{0}, {1}  {2}".format(p[0], p[1], _hex(edit.eyedropper(self.image, p[0],
				p[1]))) if inside else ""

	def _text_focused(self):
		return any(isinstance(w, TextInput) and w.focus for w in self.walk(restrict=True))

	def _key(self, win, key, scancode, codepoint, modifiers):
		if self.manager is None or self.manager.current != self.name or self._text_focused():
			return False
		mods = set(modifiers or [])
		if "ctrl" in mods:
			fn = {"c": self.copy, "x": self.cut, "v": self.paste, "a": self.select_all, "d": self.deselect}.get(
				codepoint)
			if fn is None:
				return False
			fn()
			return True
		if key == 27:
			if self.floating is not None:
				self.cancel_floating()
			else:
				self.deselect()
			return True
		if key in (13, 271):
			self.commit_floating()
			return True
		if key in (127, 8):
			self.delete_selection()
			return True
		arrows = {276: (-1, 0), 275: (1, 0), 273: (0, -1), 274: (0, 1)}
		if key in arrows and self.floating is not None:
			self.nudge_floating(*arrows[key])
			return True
		if codepoint in TOOL_KEYS:
			self.set_tool(TOOL_KEYS[codepoint])
			return True
		return False

	def shutdown(self):
		if self._autosave_ev is not None:
			self._autosave_ev.cancel()
		if self.is_dirty():
			self.autosave_now()
		super().shutdown()


def _hex(c):
	from ...core.colors import to_hex
	return to_hex(c)

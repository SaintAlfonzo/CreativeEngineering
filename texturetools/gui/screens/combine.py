# SPDX-License-Identifier: GPL-3.0-only
"""Combine screen: a layer stack (plan_texture_tools_gui.md section 4, P6).

params["layers"] is a list of layer dicts: the core Layer fields (core/combine.py layer_to_dict) plus two GUI keys,
"varies" (the layer's image is a folder filled file by file in a bulk run) and "resize" (none, scale or size).
build_stack(params) is the one translation to (layers, combine_stack options, vary); the preview, Run, bulk jobs,
the stack JSON export and Copy as CLI all use it. Layer 0 is the bottom (base) layer; the list shows the top layer
first, as image editors do. Bulk: one varying layer runs every file of its folder; two or more run every combination
(cartesian), which Run only starts after the Confirm button for the current count was pressed.
A left drag on the preview moves the selected layer (whole pixels; "Snap" steps by the layer's scale factor).
"""
import json
import os

from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.checkbox import CheckBox
from kivy.uix.label import Label
from kivy.uix.spinner import Spinner
from kivy.uix.togglebutton import ToggleButton

from ...core import batch
from ...core.combine import (ANCHORS, BLEND_MODES, FIT_POLICIES, OFFSET_MODES, Layer, combine_stack,
							 count_stack_jobs, layer_from_dict, layer_to_dict, parse_color, stack_from_dict,
							 stack_jobs, stack_to_dict)
from ...core.image_io import save_raw
from ..registry import register_screen
from ..widgets.controls import ChoiceControl, NumberControl, ToggleControl
from ..widgets.fileslot import FileSlot
from ..widgets.filebrowser import open_browser
from .base import ToolScreen
from .bulkui import BulkPreviewMixin

CLI_MODULE = "texturetools.cli.combine_tex"
RESIZE = ("none", "scale", "size")
LAYER_GUI_KEYS = ("varies", "resize")


def new_layer(name="", **kw):
	d = layer_to_dict(Layer(name=name))
	d.update({"varies": False, "resize": "none"})
	d.update(kw)
	return d


def default_params():
	return {
		"layers": [new_layer("base"), new_layer("layer 1")],
		"canvas": "base", "canvas_w": 16, "canvas_h": 16, "background": "", "lcm": False, "fit": "any",
		"snap": False,
		"output": "", "out_dir": "", "pattern": "{name}.png", "include": "", "exclude": "", "overwrite": "replace",
		"dry_run": False,
	}


def _globs(text):
	return [g.strip() for g in (text or "").split(",") if g.strip()] or None


def _layer(d):
	d = dict(d)
	kind = d.get("resize") or ("scale" if d.get("scale") is not None else "size" if d.get("size") else "none")
	if kind != "scale":
		d["scale"] = None
	if kind != "size":
		d["size"] = None
	for k in ("image", "mask"):
		if not d.get(k):
			d[k] = None
	return layer_from_dict(d)


def stack_options(params):
	canvas = params.get("canvas", "base")
	if canvas == "fixed":
		canvas = (int(params.get("canvas_w") or 16), int(params.get("canvas_h") or 16))
	bg = (params.get("background") or "").strip()
	return {"canvas": canvas, "background": parse_color(bg) if bg else None, "lcm": bool(params.get("lcm")),
		"fit": params.get("fit") or "any"}


def build_stack(params):
	"""(layers, options, vary): Layer list, combine_stack kwargs, {slot index: folder} of the varying layers."""
	layers = [_layer(d) for d in params.get("layers") or []]
	vary = {i: d.get("image") for i, d in enumerate(params.get("layers") or []) if d.get("varies")}
	return layers, stack_options(params), vary


def stack_doc(params):
	"""The stack preset JSON (combine_tex.py --stack) for params."""
	layers, opts, vary = build_stack(params)
	return stack_to_dict(layers, opts["canvas"], opts["background"], opts["lcm"], opts["fit"], sorted(vary))


def params_from_stack_doc(doc, base_dir=None, keep=None):
	"""params from a stack preset JSON (import); keep = current params for the GUI-only keys (output, pattern ...)."""
	layers, opts = stack_from_dict(doc, base_dir)
	p = dict(keep or default_params())
	vary = set(opts.get("vary") or [])
	p["layers"] = []
	for i, l in enumerate(layers):
		d = layer_to_dict(l)
		d["varies"] = i in vary
		d["resize"] = "scale" if l.scale is not None else "size" if l.size is not None else "none"
		p["layers"].append(d)
	canvas = opts["canvas"]
	if isinstance(canvas, tuple):
		p["canvas"], p["canvas_w"], p["canvas_h"] = "fixed", int(canvas[0]), int(canvas[1])
	else:
		p["canvas"] = canvas
	bg = opts.get("background")
	p["background"] = "{0:02x}{1:02x}{2:02x}{3:02x}".format(*bg) if bg is not None else ""
	p["lcm"], p["fit"] = bool(opts["lcm"]), opts["fit"]
	return p


def preview_stack(params):
	"""Layers for the single preview: varying layers take the first matching file of their folder; visible layers
	without an image are left out."""
	layers, opts, vary = build_stack(params)
	for i, folder in vary.items():
		if layers[i].visible:
			files = batch.scan_folder(folder, _globs(params.get("include")), _globs(params.get("exclude"))) \
				if folder and os.path.isdir(folder) else []
			if not files:
				raise ValueError("layer {0}: no image in its folder {1}".format(i, folder or "(none)"))
			layers[i] = layers[i].with_image(files[0])
	layers = [l for l in layers if l.image is not None or not l.visible]
	return layers, opts


def make_jobs(params):
	layers, opts, vary = build_stack(params)
	return stack_jobs(layers, vary, opts, params.get("pattern") or "{name}.png", _globs(params.get("include")),
		_globs(params.get("exclude")))


def job_count(params):
	_, _, vary = build_stack(params)
	if not vary or any(not f or not os.path.isdir(f) for f in vary.values()):
		return 0
	return count_stack_jobs(vary, _globs(params.get("include")), _globs(params.get("exclude")))


def _flag_form(params):
	"""The two-layer combine_tex.py flags when the stack fits them, else None."""
	ls = params.get("layers") or []
	if len(ls) != 2:
		return None
	base, top = _layer(ls[0]), _layer(ls[1])
	plain = Layer(image=base.image, name=base.name)   # names are GUI labels; the CLI has none
	if not base.visible or not top.visible or ls[0].get("varies") or layer_to_dict(base) != layer_to_dict(plain):
		return None
	if top.scale is not None or top.size is not None or top.trim or top.mask_invert:
		return None
	if top.mask is not None and top.mode != "paint":
		return None
	if top.mode not in BLEND_MODES:
		return None
	return base, top


def cli_argv(params, python="python3"):
	"""Copy as CLI: combine_tex.py flags for a plain two-layer stack, else --stack with the stack JSON inline."""
	layers, opts, vary = build_stack(params)
	bulk = bool(vary)
	out = (params.get("out_dir") or "<out folder>") if bulk else (params.get("output") or "<output.png>")
	ff = _flag_form(params)
	bulk_flags = []
	if bulk:
		bulk_flags += ["--pattern", params.get("pattern") or "{name}.png"]
		if params.get("overwrite", "replace") != "replace":
			bulk_flags += ["--overwrite", params["overwrite"]]
		for g in _globs(params.get("include")) or []:
			bulk_flags += ["--include", g]
		for g in _globs(params.get("exclude")) or []:
			bulk_flags += ["--exclude", g]
	if params.get("dry_run"):
		bulk_flags.append("--dry-run")
	if ff is not None and (not bulk or set(vary) == {1}):
		base, top = ff
		c = opts["canvas"]
		argv = [python, "-m", CLI_MODULE, base.image or "<base>", top.image or "<overlay>", out, "--mode", top.mode,
			"--opacity", "{0:g}".format(top.opacity), "--offset", "{0},{1}".format(*top.offset),
			"--offset-mode", top.offset_mode, "--anchor", top.anchor,
			"--canvas", c if isinstance(c, str) else "{0}x{1}".format(*c), "--fit", opts["fit"]]
		if opts["background"] is not None:
			argv += ["--background", "{0:02x}{1:02x}{2:02x}{3:02x}".format(*opts["background"])]
		if opts["lcm"]:
			argv.append("--lcm")
		if top.mask is not None:
			argv += ["--mask", top.mask]
		if top.strength != 1.0:
			argv += ["--strength", "{0:g}".format(top.strength)]
		if top.inset:
			argv += ["--inset", str(int(top.inset))]
		if top.clip:
			argv.append("--clip")
		if top.legacy_blend and top.mode in ("add", "multiply"):
			argv.append("--legacy-blend")
		if bulk:
			argv.append("--batch")
		return argv + bulk_flags
	doc = json.dumps(stack_doc(params), sort_keys=True, separators=(",", ":"))
	return [python, "-m", CLI_MODULE, "--stack", doc, out] + bulk_flags


# ---------------------------------------------------------------- widgets

class LayerRow(BoxLayout):
	def __init__(self, screen, index, d, selected, **kw):
		super().__init__(size_hint_y=None, height=32, spacing=3, **kw)
		self.screen = screen
		eye = CheckBox(active=bool(d.get("visible", True)), size_hint_x=None, width=26)
		eye.bind(active=lambda inst, v: screen.set_layer_field(index, "visible", bool(v)))
		name = d.get("name") or "layer {0}".format(index)
		if d.get("varies"):
			name += " (varies)"
		sel = ToggleButton(text="{0}: {1}".format(index, name), state="down" if selected else "normal",
			group="tt-combine-layers", allow_no_selection=False, shorten=True)
		sel.bind(on_release=lambda *a: screen.select_layer(index))
		mode = Spinner(text=d.get("mode", "alpha"), values=list(BLEND_MODES), size_hint_x=None, width=86)
		mode.bind(text=lambda inst, v: screen.set_layer_field(index, "mode", v))
		self.add_widget(eye)
		self.add_widget(sel)
		self.add_widget(mode)
		for text, fn in (("up", lambda: screen.move_layer(index, 1)), ("dn", lambda: screen.move_layer(index, -1)),
						 ("x", lambda: screen.remove_layer(index))):
			b = Button(text=text, size_hint_x=None, width=32)
			b.bind(on_release=lambda inst, fn=fn: fn())
			self.add_widget(b)


@register_screen
class CombineScreen(BulkPreviewMixin, ToolScreen):
	tool_name = "combine"
	title = "Combine"
	order = 20

	def default_params(self):
		return default_params()

	# ---- layout ----

	def build_controls(self):
		self.sel = len(self.params["layers"]) - 1
		self._loading = False
		self._drag = None
		self.confirmed = None
		self.add_heading("Layers (top first)")
		self.layer_box = BoxLayout(orientation="vertical", size_hint_y=None, height=0, spacing=2)
		self.layer_box.bind(minimum_height=self.layer_box.setter("height"))
		self.controls.add_widget(self.layer_box)
		row = BoxLayout(size_hint_y=None, height=32, spacing=4)
		for text, fn in (("Add layer", self.add_layer), ("Duplicate", self.duplicate_layer)):
			b = Button(text=text)
			b.bind(on_release=lambda inst, fn=fn: fn())
			row.add_widget(b)
		self.controls.add_widget(row)

		self.layer_heading = self.add_heading("Selected layer", is_open=True)
		self.lc = {}
		self.image_slot = FileSlot(label="Image", kind="file")
		self.image_slot.bind(paths=lambda inst, p: self._layer_change("image", inst.path))
		self.controls.add_widget(self.image_slot)
		self._lctl(ToggleControl("varies", "Varies (bulk)", False, self._layer_change))
		self._lctl(NumberControl("opacity", "Opacity", 1.0, 0.0, 1.0, 0.05, self._layer_change))
		self._lctl(NumberControl("offset_x", "Offset X", 0, -256, 256, 1, self._layer_change))
		self._lctl(NumberControl("offset_y", "Offset Y", 0, -256, 256, 1, self._layer_change))
		arrows = BoxLayout(size_hint_y=None, height=32, spacing=4)
		arrows.add_widget(Label(text="Nudge", size_hint_x=None, width=110))
		for text, dx, dy in (("<", -1, 0), ("^", 0, -1), ("v", 0, 1), (">", 1, 0)):
			b = Button(text=text, size_hint_x=None, width=40)
			b.bind(on_release=lambda inst, dx=dx, dy=dy: self.nudge(dx, dy))
			arrows.add_widget(b)
		arrows.add_widget(Label())
		self.controls.add_widget(arrows)
		self.add_toggle("snap", "Snap to layer px")
		self._lctl(ChoiceControl("offset_mode", "Offset edge", "clip", list(OFFSET_MODES), self._layer_change))
		self._lctl(ChoiceControl("anchor", "Anchor", "top_left", list(ANCHORS), self._layer_change))
		self._lctl(ChoiceControl("resize", "Resize", "none", list(RESIZE), self._layer_change))
		self._lctl(NumberControl("scale", "Scale factor", 1, 1, 16, 1, self._layer_change))
		self._lctl(NumberControl("size_w", "Width", 16, 1, 1024, 1, self._layer_change))
		self._lctl(NumberControl("size_h", "Height", 16, 1, 1024, 1, self._layer_change))
		self._lctl(ToggleControl("trim", "Trim first", False, self._layer_change))
		self.mask_slot = FileSlot(label="Mask", kind="file")
		self.mask_slot.bind(paths=lambda inst, p: self._layer_change("mask", inst.path))
		self.controls.add_widget(self.mask_slot)
		self._lctl(ToggleControl("mask_invert", "Invert mask", False, self._layer_change))
		self._lctl(NumberControl("strength", "Paint strength", 1.0, 0.0, 1.0, 0.05, self._layer_change))
		self._lctl(NumberControl("inset", "Paint inset", 0, 0, 16, 1, self._layer_change))
		self._lctl(ToggleControl("clip", "Paint clip", False, self._layer_change))
		# add / multiply respect the overlay's alpha (owner ruling R8); this keeps the old blend
		self._lctl(ToggleControl("legacy_blend", "Ignore alpha (legacy)", False, self._layer_change))

		self.add_heading("Canvas")
		self.add_choice("canvas", "Canvas", ["base", "largest", "fixed"])
		self.add_number("canvas_w", "Canvas W", 1, 1024, 1)
		self.add_number("canvas_h", "Canvas H", 1, 1024, 1)
		self.add_text("background", "Background")
		self.controls.add_widget(Label(text="RRGGBB[AA] or r,g,b[,a]; empty = transparent", size_hint_y=None,
			height=20, font_size="11sp"))
		self.add_toggle("lcm", "LCM fit")
		self.add_choice("fit", "Size check", list(FIT_POLICIES))

		self.add_heading("Output")
		self.add_slot("output", "Output file", kind="save")
		self.add_slot("out_dir", "Output folder", kind="folder")
		self.add_text("pattern", "Name pattern")
		self.controls.add_widget(Label(text="fields {name} {name<slot>} {ext} {base} {n}", size_hint_y=None,
			height=20, font_size="11sp"))
		self.add_text("include", "Include globs")
		self.add_text("exclude", "Exclude globs")
		self.add_choice("overwrite", "Existing file", list(batch.OVERWRITE_POLICIES))
		self.add_toggle("dry_run", "Dry run")
		crow = BoxLayout(size_hint_y=None, height=32, spacing=4)
		self.count_label = Label(text="", halign="left", valign="middle")
		self.count_label.bind(size=lambda inst, *a: setattr(inst, "text_size", inst.size))
		self.confirm_button = Button(text="Confirm", size_hint_x=None, width=150, disabled=True)
		self.confirm_button.bind(on_release=lambda *a: self.confirm())
		crow.add_widget(self.count_label)
		crow.add_widget(self.confirm_button)
		self.controls.add_widget(crow)
		row = BoxLayout(size_hint_y=None, height=32, spacing=4)
		for text, fn in (("Export stack JSON", self.export_stack), ("Import stack JSON", self.import_stack)):
			b = Button(text=text)
			b.bind(on_release=lambda inst, fn=fn: fn())
			row.add_widget(b)
		self.controls.add_widget(row)

		self.preview.bind(on_pixel=self._on_preview_pixel)
		self.install_bulk_view()
		self.refresh_layers()

	def _lctl(self, ctl):
		self.lc[ctl.key] = ctl
		self.controls.add_widget(ctl)
		return ctl

	# ---- layer list ----

	def _layers(self):
		return [dict(d) for d in self.params.get("layers") or []]

	def _set_layers(self, layers):
		self.set_param("layers", layers)

	def refresh_layers(self):
		layers = self.params.get("layers") or []
		self.sel = max(0, min(self.sel, len(layers) - 1))
		self.layer_box.clear_widgets()
		for i in reversed(range(len(layers))):
			self.layer_box.add_widget(LayerRow(self, i, layers[i], i == self.sel))
		self._load_layer_panel()
		self._update_count()

	def _load_layer_panel(self):
		layers = self.params.get("layers") or []
		if not layers:
			return
		d = layers[self.sel]
		self._loading = True
		try:
			self.layer_heading.text = "[b]Selected layer {0}{1}[/b]".format(self.sel, " (base)" if self.sel == 0 else "")
			self.image_slot.kind = "folder" if d.get("varies") else "file"
			self.image_slot.label = "Folder" if d.get("varies") else "Image"
			self.image_slot._lbl.text = self.image_slot.label
			self.image_slot.set_paths([d["image"]] if d.get("image") else [])
			self.mask_slot.set_paths([d["mask"]] if d.get("mask") else [])
			off = d.get("offset") or [0, 0]
			size = d.get("size") or [16, 16]
			vals = {"varies": d.get("varies", False), "opacity": d.get("opacity", 1.0), "offset_x": off[0],
				"offset_y": off[1], "offset_mode": d.get("offset_mode", "clip"), "anchor": d.get("anchor", "top_left"),
				"resize": d.get("resize", "none"), "scale": d.get("scale") or 1, "size_w": size[0], "size_h": size[1],
				"trim": d.get("trim", False), "mask_invert": d.get("mask_invert", False),
				"strength": d.get("strength", 1.0), "inset": d.get("inset", 0), "clip": d.get("clip", False),
				"legacy_blend": d.get("legacy_blend", False)}
			for k, v in vals.items():
				self.lc[k].set_value(v)
			resize = vals["resize"]
			self.lc["scale"].disabled = resize != "scale"
			self.lc["size_w"].disabled = self.lc["size_h"].disabled = resize != "size"
			paint = d.get("mode") == "paint"
			for k in ("strength", "inset", "clip"):
				self.lc[k].disabled = not paint
			self.lc["legacy_blend"].disabled = d.get("mode") not in ("add", "multiply")
		finally:
			self._loading = False

	def select_layer(self, i):
		self.sel = i
		self.refresh_layers()

	def add_layer(self):
		layers = self._layers()
		layers.insert(self.sel + 1, new_layer("layer {0}".format(len(layers))))
		self.sel += 1
		self._set_layers(layers)
		self.refresh_layers()

	def duplicate_layer(self):
		layers = self._layers()
		if not layers:
			return
		d = json.loads(json.dumps(layers[self.sel]))
		d["name"] = (d.get("name") or "layer") + " copy"
		layers.insert(self.sel + 1, d)
		self.sel += 1
		self._set_layers(layers)
		self.refresh_layers()

	def remove_layer(self, i):
		layers = self._layers()
		if len(layers) <= 1:
			self.set_status("the stack keeps at least one layer")
			return
		del layers[i]
		if self.sel >= i and self.sel > 0:
			self.sel -= 1
		self._set_layers(layers)
		self.refresh_layers()

	def move_layer(self, i, delta):
		"""delta +1 = one step up (toward the top of the stack)."""
		layers = self._layers()
		j = i + delta
		if not 0 <= j < len(layers):
			return
		layers[i], layers[j] = layers[j], layers[i]
		if self.sel == i:
			self.sel = j
		elif self.sel == j:
			self.sel = i
		self._set_layers(layers)
		self.refresh_layers()

	def set_layer_field(self, i, key, value):
		layers = self._layers()
		if i >= len(layers) or layers[i].get(key) == value:
			return
		layers[i][key] = value
		self._set_layers(layers)
		if key in ("mode", "varies"):
			if i == self.sel:
				self._load_layer_panel()
			self._update_count()
		if key == "varies":
			self.refresh_layers()

	def _layer_change(self, key, value):
		if self._loading:
			return
		layers = self._layers()
		if not layers:
			return
		d = layers[self.sel]
		if key in ("offset_x", "offset_y"):
			off = list(d.get("offset") or [0, 0])
			off[0 if key == "offset_x" else 1] = int(value)
			d["offset"] = off
		elif key in ("size_w", "size_h"):
			size = list(d.get("size") or [16, 16])
			size[0 if key == "size_w" else 1] = int(value)
			d["size"] = size
		elif key == "scale":
			d["scale"] = int(value)
		elif key == "resize":
			d["resize"] = value
			d["scale"] = int(self.lc["scale"].get_value()) if value == "scale" else None
			d["size"] = [int(self.lc["size_w"].get_value()), int(self.lc["size_h"].get_value())] \
				if value == "size" else None
		elif key in ("image", "mask"):
			d[key] = value or None
		elif key in ("inset",):
			d[key] = int(value)
		else:
			d[key] = value
		if layers == self.params.get("layers"):
			return
		self._set_layers(layers)
		if key in ("resize", "varies"):
			self._load_layer_panel()
		if key in ("varies", "image"):
			self.refresh_layers() if key == "varies" else self._update_count()

	def _step(self):
		if not self.params.get("snap"):
			return 1
		d = (self.params.get("layers") or [{}])[self.sel]
		return int(d.get("scale") or 1) if d.get("resize") == "scale" else 1

	def nudge(self, dx, dy):
		layers = self._layers()
		d = layers[self.sel]
		s = self._step()
		off = list(d.get("offset") or [0, 0])
		d["offset"] = [int(off[0]) + dx * s, int(off[1]) + dy * s]
		self._set_layers(layers)
		self._load_layer_panel()

	def _on_preview_pixel(self, pv, col, row, button, kind):
		if self._grid_shown:
			return
		if kind == "down":
			d = (self.params.get("layers") or [{}])[self.sel]
			self._drag = (col, row, list(d.get("offset") or [0, 0]))
			return
		if self._drag is None:
			return
		c0, r0, off0 = self._drag
		s = self._step()
		dx, dy = (col - c0) // s * s, (row - r0) // s * s
		layers = self._layers()
		new = [off0[0] + dx, off0[1] + dy]
		if layers[self.sel].get("offset") != new:
			layers[self.sel]["offset"] = new
			self._set_layers(layers)
			self._load_layer_panel()
		if kind == "up":
			self._drag = None

	# ---- params ----

	def set_param(self, key, value):
		super().set_param(key, value)
		if key in ("include", "exclude", "layers"):
			self._update_count()
		self.note_params_changed()

	def update_params(self, values):
		super().update_params(values)
		self.refresh_layers()

	def _update_count(self):
		_, _, vary = build_stack(self.params)
		if not vary:
			self.count_label.text = "single output (no varying layer)"
			self.confirm_button.disabled = True
			self.confirm_button.text = "Confirm"
			self.preview_all_button.disabled = True
			return
		n = job_count(self.params)
		self.preview_all_button.disabled = n == 0
		if len(vary) > 1:
			sig = self._confirm_sig(n)
			ok = self.confirmed == sig
			self.count_label.text = "cartesian: {0} outputs from {1} varying layers{2}".format(
				n, len(vary), " (confirmed)" if ok else "")
			self.confirm_button.disabled = ok or n == 0
			self.confirm_button.text = "Confirm {0}".format(n)
		else:
			self.count_label.text = "bulk: {0} output(s)".format(n)
			self.confirm_button.disabled = True
			self.confirm_button.text = "Confirm"

	def _confirm_sig(self, n):
		_, _, vary = build_stack(self.params)
		return (n, tuple(sorted(vary.items())), self.params.get("include"), self.params.get("exclude"))

	def confirm(self):
		n = job_count(self.params)
		self.confirmed = self._confirm_sig(n)
		self._update_count()
		self.set_status("confirmed: Run writes {0} combinations".format(n))

	# ---- engine ----

	def validate(self, params):
		layers = params.get("layers") or []
		if not any(d.get("visible", True) and d.get("image") for d in layers):
			return "give a visible layer an image"
		bg = (params.get("background") or "").strip()
		if bg:
			try:
				parse_color(bg)
			except ValueError as e:
				return str(e)
		return None

	def validate_run(self, params):
		layers, opts, vary = build_stack(params)
		for i, l in enumerate(layers):
			if l.visible and l.image is None:
				return "layer {0} has no image (hide or remove it)".format(i)
		if vary:
			if not params.get("out_dir"):
				return "pick an output folder"
			n = job_count(params)
			if n == 0:
				return "no input files in the varying folders"
			if len(vary) > 1 and self.confirmed != self._confirm_sig(n):
				return "cartesian run makes {0} outputs: press Confirm {0} first".format(n)
			return None
		return None if params.get("output") else "pick an output file"

	def compute_preview(self, params):
		layers, opts = preview_stack(params)
		arr, layout = combine_stack(layers, return_layout=True, **opts)
		w, h = layout["size"]
		status = "canvas {0}x{1}".format(w, h)
		if layout.get("lcm"):
			status += ", LCM {0}".format(layout["lcm"])
		return {"preview": arr, "status": status}

	def bulk_jobs(self, params):
		return make_jobs(params)

	def run_job(self, params, progress):
		layers, opts, vary = build_stack(params)
		if vary:
			jobs = self.filter_skipped(make_jobs(params), params)
			report = batch.run_batch(jobs, params["out_dir"], dry_run=bool(params.get("dry_run")),
				overwrite=params.get("overwrite") or "replace",
				progress=lambda d, t, r: progress(d / float(t), r.job.label))
			c = report.counts()
			n = c.get("written", 0) + c.get("dry", 0)
			return {"outputs": [r.output for r in report.written], "status": "{0} {1} file(s), {2} skipped".format(
				"would write" if params.get("dry_run") else "wrote", n, len(report.skipped) + len(report.errors))}
		arr = combine_stack(layers, **opts)
		if params.get("dry_run"):
			return {"outputs": [], "status": "would write {0}".format(params["output"])}
		progress(1.0, "saving")
		save_raw(arr, params["output"])
		return [params["output"]]

	def cli_argv(self, params):
		return cli_argv(params)

	def provenance_inputs(self, params):
		out = []
		for d in params.get("layers") or []:
			if d.get("visible", True):
				out.extend(p for p in (d.get("image"), d.get("mask")) if p)
		return out

	# ---- stack JSON presets ----

	def export_stack(self):
		doc = stack_doc(self.params)

		def write(paths):
			path = paths[0] if paths[0].lower().endswith(".json") else paths[0] + ".json"
			with open(path, "w", encoding="utf-8") as f:
				json.dump(doc, f, indent="\t", sort_keys=True)
				f.write("\n")
			self.set_status("stack saved: {0} (combine_tex.py --stack)".format(path))

		open_browser("save", write, title="Export stack JSON", filters=("*.json",), default_name="stack.json")

	def import_stack(self):
		def read(paths):
			try:
				with open(paths[0], encoding="utf-8") as f:
					doc = json.load(f)
				p = params_from_stack_doc(doc, os.path.dirname(os.path.abspath(paths[0])), self.params)
			except (OSError, ValueError, TypeError, KeyError) as e:
				self.set_status("stack not loaded: {0}".format(e))
				return
			self.sel = len(p["layers"]) - 1
			self.update_params(p)
			self.set_status("stack loaded: {0}".format(paths[0]))

		open_browser("file", read, title="Import stack JSON", filters=("*.json",))

	def shutdown(self):
		super().shutdown()
		self.shutdown_bulk()

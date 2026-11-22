# SPDX-License-Identifier: GPL-3.0-only
"""Palette Swap screen (plan_texture_tools_gui.md section 3, P6).

Inputs: a source image and a palette (an image or a hand-built swatch strip). Bulk: one fixed image plus a folder;
by default the source is fixed and every image in the folder supplies a palette (the legacy --bulk); "Reverse roles"
fixes the palette and recolors every image in the folder. Every mapping control is a SwapOptions field
(core/swap.py); options_from_params() is the one translation, used by the preview, Run, bulk jobs and Copy as CLI.
Thumbnails of the source and palette sit over the preview: a left click on the palette thumbnail toggles that color
on the exclusion list (eyedropper), on the source thumbnail on the keep list.
"""
import os

from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label

from ...core import batch
from ...core.palette import extract_palette, get_colors_by_brightness
from ...core.swap import (ALPHA_SOURCES, DITHERS, INTERPOLATE, MODES, SPACES, SwapOptions, colors_spec,
						  load_palette_source, load_rgba_u8, save_swap, swap_image, swap_jobs)
from ..registry import register_screen
from ..widgets.previewgrid import TilePreview
from ..widgets.swatches import SwatchList, SwatchStrip
from .base import ToolScreen
from .bulkui import BulkPreviewMixin

CLI_MODULE = "texturetools.cli.palette_swap"
OPTION_KEYS = tuple(k for k in SwapOptions().to_dict() if k != "drops")
DROP_KEYS = (("drop_brown", "brown"), ("drop_white", "white"))


def default_params():
	p = SwapOptions().to_dict()
	p.pop("drops")
	p.update({
		"source": "", "palette": "", "palette_from": "file", "swatches": [],
		"drop_brown": False, "drop_white": False,
		"output": "",
		"bulk": False, "reverse_roles": False, "folder": "", "out_dir": "", "pattern": "{name}.png",
		"include": "", "exclude": "", "overwrite": "replace", "dry_run": False,
	})
	return p


def options_from_params(params):
	"""The SwapOptions a params dict means (the only GUI -> engine translation)."""
	d = {k: params[k] for k in OPTION_KEYS if k in params}
	d["drops"] = [name for key, name in DROP_KEYS if params.get(key)]
	for k in ("drop_darkest", "drop_lightest", "palette_alpha", "palette_exclude_tol", "source_keep_tol"):
		if k in d:
			d[k] = int(d[k])
	for k in ("interp_amount", "hue_shift", "sat_shift", "val_shift", "preserve"):
		if k in d:
			d[k] = float(d[k])
	return SwapOptions.from_dict(d)


def _globs(text):
	return [g.strip() for g in (text or "").split(",") if g.strip()]


def palette_spec(params):
	"""The fixed palette: a path, or a colors: spec for the swatch strip."""
	if params.get("palette_from") == "swatches":
		return colors_spec(params.get("swatches") or []) if params.get("swatches") else ""
	return params.get("palette") or ""


def bulk_fixed(params):
	return palette_spec(params) if params.get("reverse_roles") else params.get("source") or ""


def make_jobs(params):
	return swap_jobs(bulk_fixed(params), params["folder"], bool(params.get("reverse_roles")),
		options_from_params(params), params.get("pattern") or "{name}.png", _globs(params.get("include")) or None,
		_globs(params.get("exclude")) or None)


def preview_inputs(params):
	"""(source spec, palette spec) of the single preview: the pair, or the first bulk job's pair."""
	if params.get("bulk") and params.get("folder") and os.path.isdir(params["folder"]):
		jobs = make_jobs(params)
		if jobs:
			return jobs[0].inputs["source"], jobs[0].inputs["palette"]
		raise ValueError("no image in the bulk folder matches")
	return params.get("source") or "", palette_spec(params)


def cli_argv(params, python="python3"):
	"""Copy as CLI: palette_swap.py single form, or --bulk with the P6 batch flags."""
	opts = options_from_params(params).cli_args()
	argv = [python, "-m", CLI_MODULE]
	if params.get("bulk"):
		argv += [bulk_fixed(params) or "<fixed image>", "--bulk", "--in-dir", params.get("folder") or "<folder>",
			"--out-dir", params.get("out_dir") or "<out folder>", "--pattern", params.get("pattern") or "{name}.png"]
		if params.get("reverse_roles"):
			argv.append("--reverse")
		if params.get("overwrite", "replace") != "replace":
			argv += ["--overwrite", params["overwrite"]]
		for g in _globs(params.get("include")):
			argv += ["--include", g]
		for g in _globs(params.get("exclude")):
			argv += ["--exclude", g]
		if params.get("dry_run"):
			argv.append("--dry-run")
		return argv + opts
	return argv + [params.get("source") or "<source>", palette_spec(params) or "<palette>",
		params.get("output") or "<output.png>"] + opts


@register_screen
class SwapScreen(BulkPreviewMixin, ToolScreen):
	tool_name = "swap"
	title = "Palette Swap"
	order = 10

	def default_params(self):
		return default_params()

	# ---- layout ----

	def build_controls(self):
		self._thumb_src = None
		self._thumb_pal = None
		self._shown_sig = None
		self.add_heading("Inputs")
		self.add_slot("source", "Source", kind="file")
		self.add_choice("palette_from", "Palette from", ["file", "swatches"])
		self.add_slot("palette", "Palette", kind="file")
		self.strip = SwatchStrip(on_change=lambda cols: self.set_param("swatches", list(cols)))
		self.strip.set_colors(self.params.get("swatches"))
		self.controls.add_widget(self.strip)
		seed = Button(text="Strip from palette colors", size_hint_y=None, height=30)
		seed.bind(on_release=lambda *a: self.seed_strip())
		self.controls.add_widget(seed)

		self.add_heading("Mapping")
		self.add_choice("mode", "Mode", list(MODES))
		self.add_choice("interpolate", "Interpolate", list(INTERPOLATE))
		self.add_number("interp_amount", "Amount", 0.0, 1.0, 0.05)
		self.add_choice("interp_space", "Blend space", list(SPACES))
		self.add_toggle("invert", "Invert")
		self.add_choice("dither", "Dither", list(DITHERS))
		self.add_choice("alpha", "Alpha from", list(ALPHA_SOURCES))

		self.add_heading("Palette")
		self.add_number("palette_alpha", "Min alpha", 1, 255, 1)
		self.add_toggle("drop_brown", "Drop browns")
		self.add_toggle("drop_white", "Drop whites")
		self.add_number("drop_darkest", "Drop darkest", 0, 16, 1)
		self.add_number("drop_lightest", "Drop lightest", 0, 16, 1)
		self.add_number("hue_shift", "Hue shift", -180.0, 180.0, 1.0)
		self.add_number("sat_shift", "Sat shift", -1.0, 1.0, 0.05)
		self.add_number("val_shift", "Value shift", -1.0, 1.0, 0.05)
		self.add_number("palette_exclude_tol", "Exclude tol", 0, 64, 1)
		self.exclude_list = SwatchList("Palette exclusions (tick, or click the palette thumbnail)",
			on_change=lambda cols: self.set_param("palette_exclude", list(cols)))
		self.controls.add_widget(self.exclude_list)

		self.add_heading("Source")
		self.add_number("preserve", "Preserve source", 0.0, 1.0, 0.01)
		self.add_toggle("stretch", "Contrast stretch")
		self.add_number("source_keep_tol", "Keep tol", 0, 64, 1)
		self.keep_list = SwatchList("Source keep list (tick, or click the source thumbnail)",
			on_change=lambda cols: self.set_param("source_keep", list(cols)))
		self.controls.add_widget(self.keep_list)

		self.add_heading("Output")
		self.add_slot("output", "Output file", kind="save")
		self.add_toggle("bulk", "Bulk")
		self.add_toggle("reverse_roles", "Reverse roles")
		self.folder_slot = self.add_slot("folder", "Palettes folder", kind="folder")
		self.add_slot("out_dir", "Output folder", kind="folder")
		self.add_text("pattern", "Name pattern")
		hint = Label(text="fields {name} {shape} {palette} {ext} {n}", size_hint_y=None, height=20,
			font_size="11sp")
		self.controls.add_widget(hint)
		self.add_text("include", "Include globs")
		self.add_text("exclude", "Exclude globs")
		self.add_choice("overwrite", "Existing file", list(batch.OVERWRITE_POLICIES))
		self.add_toggle("dry_run", "Dry run")

		self._install_thumbs()
		self.install_bulk_view()
		self._sync_widgets()

	def _install_thumbs(self):
		row = BoxLayout(size_hint_y=None, height=150, spacing=6)
		self.thumb_src = TilePreview(pick_buttons=["left"])
		self.thumb_pal = TilePreview(pick_buttons=["left"])
		for title, pv, fn in (("Source: click = keep color", self.thumb_src, self._pick_src),
							  ("Palette: click = exclude color", self.thumb_pal, self._pick_pal)):
			col = BoxLayout(orientation="vertical")
			col.add_widget(Label(text=title, size_hint_y=None, height=20, font_size="12sp"))
			pv.bind(on_pixel=lambda inst, c, r, b, kind, fn=fn: kind == "down" and fn(inst, c, r))
			pv.bind(size=lambda inst, *a: inst.fit())
			col.add_widget(pv)
			row.add_widget(col)
		self.thumbs = row
		self.preview_area.add_widget(row, index=len(self.preview_area.children))

	def _pick(self, pv, col, row):
		if pv.array is None:
			return None
		px = pv.array[row, col]
		return None if px[3] == 0 else tuple(int(v) for v in px[:3])

	def _pick_pal(self, pv, col, row):
		c = self._pick(pv, col, row)
		if c is not None:
			self.exclude_list.toggle(c)

	def _pick_src(self, pv, col, row):
		c = self._pick(pv, col, row)
		if c is not None:
			self.keep_list.toggle(c)

	def seed_strip(self):
		cols = [c for c in self.exclude_list.colors if c not in set(self.exclude_list.checked())]
		if not cols:
			self.set_status("no palette colors to copy (pick a palette image first)")
			return
		self.strip.set_colors(cols)
		self.update_params({"swatches": list(self.strip.colors), "palette_from": "swatches"})

	def _sync_widgets(self):
		p = self.params
		swatches = p.get("palette_from") == "swatches"
		self.slots["palette"].disabled = swatches
		self.strip.disabled = not swatches
		bulk = bool(p.get("bulk"))
		self.folder_slot.label = "Sources folder" if p.get("reverse_roles") else "Palettes folder"
		self.folder_slot._lbl.text = self.folder_slot.label
		for key in ("folder", "out_dir"):
			self.slots[key].disabled = not bulk
		self.slots["output"].disabled = bulk
		self.slots["source"].disabled = bulk and bool(p.get("reverse_roles"))
		if not swatches:
			self.slots["palette"].disabled = bulk and not p.get("reverse_roles")
		for key in ("reverse_roles", "pattern", "include", "exclude", "overwrite", "dry_run"):
			self.param_controls[key].disabled = not bulk
		self.preview_all_button.disabled = not bulk
		self.exclude_list.set_checked(p.get("palette_exclude"))
		self.keep_list.set_checked(p.get("source_keep"))
		if list(self.strip.colors) != list(p.get("swatches") or []):
			self.strip.set_colors(p.get("swatches"))

	def set_param(self, key, value):
		super().set_param(key, value)
		if key in ("palette_from", "bulk", "reverse_roles"):
			self._sync_widgets()
		self.note_params_changed()

	def update_params(self, values):
		super().update_params(values)
		self._sync_widgets()

	# ---- engine ----

	def validate(self, params):
		bulk = params.get("bulk")
		rev = params.get("reverse_roles")
		if bulk:
			if not params.get("folder"):
				return "pick the bulk folder ({0})".format("sources" if rev else "palettes")
			if rev and not palette_spec(params):
				return "pick a palette (file or swatches)"
			if not rev and not params.get("source"):
				return "pick a source (the fixed shape)"
			return None
		if not params.get("source"):
			return "pick a source"
		if not palette_spec(params):
			return "pick a palette (file or swatches)"
		return None

	def validate_run(self, params):
		if params.get("bulk"):
			return None if params.get("out_dir") else "pick an output folder"
		return None if params.get("output") else "pick an output file"

	def compute_preview(self, params):
		opts = options_from_params(params)
		src_spec, pal_spec = preview_inputs(params)
		src = load_rgba_u8(src_spec)
		pal = load_palette_source(pal_spec)
		out = {"src": src, "pal": pal, "sig": (src_spec, pal_spec, opts.palette_alpha)}
		out["pal_colors"] = list(extract_palette(pal, alpha_min=opts.palette_alpha)[0])   # darkest first
		out["src_colors"] = list(reversed(get_colors_by_brightness(src)))
		try:
			out["preview"] = swap_image(src, pal, opts)
			label = os.path.basename(pal_spec) if not pal_spec.startswith("colors:") else "swatches"
			out["status"] = "{0}x{1}, {2} source colors, palette {3} ({4} colors)".format(
				src.shape[1], src.shape[0], len(out["src_colors"]), label, len(out["pal_colors"]))
			out["name"] = "{0}.png".format(os.path.splitext(os.path.basename(src_spec))[0])
		except ValueError as e:
			out["preview"] = None
			out["status"] = "cannot swap: {0}".format(e)
		return out

	def show_preview(self, result):
		super().show_preview(result)
		if result.get("preview") is not None:
			self.result_name = result.get("name", "")
		if result.get("sig") != self._shown_sig:
			self._shown_sig = result.get("sig")
			self.thumb_src.set_array(result["src"], fit=True)
			self.thumb_pal.set_array(result["pal"], fit=True)
			self.exclude_list.set_colors(result["pal_colors"])
			self.keep_list.set_colors(result["src_colors"])
			self.exclude_list.set_checked(self.params.get("palette_exclude"))
			self.keep_list.set_checked(self.params.get("source_keep"))

	def bulk_jobs(self, params):
		return make_jobs(params)

	def run_job(self, params, progress):
		if params.get("bulk"):
			jobs = self.filter_skipped(make_jobs(params), params)
			report = batch.run_batch(jobs, params["out_dir"], dry_run=bool(params.get("dry_run")),
				overwrite=params.get("overwrite") or "replace", saver=save_swap,
				progress=lambda d, t, r: progress(d / float(t), r.job.label))
			c = report.counts()
			n = c.get("written", 0) + c.get("dry", 0)
			return {"outputs": [r.output for r in report.written], "status": "{0} {1} file(s), {2} skipped{3}".format(
				"would write" if params.get("dry_run") else "wrote", n, len(report.skipped) + len(report.errors),
				", left out {0} unticked".format(len(params.get("_skip") or ())) if params.get("_skip") else "")}
		src_spec, pal_spec = params["source"], palette_spec(params)
		arr = swap_image(load_rgba_u8(src_spec), load_palette_source(pal_spec), options_from_params(params))
		progress(1.0, "saving")
		save_swap(arr, params["output"])
		return [params["output"]]

	def cli_argv(self, params):
		return cli_argv(params)

	def provenance_inputs(self, params):
		if params.get("bulk"):
			return [bulk_fixed(params), params.get("folder") or ""]
		return [params.get("source") or "", palette_spec(params)]

	def shutdown(self):
		super().shutdown()
		self.shutdown_bulk()

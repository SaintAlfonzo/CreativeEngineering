# SPDX-License-Identifier: GPL-3.0-only
"""Texture Painter screen (plan_texture_tools_gui.md section 5, P8).

Inputs: a shape (base) and a material; an optional mask (default: the shape's alpha) with invert. Every PaintOptions
field (core/paint.py) is a control; options_from_params() is the one translation, used by the preview, Run, bulk jobs
and Copy as CLI (cli/paint_tex.py). Thumbnails of the shape and the material sit over the preview: a left click on the
shape thumbnail toggles that color on the keep list or the paint-only list ("Eyedropper adds to"). Bulk: one shape
and every material in a folder, or with "Reverse roles" one material and every shape in a folder (batch kind
"paint"). Open in Editor (base action bar) hands the result to the Texture Editor.
"""
import os

from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label

from ...cli.paint_tex import cli_args
from ...core import batch
from ...core.image_io import load_rgba_array, save_raw
from ...core.paint import FITS, MATERIAL_ALPHA, OUTLINES, PAINT_MODES, ROTATIONS, PaintOptions, paint, paint_jobs
from ...core.palette import get_colors_by_brightness
from ..registry import register_screen
from ..widgets.previewgrid import TilePreview
from ..widgets.swatches import SwatchList
from .base import ToolScreen
from .bulkui import BulkPreviewMixin

CLI_MODULE = "texturetools.cli.paint_tex"
OPTION_KEYS = tuple(PaintOptions().to_dict())
INT_KEYS = ("offset_x", "offset_y", "scale", "rotate", "inset", "only_tol", "keep_tol")
PICK_TARGETS = ("keep", "only")


def default_params():
	p = PaintOptions().to_dict()
	p.update({
		"shape": "", "material": "", "mask": "", "output": "", "pick_target": "keep",
		"bulk": False, "reverse_roles": False, "folder": "", "out_dir": "", "pattern": "{name}.png",
		"include": "", "exclude": "", "recursive": False, "overwrite": "replace", "dry_run": False,
	})
	return p


def options_from_params(params):
	"""The PaintOptions a params dict means (the only GUI -> engine translation)."""
	d = {k: params[k] for k in OPTION_KEYS if k in params}
	for k in INT_KEYS:
		if k in d:
			d[k] = int(float(d[k]))
	if "strength" in d:
		d["strength"] = float(d["strength"])
	for k in ("only_colors", "keep_colors"):
		d[k] = list(d.get(k) or [])
	return PaintOptions.from_dict(d)


def _globs(text):
	return [g.strip() for g in (text or "").split(",") if g.strip()]


def bulk_fixed(params):
	return (params.get("material") if params.get("reverse_roles") else params.get("shape")) or ""


def make_jobs(params):
	return paint_jobs(bulk_fixed(params), params["folder"], bool(params.get("reverse_roles")),
		options_from_params(params), params.get("mask") or None, params.get("pattern") or "{name}.png",
		_globs(params.get("include")) or None, _globs(params.get("exclude")) or None, bool(params.get("recursive")))


def preview_inputs(params):
	"""(shape path, material path) of the single preview: the pair, or the first bulk job's pair."""
	if params.get("bulk") and params.get("folder") and os.path.isdir(params["folder"]):
		jobs = make_jobs(params)
		if jobs:
			return jobs[0].inputs["shape"], jobs[0].inputs["material"]
		raise ValueError("no image in the bulk folder matches")
	return params.get("shape") or "", params.get("material") or ""


def cli_argv(params, python="python3"):
	"""Copy as CLI: paint_tex.py single form, or --bulk with the batch flags."""
	opts = cli_args(options_from_params(params))
	if params.get("mask"):
		opts += ["--mask", params["mask"]]
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
		if params.get("recursive"):
			argv.append("--recursive")
		if params.get("dry_run"):
			argv.append("--dry-run")
		return argv + opts
	return argv + [params.get("shape") or "<shape>", params.get("material") or "<material>",
		params.get("output") or "<output.png>"] + opts


@register_screen
class PaintScreen(BulkPreviewMixin, ToolScreen):
	tool_name = "paint"
	title = "Painter"
	order = 30

	def default_params(self):
		return default_params()

	# ---- layout ----

	def build_controls(self):
		self._shown_sig = None
		self.add_heading("Inputs")
		self.add_slot("shape", "Shape", kind="file")
		self.add_slot("material", "Material", kind="file")
		self.add_slot("mask", "Mask (blank = shape alpha)", kind="file")
		self.add_toggle("mask_invert", "Invert mask")

		self.add_heading("Material placement")
		self.add_choice("mode", "Mode", list(PAINT_MODES))
		self.add_choice("fit", "Projected fit", list(FITS))
		self.add_number("offset_x", "Offset X", -64, 64, 1)
		self.add_number("offset_y", "Offset Y", -64, 64, 1)
		self.add_number("scale", "Scale", 1, 16, 1)
		self.add_toggle("tile", "Tile")
		self.add_toggle("flip_h", "Flip H")
		self.add_toggle("flip_v", "Flip V")
		self.add_choice("rotate", "Rotate", [str(r) for r in ROTATIONS])
		self.add_number("inset", "Inset", 0, 16, 1)

		self.add_heading("Shading")
		self.add_number("strength", "Strength", 0.0, 1.0, 0.01)
		self.add_choice("outline", "Outline", list(OUTLINES))
		self.add_choice("material_alpha", "Material alpha", list(MATERIAL_ALPHA))
		self.add_toggle("clip", "Clip unpainted")

		self.add_heading("Shape colors")
		self.add_choice("pick_target", "Eyedropper adds to", list(PICK_TARGETS))
		self.add_number("keep_tol", "Keep tol", 0, 64, 1)
		self.keep_list = SwatchList("Keep (never painted; tick, or click the shape thumbnail)",
			on_change=lambda cols: self.set_param("keep_colors", list(cols)))
		self.controls.add_widget(self.keep_list)
		self.add_number("only_tol", "Only tol", 0, 64, 1)
		self.only_list = SwatchList("Paint only these (none ticked = every color)",
			on_change=lambda cols: self.set_param("only_colors", list(cols)))
		self.controls.add_widget(self.only_list)

		self.add_heading("Output")
		self.add_slot("output", "Output file", kind="save")
		self.add_toggle("bulk", "Bulk")
		self.add_toggle("reverse_roles", "Reverse roles")
		self.folder_slot = self.add_slot("folder", "Materials folder", kind="folder")
		self.add_slot("out_dir", "Output folder", kind="folder")
		self.add_text("pattern", "Name pattern")
		hint = Label(text="fields {name} {shape} {material} {ext} {n}", size_hint_y=None, height=20,
			font_size="11sp")
		self.controls.add_widget(hint)
		self.add_text("include", "Include globs")
		self.add_text("exclude", "Exclude globs")
		self.add_toggle("recursive", "Recursive")
		self.add_choice("overwrite", "Existing file", list(batch.OVERWRITE_POLICIES))
		self.add_toggle("dry_run", "Dry run")

		self._install_thumbs()
		self.install_bulk_view()
		self._sync_widgets()

	def _install_thumbs(self):
		row = BoxLayout(size_hint_y=None, height=150, spacing=6)
		self.thumb_shape = TilePreview(pick_buttons=["left"])
		self.thumb_mat = TilePreview()
		for title, pv in (("Shape: click = keep / only color", self.thumb_shape), ("Material", self.thumb_mat)):
			col = BoxLayout(orientation="vertical")
			col.add_widget(Label(text=title, size_hint_y=None, height=20, font_size="12sp"))
			pv.bind(size=lambda inst, *a: inst.fit())
			col.add_widget(pv)
			row.add_widget(col)
		self.thumb_shape.bind(on_pixel=lambda inst, c, r, b, kind: kind == "down" and self.pick_shape(c, r))
		self.thumbs = row
		self.preview_area.add_widget(row, index=len(self.preview_area.children))

	def pick_shape(self, col, row):
		"""Eyedropper on the shape thumbnail: toggle the color on the list "Eyedropper adds to" names."""
		arr = self.thumb_shape.array
		if arr is None:
			return None
		px = arr[row, col]
		if px[3] == 0:
			return None
		c = tuple(int(v) for v in px[:3])
		(self.only_list if self.params.get("pick_target") == "only" else self.keep_list).toggle(c)
		return c

	def _sync_widgets(self):
		p = self.params
		bulk = bool(p.get("bulk"))
		rev = bool(p.get("reverse_roles"))
		self.folder_slot.label = "Shapes folder" if rev else "Materials folder"
		self.folder_slot._lbl.text = self.folder_slot.label
		for key in ("folder", "out_dir"):
			self.slots[key].disabled = not bulk
		self.slots["output"].disabled = bulk
		self.slots["shape"].disabled = bulk and rev
		self.slots["material"].disabled = bulk and not rev
		for key in ("reverse_roles", "pattern", "include", "exclude", "recursive", "overwrite", "dry_run"):
			self.param_controls[key].disabled = not bulk
		projected = p.get("mode") == "projected"
		self.param_controls["fit"].disabled = not projected
		for key in ("scale", "tile"):
			self.param_controls[key].disabled = projected
		self.preview_all_button.disabled = not bulk
		self.keep_list.set_checked(p.get("keep_colors"))
		self.only_list.set_checked(p.get("only_colors"))

	def set_param(self, key, value):
		super().set_param(key, value)
		if key in ("bulk", "reverse_roles", "mode"):
			self._sync_widgets()
		self.note_params_changed()

	def update_params(self, values):
		super().update_params(values)
		self._sync_widgets()

	# ---- engine ----

	def validate(self, params):
		if params.get("bulk"):
			rev = params.get("reverse_roles")
			if not params.get("folder"):
				return "pick the bulk folder ({0})".format("shapes" if rev else "materials")
			if rev and not params.get("material"):
				return "pick a material (the fixed one)"
			if not rev and not params.get("shape"):
				return "pick a shape (the fixed one)"
			return None
		if not params.get("shape"):
			return "pick a shape"
		if not params.get("material"):
			return "pick a material"
		return None

	def validate_run(self, params):
		if params.get("bulk"):
			return None if params.get("out_dir") else "pick an output folder"
		return None if params.get("output") else "pick an output file"

	def compute_preview(self, params):
		opts = options_from_params(params)
		shape_path, mat_path = preview_inputs(params)
		shape = load_rgba_array(shape_path)
		mat = load_rgba_array(mat_path)
		mask = load_rgba_array(params["mask"]) if params.get("mask") else None
		out = {"shape": shape, "material": mat, "sig": (shape_path, mat_path)}
		out["shape_colors"] = list(reversed(get_colors_by_brightness(shape.astype("uint8"))))
		try:
			out["preview"] = paint(shape, mat, opts, mask)
			out["status"] = "{0}x{1} shape, material {2} ({3}x{4}), {5}".format(shape.shape[1], shape.shape[0],
				os.path.basename(mat_path), mat.shape[1], mat.shape[0], opts.mode)
			out["name"] = "{0}.png".format(os.path.splitext(os.path.basename(shape_path))[0])
		except ValueError as e:
			out["preview"] = None
			out["status"] = "cannot paint: {0}".format(e)
		return out

	def show_preview(self, result):
		super().show_preview(result)
		if result.get("preview") is not None:
			self.result_name = result.get("name", "")
		if result.get("sig") != self._shown_sig:
			self._shown_sig = result.get("sig")
			self.thumb_shape.set_array(result["shape"], fit=True)
			self.thumb_mat.set_array(result["material"], fit=True)
			self.keep_list.set_colors(result["shape_colors"])
			self.only_list.set_colors(result["shape_colors"])
			self.keep_list.set_checked(self.params.get("keep_colors"))
			self.only_list.set_checked(self.params.get("only_colors"))

	def bulk_jobs(self, params):
		return make_jobs(params)

	def run_job(self, params, progress):
		if params.get("bulk"):
			jobs = self.filter_skipped(make_jobs(params), params)
			report = batch.run_batch(jobs, params["out_dir"], dry_run=bool(params.get("dry_run")),
				overwrite=params.get("overwrite") or "replace",
				progress=lambda d, t, r: progress(d / float(t), r.job.label))
			c = report.counts()
			n = c.get("written", 0) + c.get("dry", 0)
			return {"outputs": [r.output for r in report.written], "status": "{0} {1} file(s), {2} skipped{3}".format(
				"would write" if params.get("dry_run") else "wrote", n, len(report.skipped) + len(report.errors),
				", left out {0} unticked".format(len(params.get("_skip") or ())) if params.get("_skip") else "")}
		mask = load_rgba_array(params["mask"]) if params.get("mask") else None
		arr = paint(load_rgba_array(params["shape"]), load_rgba_array(params["material"]),
			options_from_params(params), mask)
		progress(1.0, "saving")
		save_raw(arr, params["output"])
		return [params["output"]]

	def cli_argv(self, params):
		return cli_argv(params)

	def provenance_inputs(self, params):
		extra = [params["mask"]] if params.get("mask") else []
		if params.get("bulk"):
			return [bulk_fixed(params), params.get("folder") or ""] + extra
		return [params.get("shape") or "", params.get("material") or ""] + extra

	def shutdown(self):
		super().shutdown()
		self.shutdown_bulk()

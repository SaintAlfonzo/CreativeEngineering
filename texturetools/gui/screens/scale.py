# SPDX-License-Identifier: GPL-3.0-only
"""Scale screen (plan_texture_tools_gui.md section 7, P8): scale_tex.py and upscale_sprites.py in one screen.

Single image (scale_tex.py): a whole-number factor, a target size (S or WxH; must be the same whole multiple on both
sides) or "fit canvas" (the largest factor that fits); trim first; a transparent canvas with an anchor. The preview and
Run both call core/scale.py scale_pipeline, the CLI's own path, and Run saves as the CLI does (the image mode kept, no
metadata). A blank output writes <stem>_<size>.png next to the input, as the CLI.
Folder (upscale_sprites.py): every image in a folder (recursive optional) at each factor, into x<N>/ subfolders or
flat with a _x<N> suffix; Preview all shows every output (nothing written), unticked tiles are left out of Run.
The Texture Editor's "Scale screen" button hands its image here (load_array); Open in Editor hands the result back.
Copy as CLI gives the scale_tex.py or upscale_sprites.py command with its exact flags.
"""
import os
from pathlib import Path

from PIL import Image

from ...core.scale import (default_output, parse_size, pil_to_rgba, scale_pipeline, upscale_factors, upscale_image,
						   upscale_jobs, upscale_output_dir, upscale_pil, upscale_plan)
from ..registry import register_screen
from .base import ToolScreen
from .bulkui import BulkPreviewMixin

MODES = ("single", "folder")
BY = ("factor", "size", "fit canvas")
ANCHORS = ("topleft", "center")
MEMORY = "<editor image>"


def default_params():
	return {
		"mode": "single", "source": "", "output": "", "by": "factor", "factor": 2, "size": "", "trim": False,
		"canvas": "", "anchor": "topleft",
		"folder": "", "out_dir": "", "factors": "2 4 8 16", "recursive": False, "flat": False,
	}


def parse_factors(text):
	"""'2 4, 8' -> [2, 4, 8] as given (upscale_sprites.py filters and sorts them)."""
	out = []
	for part in str(text or "").replace(",", " ").split():
		out.append(int(part))
	return out


def single_args(params):
	"""(factor, size, canvas) for scale_pipeline from params; raises ValueError on bad text."""
	by = params.get("by") or "factor"
	canvas = parse_size(params["canvas"]) if (params.get("canvas") or "").strip() else None
	factor = size = None
	if by == "factor":
		factor = int(params.get("factor") or 0)
	elif by == "size":
		if not (params.get("size") or "").strip():
			raise ValueError("give a target size (S or WxH)")
		size = parse_size(params["size"])
	elif canvas is None:
		raise ValueError("fit canvas needs a canvas size")
	return factor, size, canvas


def folder_plan(params):
	"""(output_dir, factors, plan) of a folder run, exactly as upscale_sprites.py resolves them."""
	in_dir = Path(params["folder"])
	out_dir = upscale_output_dir(in_dir, params.get("out_dir") or None)
	factors = upscale_factors(parse_factors(params.get("factors")))
	return out_dir, factors, upscale_plan(in_dir, out_dir, factors, bool(params.get("recursive")),
		bool(params.get("flat")))


def cli_argv(params, python="python3"):
	"""Copy as CLI: scale_tex.py (single) or upscale_sprites.py (folder), with their exact flags."""
	if params.get("mode") == "folder":
		argv = [python, "-m", "texturetools.cli.upscale_sprites", params.get("folder") or "<folder>"]
		if params.get("out_dir"):
			argv.append(params["out_dir"])
		argv += ["--factors"] + [str(f) for f in (parse_factors(params.get("factors")) or [2, 4, 8, 16])]
		if params.get("recursive"):
			argv.append("--recursive")
		if params.get("flat"):
			argv.append("--flat")
		return argv
	src = params.get("source") or "<source>"
	argv = [python, "-m", "texturetools.cli.scale_tex", src]
	if params.get("output"):
		argv.append(params["output"])
	by = params.get("by") or "factor"
	if by == "factor":
		argv += ["--factor", str(int(params.get("factor") or 1))]
	elif by == "size":
		argv += ["--size", (params.get("size") or "<size>").strip()]
	if params.get("trim"):
		argv.append("--trim")
	if (params.get("canvas") or "").strip():
		argv += ["--canvas", params["canvas"].strip()]
		if params.get("anchor", "topleft") != "topleft":
			argv += ["--anchor", params["anchor"]]
	elif by == "fit canvas":
		argv += ["--canvas", "<canvas>"]
	return argv


def save_like_cli(out, output):
	"""scale_tex.py's save: palette transparency kept, nothing else."""
	kw = {}
	if "transparency" in out.info:
		kw["transparency"] = out.info["transparency"]
	out.save(output, **kw)


@register_screen
class ScaleScreen(BulkPreviewMixin, ToolScreen):
	tool_name = "scale"
	title = "Scale"
	order = 50

	def default_params(self):
		return default_params()

	def build_controls(self):
		self._memory = None   # PIL image handed over from the editor (load_array)
		self._memory_name = ""
		self.add_heading("Mode")
		self.add_choice("mode", "Mode", list(MODES))

		self.add_heading("Single image (scale_tex)")
		self.add_slot("source", "Source", kind="file")
		self.add_slot("output", "Output (blank = <stem>_<size>.png)", kind="save")
		self.add_choice("by", "Scale by", list(BY))
		self.add_number("factor", "Factor", 1, 64, 1)
		self.add_text("size", "Target size S / WxH")
		self.add_toggle("trim", "Trim first")
		self.add_text("canvas", "Canvas S / WxH")
		self.add_choice("anchor", "Anchor", list(ANCHORS))

		self.add_heading("Folder (upscale_sprites)")
		self.add_slot("folder", "Input folder", kind="folder")
		self.add_slot("out_dir", "Output folder (blank = <folder>_upscaled)", kind="folder")
		self.add_text("factors", "Factors")
		self.add_toggle("recursive", "Recursive")
		self.add_toggle("flat", "Flat (_xN suffix)")
		self.install_bulk_view()
		self._sync_widgets()

	SINGLE_KEYS = ("by", "factor", "size", "trim", "canvas", "anchor")
	FOLDER_KEYS = ("factors", "recursive", "flat")

	def _sync_widgets(self):
		folder = self.params.get("mode") == "folder"
		for key in ("source", "output"):
			self.slots[key].disabled = folder
		for key in ("folder", "out_dir"):
			self.slots[key].disabled = not folder
		for key in self.SINGLE_KEYS:
			self.param_controls[key].disabled = folder
		for key in self.FOLDER_KEYS:
			self.param_controls[key].disabled = not folder
		if not folder:
			by = self.params.get("by")
			self.param_controls["factor"].disabled = by != "factor"
			self.param_controls["size"].disabled = by != "size"
		self.preview_all_button.disabled = not folder

	def set_param(self, key, value):
		if key == "source" and value:
			self._memory = None
		super().set_param(key, value)
		if key in ("mode", "by"):
			self._sync_widgets()
		self.note_params_changed()

	def update_params(self, values):
		super().update_params(values)
		self._sync_widgets()

	def load_array(self, arr, name=""):
		"""Editor handoff: scale an in-memory image (Run needs an output path; Copy as CLI needs a saved file)."""
		from ..widgets.preview import to_rgba_u8
		self._memory = Image.fromarray(to_rgba_u8(arr), "RGBA")
		self._memory_name = name or "image.png"
		self.update_params({"mode": "single", "source": ""})
		self.set_status("scaling {0} from the editor (unsaved; Run needs an output file)".format(self._memory_name))

	# ---- engine ----

	def _open_source(self, params):
		if self._memory is not None and not params.get("source"):
			return self._memory.copy(), MEMORY
		img = Image.open(params["source"])
		img.load()
		return img, params["source"]

	def validate(self, params):
		if params.get("mode") == "folder":
			if not params.get("folder"):
				return "pick an input folder"
			try:
				factors = upscale_factors(parse_factors(params.get("factors")))
			except ValueError:
				return "factors are whole numbers, e.g. 2 4 8"
			return None if factors else "give at least one factor above 1"
		if not params.get("source") and self._memory is None:
			return "pick a source"
		try:
			single_args(params)
		except ValueError as e:
			return str(e)
		return None

	def validate_run(self, params):
		if params.get("mode") != "folder" and not params.get("source") and not params.get("output"):
			return "pick an output file (the image came from the editor)"
		return None

	def compute_preview(self, params):
		if params.get("mode") == "folder":
			out_dir, factors, plan = folder_plan(params)
			if not plan:
				return {"preview": None, "status": "no supported images in {0}".format(params["folder"])}
			src, rel, factor, dest = plan[0]
			with Image.open(src) as img:
				arr = pil_to_rgba(upscale_pil(img, factor))
			n_img = len(plan) // len(factors)
			return {"preview": arr, "name": os.path.basename(str(dest)), "status": "{0} image(s) x {1} = {2} file(s) "
				"-> {3}; showing {4} x{5}".format(n_img, factors, len(plan), out_dir, rel.as_posix(), factor)}
		img, label = self._open_source(params)
		factor, size, canvas = single_args(params)
		try:
			out, info = scale_pipeline(img, factor, size, bool(params.get("trim")), canvas,
				params.get("anchor") or "topleft")
		except ValueError as e:
			return {"preview": None, "status": "cannot scale: {0}".format(e)}
		target = params.get("output") or (default_output(label, out.size) if label != MEMORY else "")
		parts = ["{0}x{1} {2}".format(img.size[0], img.size[1], img.mode)]
		if params.get("trim"):
			parts.append("trimmed {0}x{1}".format(*info["trimmed"]))
		parts.append("factor {0}x -> {1}x{2}".format(info["factor"], out.size[0], out.size[1]))
		if target:
			parts.append("saves {0}".format(os.path.basename(target)))
		name = os.path.basename(target) if target else self._memory_name if self._memory is not None else ""
		return {"preview": pil_to_rgba(out), "status": ", ".join(parts), "name": name}

	def show_preview(self, result):
		super().show_preview(result)
		if result.get("preview") is not None:
			self.result_name = result.get("name", "")

	def bulk_jobs(self, params):
		if params.get("mode") != "folder":
			return []
		out_dir, factors, _ = folder_plan(params)
		return upscale_jobs(Path(params["folder"]), out_dir, factors, bool(params.get("recursive")),
			bool(params.get("flat")))

	def run_job(self, params, progress):
		if params.get("mode") == "folder":
			out_dir, factors, plan = folder_plan(params)
			skip = set(params.get("_skip") or ())
			plan = [e for e in plan if "{0} x{1}".format(e[1].as_posix(), e[2]) not in skip]
			written, failed = [], []
			for i, (src, rel, factor, dest) in enumerate(plan, 1):
				try:
					upscale_image(src, factor, dest)
					written.append(str(dest))
				except Exception as e:  # upscale_sprites.py reports and goes on
					failed.append("{0} (x{1}): {2}".format(rel, factor, e))
				progress(i / float(len(plan)), rel.as_posix())
			status = "wrote {0} file(s) to {1}".format(len(written), out_dir)
			if failed:
				status += ", {0} failed: {1}".format(len(failed), "; ".join(failed[:3]))
			if skip:
				status += ", left out {0} unticked".format(len(skip))
			return {"outputs": written, "status": status}
		img, label = self._open_source(params)
		factor, size, canvas = single_args(params)
		out, _ = scale_pipeline(img, factor, size, bool(params.get("trim")), canvas, params.get("anchor") or "topleft")
		output = params.get("output") or default_output(label, out.size)
		progress(1.0, "saving")
		save_like_cli(out, output)
		return [output]

	def cli_argv(self, params):
		return cli_argv(params)

	def provenance_inputs(self, params):
		if params.get("mode") == "folder":
			return [params.get("folder") or ""]
		return [params.get("source") or MEMORY]

	def shutdown(self):
		super().shutdown()
		self.shutdown_bulk()

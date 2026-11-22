# SPDX-License-Identifier: GPL-3.0-only
"""
combine_textures.py - combine two textures (e.g. a base layer + an overlay layer)
using raw, gamma-agnostic alpha compositing. No color management is applied and
no gAMA/sRGB/ICC metadata is written to the output, so colors come out exactly
as stored -- no unexpected shifts.

Usage:
    python combine_textures.py <base> <overlay> <output> [--mode alpha|add|multiply|paint]
    python combine_textures.py --batch <base> <overlay_dir> <output_dir>
                               [--pattern "{name}.png"] [--mode ...]
    Add --center to either form to allow an overlay smaller than the base.
    Add --lcm to either form to scale both images up to a shared size first.

Arguments:
    base        Bottom-layer image (e.g. item_base.png)
    overlay     Top-layer image, composited over base (e.g. item_overlay.png)
    output      Path to save the combined result

Batch mode (--batch):
    overlay is a directory and output is a directory. base is combined with
    every image in overlay_dir (non-recursive, sorted), and each result is
    written to output_dir (created if missing) under a name built from
    --pattern. Pattern placeholders:
                  {name}  overlay file name without extension (e.g. apple1)
                  {ext}   overlay extension without the dot (e.g. png)
                  {base}  base file name without extension (e.g. can)
    Default pattern is "{name}.png". Files that aren't images or don't match
    the base size are skipped with a warning.

Options:
    --mode      How to combine pixels (default: alpha)
                  alpha    - standard "over" alpha compositing (overlay drawn on top of base)
                  add      - additive blend, channel values summed and clamped to 255
                  multiply - multiplicative blend (darkens), channel values multiplied
                  paint    - paint-on: base = shape and shading, overlay = the
                             material painted into the base's masked pixels.
                             Each masked pixel takes the overlay pixel at the same
                             spot, scaled by the base pixel's luminance over the
                             mean luminance of the masked base pixels (seams, rims
                             and outlines of the base stay). Alpha comes from the
                             base. Unmasked pixels keep the base. Same-size images.

Paint-mode options (ignored by the other modes):
    --mask PNG  Pixels to paint: every pixel with alpha > 0 in this PNG (same size
                as the base). Default: every opaque pixel of the base.
    --strength S
                How much of the base's shading is kept, 0..1 (default 1).
                0 copies the overlay pixels unchanged (e.g. a bucket's fill).
    --inset N   Sample the overlay only from its inside, N px in from each edge
                (coordinates clamped), e.g. 1 to leave out a block's 1 px border.
    --clip      Make unmasked pixels transparent instead of keeping the base
                (cuts a region out, e.g. a liquid, for use as a palette).

    --center    Allow an overlay smaller than the base: it is placed in the
                center of the base (padded with transparent pixels, never
                scaled). If the size difference is odd on an axis, the extra
                pixel goes right/bottom (offset = difference // 2). An overlay
                larger than the base on either axis is still an error/skip.

    --lcm       Scale base and overlay up to the least common multiple of their
                sizes before combining (each side separately: lcm of the widths,
                lcm of the heights). Each image gets its own whole-number factor
                (nearest-neighbor, every pixel becomes an exact block), e.g. a
                48x48 base and a 512x512 overlay both become 1536x1536 (base
                32x, overlay 3x). An image whose two factors would differ
                (aspect ratios that don't match) is an error/skip, since that
                would stretch it. Done before --center.

Layer stack and bulk options (P2, plan_texture_tools_gui.md section 4). Any of these switches to the extended
parser; without them the old parser and output are unchanged:
    --mode screen|overlay|subtract|min|max
                New blend modes (W3C separable blends on raw values).
    --opacity O Overlay opacity 0..1 (default 1).
    --offset X,Y
                Move the overlay by whole pixels after placement (negative allowed).
    --offset-mode clip|wrap|grow
                Pixels pushed past the canvas edge are cut (clip, default), re-enter on the other side (wrap),
                or the canvas grows to hold them (grow).
    --anchor A  Where a smaller or larger overlay sits: top_left (default), top, top_right, left, center, right,
                bottom_left, bottom, bottom_right. --center is --anchor center.
    --canvas C  base (default), largest, or WxH.
    --background COLOR
                Fill the canvas with RRGGBB, RRGGBBAA or r,g,b[,a] under the base (default transparent).
    --fit any|contain|same
                same: sizes must match (default when nothing below moves the overlay), contain: the overlay
                must fit inside the canvas (default with --center), any: crop what falls outside (default with
                --anchor, --offset, --offset-mode wrap|grow or --canvas).
    --dry-run   Batch: list what would be written, write nothing.
    --overwrite replace|skip|rename
                Batch: an existing output file is replaced (default), kept (skipped and reported), or the new
                one gets a _1, _2 ... suffix.
    --include GLOB, --exclude GLOB
                Batch: only overlay files matching (repeatable), minus files matching.

Stack file form:
    python combine_textures.py --stack recipe.json <output> [--dry-run] [--overwrite ...] [--yes] [--count]
                [--pattern ...] [--include ...] [--exclude ...]
    recipe.json is a stack preset: {"layers": [{"image": "can.png"}, {"image": "fruits", "mode": "alpha",
    "opacity": 1, "offset": [0, 0], "offset_mode": "clip", "anchor": "center", "scale": null, "size": null,
    "trim": false, "mask": null, "mask_invert": false, "visible": true}, ...], "canvas": "base",
    "background": null, "lcm": false, "fit": "any", "vary": [1]}. Relative paths resolve against the json's
    folder. --stack may also be the JSON text itself (starts with "{"; relative paths resolve against the
    current folder; the GUI's Copy as CLI writes this form). With no "vary" slot <output> is a file; with vary slots each such layer's "image" is a folder and
    <output> is a folder: one slot runs every file, two or more run every combination (cartesian), which needs
    --yes when more than one slot varies (--count prints the number of outputs and exits).
    Pattern placeholders in the stack form: {name} (varying stems joined with _), {ext}, {name<slot>},
    {ext<slot>}, {n}, {base}.

Notes:
    Without --center or --lcm both images must be the same pixel dimensions. The script
    never rescales, since that could distort pixel art.

Examples:
    python combine_textures.py item_base.png item_overlay.png item_combined.png
    python combine_textures.py wood_base.png grain_overlay.png result.png --mode multiply
    python combine_textures.py --batch canned/can.png canned/newlabel canned/newcan
    python combine_textures.py --batch can.png labels out --pattern "canned_{name}.png"
    python combine_textures.py plate_32.png icon_16.png out.png --center
    python combine_textures.py cutting_board.png polished_granite.png out.png --mode paint --inset 1
    python combine_textures.py wooden_bucket.png milk_bucket.png out.png --mode paint         --mask masks/milk_fill.png --strength 0
    python combine_textures.py packart/filtered/background/blueprint_large.png         packart/filtered/icon/book.png out.png --lcm
"""

import argparse
import json
import os
import sys

from PIL import Image

from texturetools.core.combine import *  # noqa: F401,F403 (the old module's names, for importers)
from texturetools.core import batch
from texturetools.core.combine import (ANCHORS, BLEND_MODES, FIT_POLICIES, MODES, NEW_MODES, OFFSET_MODES, Layer,
										  combine, combine_dir, combine_stack, count_stack_jobs, fit_overlay, lcm_pair,
										  parse_color, stack_from_dict, stack_jobs)
from texturetools.core.image_io import *  # noqa: F401,F403
from texturetools.core.image_io import load_rgba_array, save_raw


EXT_FLAGS = ("--stack", "--opacity", "--offset", "--offset-mode", "--anchor", "--canvas", "--background", "--fit",
			 "--dry-run", "--overwrite", "--include", "--exclude", "--yes", "--count")


def _extended(argv):
	"""True when argv uses a P2 flag or a new mode, so the extended parser runs (the old one stays byte-identical)."""
	for i, a in enumerate(argv):
		if a.split("=", 1)[0] in EXT_FLAGS:
			return True
		if a == "--mode" and i + 1 < len(argv) and argv[i + 1] in NEW_MODES:
			return True
		if a.startswith("--mode=") and a[7:] in NEW_MODES:
			return True
	return False


def _build_parser(extended):
	parser = argparse.ArgumentParser(
		description="Combine two textures via raw, gamma-agnostic pixel compositing."
	)
	parser.add_argument("base", help="Bottom-layer image")
	parser.add_argument("overlay", help="Top-layer image (directory with --batch)")
	parser.add_argument("output", help="Output file path (directory with --batch)")
	parser.add_argument(
		"--batch",
		action="store_true",
		help="Combine base with every image in the overlay directory",
	)
	parser.add_argument(
		"--pattern",
		default="{name}.png",
		help="Batch output name; placeholders {name}, {ext}, {base} (default: {name}.png)",
	)
	parser.add_argument(
		"--mode",
		choices=list(BLEND_MODES) if extended else MODES.keys(),
		default="alpha",
		help="Blend mode (default: alpha)",
	)
	parser.add_argument(
		"--center",
		action="store_true",
		help="Allow a smaller overlay; place it in the center of the base",
	)
	parser.add_argument(
		"--lcm",
		action="store_true",
		help="Scale both images to the least common multiple of their sizes first",
	)
	parser.add_argument("--mask", help="paint: PNG whose alpha > 0 pixels are painted (default: base's opaque pixels)")
	parser.add_argument("--strength", type=float, default=1.0, help="paint: share of the base's shading kept, 0..1 (default 1)")
	parser.add_argument("--inset", type=int, default=0, help="paint: sample the overlay N px in from its edges")
	parser.add_argument("--clip", action="store_true", help="paint: unmasked pixels become transparent")
	if extended:
		_add_ext_args(parser)
	return parser


def _add_ext_args(parser):
	parser.add_argument("--opacity", type=float, default=1.0, help="Overlay opacity 0..1 (default 1)")
	parser.add_argument("--offset", default="0,0", help="Overlay offset X,Y in whole pixels")
	parser.add_argument("--offset-mode", choices=OFFSET_MODES, default="clip", help="clip, wrap or grow (default clip)")
	parser.add_argument("--anchor", choices=list(ANCHORS), default=None, help="Overlay anchor (default top_left)")
	parser.add_argument("--canvas", default="base", help="base, largest or WxH (default base)")
	parser.add_argument("--background", help="Canvas color RRGGBB[AA] or r,g,b[,a] (default transparent)")
	parser.add_argument("--fit", choices=FIT_POLICIES, help="any, contain or same (default same; contain with --center)")
	_add_bulk_args(parser)


def _add_bulk_args(parser):
	parser.add_argument("--dry-run", action="store_true", help="Batch: list outputs, write nothing")
	parser.add_argument("--overwrite", choices=batch.OVERWRITE_POLICIES, default="replace",
						help="Batch: replace, skip or rename an existing output (default replace)")
	parser.add_argument("--include", action="append", help="Batch: only files matching this glob (repeatable)")
	parser.add_argument("--exclude", action="append", help="Batch: leave out files matching this glob (repeatable)")


def _canvas_arg(text):
	if text in ("base", "largest"):
		return text
	try:
		from texturetools.core.scale import parse_size
		return parse_size(text)
	except ValueError:
		raise SystemExit("Error: --canvas must be base, largest or WxH: {0}".format(text))


def _offset_arg(text):
	try:
		x, y = (int(v) for v in text.split(","))
	except ValueError:
		raise SystemExit("Error: --offset must be X,Y: {0}".format(text))
	return x, y


def _fail(msg):
	sys.stderr.write("Error: {0}\n".format(msg))
	sys.exit(1)


def _skip_text(r):
	return r.reason if any(str(v) in r.reason for v in r.job.inputs.values()) else "{0}: {1}".format(r.job.label, r.reason)


def stack_main(argv):
	"""--stack recipe.json <output>: run a stack preset once, or as a bulk run over its varying slots."""
	parser = argparse.ArgumentParser(description="Run a combine layer-stack preset (json).")
	parser.add_argument("--stack", required=True, help="Stack preset json file, or the JSON itself")
	parser.add_argument("output", help="Output file, or folder when the stack has varying slots")
	parser.add_argument("--pattern", default="{name}.png", help="Bulk output name (default {name}.png)")
	parser.add_argument("--yes", action="store_true", help="Run a cartesian (two or more varying slots) bulk run")
	parser.add_argument("--count", action="store_true", help="Print the number of outputs and exit")
	_add_bulk_args(parser)
	args = parser.parse_args(argv)
	try:
		if args.stack.lstrip().startswith("{"):  # inline stack JSON (the GUI's Copy as CLI, P6)
			data, base_dir = json.loads(args.stack), os.getcwd()
		else:
			with open(args.stack) as fh:
				data = json.load(fh)
			base_dir = os.path.dirname(os.path.abspath(args.stack))
		layers, opts = stack_from_dict(data, base_dir)
	except (OSError, ValueError, TypeError) as e:
		_fail("cannot read stack {0}: {1}".format(args.stack, e))
	vary = opts.pop("vary")
	if not vary:
		if args.count:
			print("1 output")
			return
		try:
			arr, layout = combine_stack(layers, return_layout=True, **opts)
		except ValueError as e:
			_fail(str(e))
		if args.dry_run:
			print("Would save: {0}".format(args.output))
			return
		save_raw(arr, args.output)
		print("Canvas:  {0}x{1}".format(*layout["size"]))
		print("Saved:   {0}".format(args.output))
		return
	folders = {}
	for slot in vary:
		src = layers[slot].image
		if not isinstance(src, str) or not os.path.isdir(src):
			_fail("varying slot {0} needs a folder as its image, got {1}".format(slot, src))
		folders[slot] = src
	n = count_stack_jobs(folders, args.include, args.exclude)
	if args.count:
		print("{0} output(s)".format(n))
		return
	if len(folders) > 1 and not (args.yes or args.dry_run):
		_fail("{0} combinations from {1} varying slots; rerun with --yes (or --dry-run)".format(n, len(folders)))
	jobs = stack_jobs(layers, folders, opts, args.pattern, args.include, args.exclude)

	def show(r):
		if r.status in ("skipped", "exists", "error"):
			sys.stderr.write("Skip:    {0}\n".format(_skip_text(r)))
		else:
			print("{0}{1}".format("Saved:   " if r.status == "written" else "Would save: ", r.output))

	report = batch.run_batch(jobs, args.output, dry_run=args.dry_run, overwrite=args.overwrite, on_result=show)
	c = report.counts()
	print("{0} {1} file(s) to {2}, {3} skipped".format("Would write" if args.dry_run else "Wrote",
		c.get("dry", 0) + c.get("written", 0), args.output, len(report.skipped) + len(report.errors)))


def ext_main(argv):
	"""The old two-image and --batch forms with the P2 flags, run as a two-layer stack."""
	args = _build_parser(True).parse_args(argv)
	paint = args.mode == "paint"
	layer_opts = dict(opacity=args.opacity, offset=_offset_arg(args.offset), offset_mode=args.offset_mode)
	anchor = args.anchor or ("center" if args.center else "top_left")
	moved = args.anchor or args.offset_mode != "clip" or args.canvas != "base" or layer_opts["offset"] != (0, 0)
	fit = args.fit or ("contain" if args.center else "any" if moved else "same")
	canvas = _canvas_arg(args.canvas)
	bg = None
	if args.background:
		try:
			bg = parse_color(args.background)
		except ValueError as e:
			_fail(e)
	if not os.path.exists(args.base):
		_fail("base file not found: {0}".format(args.base))
	opts = dict(canvas=canvas, background=bg, lcm=args.lcm, fit=fit)

	def top(image):
		return Layer(image, mode=args.mode, mask=args.mask if paint else None, strength=args.strength,
					 inset=args.inset, clip=args.clip, anchor=anchor, **layer_opts)

	if args.batch:
		if not os.path.isdir(args.overlay):
			_fail("overlay directory not found: {0}".format(args.overlay))
		layers = [Layer(args.base), top(None)]
		jobs = stack_jobs(layers, {1: args.overlay}, opts, args.pattern, args.include, args.exclude)

		def show(r):
			if r.status in ("skipped", "exists", "error"):
				sys.stderr.write("Skip:    {0}\n".format(_skip_text(r)))
			else:
				print("{0}{1}{2}".format("Saved:   " if r.status == "written" else "Would save: ", r.output,
										 "  " + r.info if r.info else ""))

		report = batch.run_batch(jobs, args.output, dry_run=args.dry_run, overwrite=args.overwrite, on_result=show)
		c = report.counts()
		print("Mode:    {0}".format(args.mode))
		print("Base:    {0}".format(args.base))
		print("{0} {1} file(s) to {2}".format("Would write" if args.dry_run else "Wrote",
			c.get("dry", 0) + c.get("written", 0), args.output))
		return
	if not os.path.exists(args.overlay):
		_fail("overlay file not found: {0}".format(args.overlay))
	try:
		arr, layout = combine_stack([Layer(args.base), top(args.overlay)], return_layout=True, **opts)
	except ValueError as e:
		_fail(e)
	if args.dry_run:
		print("Would save: {0}".format(args.output))
		return
	save_raw(arr, args.output)
	print("Mode:    {0}".format(args.mode))
	print("Base:    {0} ({1}x{2})".format(args.base, *Image.open(args.base).size))
	print("Overlay: {0} ({1}x{2})".format(args.overlay, *Image.open(args.overlay).size))
	print("Canvas:  {0}x{1}".format(*layout["size"]))
	if layout["lcm"]:
		print("Scaled:  {0}".format(layout["lcm"]))
	print("Saved:   {0}".format(args.output))


def main(argv=None):
	argv = sys.argv[1:] if argv is None else list(argv)
	if any(a == "--stack" or a.startswith("--stack=") for a in argv):
		return stack_main(argv)
	if _extended(argv):
		return ext_main(argv)
	parser = _build_parser(False)
	args = parser.parse_args(argv)
	paint_opts = dict(mask=args.mask, strength=args.strength, inset=args.inset, clip=args.clip)

	if not os.path.exists(args.base):
		sys.stderr.write("Error: base file not found: {0}\n".format(args.base))
		sys.exit(1)
	if args.batch:
		if not os.path.isdir(args.overlay):
			sys.stderr.write("Error: overlay directory not found: {0}\n".format(args.overlay))
			sys.exit(1)
		combine_dir(args.base, args.overlay, args.output, args.pattern, args.mode, args.center, args.lcm, paint_opts)
		return

	if not os.path.exists(args.overlay):
		sys.stderr.write("Error: overlay file not found: {0}\n".format(args.overlay))
		sys.exit(1)

	base_arr = load_rgba_array(args.base)
	overlay_arr = load_rgba_array(args.overlay)
	info = None
	if args.lcm:
		try:
			base_arr, overlay_arr, info = lcm_pair(base_arr, overlay_arr)
		except ValueError as e:
			sys.stderr.write("Error: {0}\n".format(e))
			sys.exit(1)

	fitted = fit_overlay(base_arr, overlay_arr, args.center)
	if fitted is None:
		sys.stderr.write(
			"Error: overlay does not fit. base is {0}, overlay is {1}{2}\n".format(
				base_arr.shape[:2][::-1],
				overlay_arr.shape[:2][::-1],
				"" if args.center else " (use --center for a smaller overlay)",
			)
		)
		sys.exit(1)

	try:
		save_raw(combine(args.mode, base_arr, fitted, paint_opts), args.output)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)

	print("Mode:    {0}".format(args.mode))
	print("Base:    {0} ({1}x{2})".format(args.base, *Image.open(args.base).size))
	print("Overlay: {0} ({1}x{2})".format(args.overlay, *Image.open(args.overlay).size))
	if info:
		print("Scaled:  {0}".format(info))
	print("Saved:   {0}".format(args.output))


if __name__ == "__main__":
	main()

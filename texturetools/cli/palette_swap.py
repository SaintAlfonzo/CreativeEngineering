# SPDX-License-Identifier: GPL-3.0-only
"""
palette_swap.py - swap the palette of a source image to match a target palette image.

Usage (single file):
    python palette_swap.py <source> <palette> <output> [--invert] [--drop CLASS,...] [--interpolate] [--even] [--preview]

Usage (bulk):
    python palette_swap.py <shape> --bulk [--invert] [--drop CLASS,...] [--interpolate] [--even] [--preview]

    Bulk mode works the OPPOSITE way from single-file mode: <shape> is the one
    file whose SHAPE stays fixed for every output. Each image found in
    BULK_SOURCE_DIR instead supplies its own PALETTE (colors), which gets
    applied onto <shape>. So you get one output per item in BULK_SOURCE_DIR,
    all sharing <shape>'s silhouette but each colored like its own source item.
    Results are written into BULK_OUTPUT_DIR -- edit those two constants below
    to point at your folders.

Arguments:
    source      Image whose shape/pixels will be recolored (e.g. aluminum_wire.png)
                (single-file mode only; omitted in --bulk mode)
    palette     Image to sample colors from (e.g. silver_ingot.png)
                (single-file mode only; in --bulk mode this role is instead
                played by <shape>'s positional slot, see above)
    output      Path to save the result (single-file mode only; omitted
                in --bulk mode)

Options:
    --bulk      Keep <shape>'s silhouette fixed and, for every image in
                BULK_SOURCE_DIR, apply that image's own palette onto <shape>,
                writing results into BULK_OUTPUT_DIR (created if it doesn't exist).
    --invert    Invert the palette mapping so dark maps to light and vice versa.
                Use this when you want the outline of the item to be the darkest color.
    --drop      Comma-separated color classes to leave out of the palette
                before mapping: brown, white (see DROP_CLASSES). Ignored for a
                palette where it would leave no colors.
    --drop-darkest N
                Leave the N darkest colors out of the palette (e.g. an item's
                outline, so a soft block template stays soft). Ignored when it
                would leave no colors.
    --drop-lightest N
                The same for the N lightest colors (applied after --drop-darkest).
    --interpolate
                When the palette has fewer unique colors than the image being
                recolored, force an even gradient: the source shades, ranked by
                brightness, are spaced evenly from the palette's first color to
                its last, blending between neighboring palette colors, instead
                of several source shades snapping onto one palette color. Keeps
                shading from a near-uniform palette or a gradient. When the
                palette has as many colors as the source or more (no effect
                before 2026-11-22), it is split into one run per source
                brightness level and each level takes its run's pixel-weighted
                mean color. No effect with a one-color palette.
    --even      Even rank spacing without blending: the source's distinct brightness
                levels, ranked, are spread evenly over the palette (rank i of n
                takes palette position i/(n-1)) instead of by brightness value,
                which bunched shades onto one end of the palette. When the palette
                has more colors than the source has levels, the palette is split
                into that many runs (in brightness order) and each level takes the
                most-used real color of its run, so no stretch of the palette is
                skipped, also colors whose hue breaks the brightness order.
                Without --even and --interpolate the legacy mapping is unchanged.
    --preview   Also save a 160x160 upscaled preview alongside each output.

Mapping options (2026-11-22, plan_texture_tools_gui.md section 3; every default keeps the legacy output):
    --mode M    rank (default, legacy), even (same as --even), role (hue family, then
                lightness rank inside it), nearest (closest palette color in Lab),
                ramp (source lightness fitted onto the palette sorted by lightness)
    --interp W  off (default), smaller (blend only when the palette has fewer colors
                than the source), always (same as --interpolate)
    --interp-amount F   0..1, how far toward the blended color (default 1)
    --interp-space S    rgb (default), hsv, lab
    --palette-alpha N   ignore palette pixels with alpha below N (default 1 = every
                non-transparent pixel; 255 = opaque only)
    --palette-exclude C,C   palette colors to leave out (#rrggbb, comma separated)
    --palette-tol N     also leave out colors within N per channel of those
    --keep-source C,C   source colors left untouched (outlines, eyes)
    --keep-tol N        tolerance for --keep-source
    --hue-shift DEG, --sat-shift F, --val-shift F   shift the palette first (F in -1..1)
    --preserve F        0..1, blend the result back toward the source
    --stretch           contrast stretch the source lightness (nearest, ramp)
    --alpha A           palette (default, legacy: alpha from the palette color) or source
    --dither D          none (default) or ordered (only where the palette is smaller)

Texture Tools GUI options (2026-11-22, P6; "Copy as CLI" emits them):
    palette     may be colors:#rrggbb,#rrggbb (a hand-built swatch strip) instead of a file
    --bulk with --in-dir DIR / --out-dir DIR    the bulk folders instead of the constants below
    --reverse           bulk: <shape> is the palette, every image in the folder is a source
    --pattern P         bulk output name, fields {name} {ext} {shape} {palette} {n}
    --overwrite W       replace (default), skip or rename an existing output
    --include G, --exclude G   bulk globs (repeatable); --dry-run lists outputs, writes nothing

Examples:
    python palette_swap.py wire_aluminum.png ingot_silver.png silver_wire.png --invert
    python palette_swap.py gold_nugget.png rose_gold_ingot.png rose_gold_nugget.png --invert
    python palette_swap.py wire_aluminum.png --bulk --invert --preview
    python palette_swap.py strad_wine.png creeper_wine.png creeper_box.png --drop brown
    python palette_swap.py gradient_block.png flat_block.png out.png --interpolate
    python palette_swap.py jelly_content.png pepper.png pepper_jelly.png --even
"""

import argparse
import os
import sys

import numpy as np
from PIL import Image

from texturetools.core.palette import *  # noqa: F401,F403 (the old module's names, for importers)
from texturetools.core.palette import DROP_CLASSES, get_colors_by_brightness, load_palette_colors
from texturetools.core.swap import *  # noqa: F401,F403
from texturetools.core.swap import (ALPHA_SOURCES, DITHERS, INTERPOLATE, MODES, SPACES, IMAGE_EXTENSIONS, SwapOptions,
									 _save, is_colors_spec, load_palette_source, load_rgba_u8, prepare_palette,
									 process_one, save_swap, swap_colors, swap_jobs)

# ---------------------------------------------------------------------------
# Bulk mode folders -- edit these to point at your own source/output folders.
# ALL image files in BULK_SOURCE_DIR are processed when --bulk is used.
# ---------------------------------------------------------------------------
BULK_SOURCE_DIR = "./in"
BULK_OUTPUT_DIR = "./out"


def parse_drops(value):
	names = [n.strip().lower() for n in value.split(",") if n.strip()]
	for n in names:
		if n not in DROP_CLASSES:
			raise argparse.ArgumentTypeError(
				"unknown color class {0!r} (known: {1})".format(n, ", ".join(sorted(DROP_CLASSES)))
			)
	return names


def parse_color_list(value):
	from texturetools.core.palette import parse_color
	out = []
	for part in value.split(","):
		part = part.strip()
		if part:
			try:
				parse_color(part)
			except ValueError as e:
				raise argparse.ArgumentTypeError(str(e))
			out.append(part)
	return out


def add_mode_args(parser):
	"""The P1 mapping flags (shared by single and --bulk). The usage line stays the legacy one (error output is
	part of the T1 parity); -h lists the new flags."""
	parser.usage = parser.format_usage()[len("usage: "):].rstrip("\n")
	parser.add_argument("--mode", choices=MODES, default="rank", help="Mapping mode (default rank, the legacy map)")
	parser.add_argument("--interp", choices=INTERPOLATE, default="off", help="Blend between palette neighbors")
	parser.add_argument("--interp-amount", type=float, default=1.0, metavar="F", help="0..1 toward the blend")
	parser.add_argument("--interp-space", choices=SPACES, default="rgb", help="Blend space")
	parser.add_argument("--palette-alpha", type=int, default=1, metavar="N",
						help="Ignore palette pixels with alpha below N (1 = legacy, 255 = opaque only)")
	parser.add_argument("--palette-exclude", type=parse_color_list, default=[], metavar="C,C",
						help="Palette colors to leave out (#rrggbb, comma separated)")
	parser.add_argument("--palette-tol", type=int, default=0, metavar="N", help="Tolerance for --palette-exclude")
	parser.add_argument("--keep-source", type=parse_color_list, default=[], metavar="C,C",
						help="Source colors left untouched (#rrggbb, comma separated)")
	parser.add_argument("--keep-tol", type=int, default=0, metavar="N", help="Tolerance for --keep-source")
	parser.add_argument("--hue-shift", type=float, default=0.0, metavar="DEG", help="Shift palette hue")
	parser.add_argument("--sat-shift", type=float, default=0.0, metavar="F", help="Shift palette saturation (-1..1)")
	parser.add_argument("--val-shift", type=float, default=0.0, metavar="F", help="Shift palette value (-1..1)")
	parser.add_argument("--preserve", type=float, default=0.0, metavar="F", help="0..1 blend back toward the source")
	parser.add_argument("--stretch", action="store_true", help="Contrast stretch source lightness (nearest, ramp)")
	parser.add_argument("--alpha", choices=ALPHA_SOURCES, default="palette", help="Output alpha source")
	parser.add_argument("--dither", choices=DITHERS, default="none", help="Ordered dither for smaller palettes")


def options_from_args(args):
	"""SwapOptions from parsed args; --even / --interpolate fold into --mode even / --interp always."""
	mode = "even" if args.even and args.mode == "rank" else args.mode
	interp = "always" if args.interpolate and args.interp == "off" else args.interp
	return SwapOptions(
		mode=mode, interpolate=interp, interp_amount=args.interp_amount, interp_space=args.interp_space,
		invert=args.invert, drops=list(args.drop), drop_darkest=args.drop_darkest, drop_lightest=args.drop_lightest,
		palette_alpha=args.palette_alpha, palette_exclude=list(args.palette_exclude),
		palette_exclude_tol=args.palette_tol, source_keep=list(args.keep_source), source_keep_tol=args.keep_tol,
		hue_shift=args.hue_shift, sat_shift=args.sat_shift, val_shift=args.val_shift, preserve=args.preserve,
		stretch=args.stretch, alpha=args.alpha, dither=args.dither).validate()


def print_new_options(opts):
	"""One line for the non-default P1 options (nothing for a legacy run, so legacy stdout is unchanged)."""
	d = SwapOptions()
	extra = ["{0}={1}".format(k, v) for k, v in opts.to_dict().items()
			 if k not in ("invert", "drops", "drop_darkest", "drop_lightest") and v != getattr(d, k)]
	if extra and not opts.is_legacy():
		print("Options: {0}".format(", ".join(extra)))


def swap_one(pal_path, opts):
	"""Load one palette; returns (palette image, palette colors, run(src_arr, out_path, preview)). The legacy
	calls when opts is legacy, else the P1 engine. pal_path may be a colors:#rrggbb,... swatch strip (P6)."""
	if is_colors_spec(pal_path):
		pal_arr = load_palette_source(pal_path)
		palette_colors, palette_counts = prepare_palette(pal_arr, opts)
		return Image.fromarray(pal_arr, "RGBA"), palette_colors, lambda src_arr, out_path, preview: _save(
			swap_colors(src_arr, palette_colors, opts, palette_counts), out_path, preview)
	if opts.is_legacy():
		pal_img, palette_colors, palette_counts = load_palette_colors(
			pal_path, opts.invert, opts.drops, opts.drop_darkest, opts.drop_lightest, with_counts=True)
		return pal_img, palette_colors, lambda src_arr, out_path, preview: process_one(
			src_arr, palette_colors, out_path, preview, opts.interpolate == "always", opts.mode == "even",
			palette_counts)
	pal_img = Image.open(pal_path).convert("RGBA")
	palette_colors, palette_counts = prepare_palette(np.array(pal_img), opts)
	return pal_img, palette_colors, lambda src_arr, out_path, preview: _save(
		swap_colors(src_arr, palette_colors, opts, palette_counts), out_path, preview)


def run_single(args):
	src_path, pal_path, out_path = args.source, args.palette, args.output

	if not os.path.exists(src_path):
		sys.stderr.write("Error: source file not found: {0}\n".format(src_path))
		sys.exit(1)
	if not os.path.exists(pal_path) and not is_colors_spec(pal_path):
		sys.stderr.write("Error: palette file not found: {0}\n".format(pal_path))
		sys.exit(1)

	try:
		opts = options_from_args(args)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)
	pal_img, palette_colors, run = swap_one(pal_path, opts)
	src_img = Image.open(src_path).convert("RGBA")
	src_arr = np.array(src_img)

	print(
		"Source:  {0} ({1}x{2}, {3} colors)".format(
			src_path, src_img.size[0], src_img.size[1],
			len(get_colors_by_brightness(src_arr)),
		)
	)
	print(
		"Palette: {0} ({1}x{2}, {3} colors)".format(
			pal_path, pal_img.size[0], pal_img.size[1], len(palette_colors)
		)
	)
	print("Invert:  {0}".format(args.invert))
	if args.interpolate:
		print("Interpolate: on (even gradient; a larger palette is averaged in even runs)")
	if args.even:
		print("Even:    on (even rank spacing; a larger palette is sampled in even runs)")
	if args.drop:
		print("Drop:    {0}".format(", ".join(args.drop)))
	print_new_options(opts)

	try:
		out_path, preview_path = run(src_arr, out_path, args.preview)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)

	print("Saved:   {0}".format(out_path))
	if preview_path:
		print("Preview: {0}".format(preview_path))


def run_bulk(args, src_dir=None, out_dir=None):
	shape_path = args.shape
	src_dir = BULK_SOURCE_DIR if src_dir is None else src_dir
	out_dir = BULK_OUTPUT_DIR if out_dir is None else out_dir

	if not os.path.exists(shape_path):
		sys.stderr.write("Error: shape file not found: {0}\n".format(shape_path))
		sys.exit(1)
	if not os.path.isdir(src_dir):
		sys.stderr.write(
			"Error: BULK_SOURCE_DIR is not a directory: {0}\n"
			"Edit BULK_SOURCE_DIR near the top of this script to point at your items folder.\n".format(src_dir)
		)
		sys.exit(1)

	os.makedirs(out_dir, exist_ok=True)

	# <shape>'s silhouette/pixels stay fixed for every output -- load it once.
	shape_img = Image.open(shape_path).convert("RGBA")
	shape_arr = np.array(shape_img)
	print(
		"Shape:   {0} ({1}x{2}, {3} colors)".format(
			shape_path, shape_img.size[0], shape_img.size[1],
			len(get_colors_by_brightness(shape_arr)),
		)
	)
	print("Invert:  {0}".format(args.invert))
	if args.interpolate:
		print("Interpolate: on (even gradient; a larger palette is averaged in even runs)")
	if args.even:
		print("Even:    on (even rank spacing; a larger palette is sampled in even runs)")
	if args.drop:
		print("Drop:    {0}".format(", ".join(args.drop)))
	try:
		opts = options_from_args(args)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)
	print_new_options(opts)

	# Don't treat the shape file itself as one of the palette-providing items,
	# in case it happens to also live in src_dir.
	shape_abs = os.path.abspath(shape_path)

	filenames = sorted(
		f for f in os.listdir(src_dir)
		if f.lower().endswith(IMAGE_EXTENSIONS)
		and os.path.abspath(os.path.join(src_dir, f)) != shape_abs
	)

	if not filenames:
		sys.stderr.write(
			"Warning: no image files ({0}) found in {1}\n".format(
				", ".join(IMAGE_EXTENSIONS), src_dir
			)
		)
		sys.exit(1)

	print("Found {0} palette source(s) in {1}".format(len(filenames), src_dir))

	ok_count = 0
	fail_count = 0
	for fname in filenames:
		item_path = os.path.join(src_dir, fname)
		out_path = os.path.join(out_dir, fname)
		try:
			# Each item in the folder supplies its OWN palette, which gets
			# applied onto the fixed shape's pixels.
			_, _, run = swap_one(item_path, opts)
			saved_path, preview_path = run(shape_arr, out_path, args.preview)
			msg = "  OK    {0} -> {1}".format(fname, saved_path)
			if preview_path:
				msg += " (+ preview)"
			print(msg)
			ok_count += 1
		except Exception as e:
			# Isolate per-file failures so one bad/corrupt/empty image doesn't
			# abort the whole batch.
			print("  FAIL  {0}: {1}".format(fname, e))
			fail_count += 1

	print("Done. {0} succeeded, {1} failed.".format(ok_count, fail_count))
	if fail_count and not ok_count:
		sys.exit(1)


def add_bulk_batch_args(parser):
	"""P6 bulk flags (the GUI's Copy as CLI). --in-dir / --out-dir alone keep the legacy bulk run; any other flag
	here runs the bulk through core/batch.py (name pattern, overwrite policy, globs, dry run, reversed roles)."""
	parser.add_argument("--in-dir", help="Bulk folder (default BULK_SOURCE_DIR)")
	parser.add_argument("--out-dir", help="Bulk output folder (default BULK_OUTPUT_DIR)")
	parser.add_argument("--reverse", action="store_true",
						help="Reverse roles: <shape> is the palette (file or colors:#rrggbb,...), every image in the folder is a source")
	parser.add_argument("--pattern", help="Output name; fields {name} {ext} {shape} {palette} {n} (default {name}.png)")
	parser.add_argument("--overwrite", choices=("replace", "skip", "rename"), help="Existing output (default replace)")
	parser.add_argument("--include", action="append", help="Only files matching this glob (repeatable)")
	parser.add_argument("--exclude", action="append", help="Leave out files matching this glob (repeatable)")
	parser.add_argument("--dry-run", action="store_true", help="List outputs, write nothing")


def uses_batch(args):
	return bool(args.reverse or args.pattern or args.overwrite or args.include or args.exclude or args.dry_run)


def run_bulk_batch(args, src_dir, out_dir):
	"""The bulk run through core/batch.py (P6 flags)."""
	from texturetools.core import batch
	if not os.path.exists(args.shape) and not is_colors_spec(args.shape):
		sys.stderr.write("Error: file not found: {0}\n".format(args.shape))
		sys.exit(1)
	if not os.path.isdir(src_dir):
		sys.stderr.write("Error: bulk folder is not a directory: {0}\n".format(src_dir))
		sys.exit(1)
	try:
		opts = options_from_args(args)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)
	jobs = swap_jobs(args.shape, src_dir, args.reverse, opts, args.pattern or "{name}.png", args.include, args.exclude)

	def show(r):
		if r.status in ("skipped", "exists", "error"):
			print("  SKIP  {0}: {1}".format(r.job.label, r.reason))
		else:
			print("  {0}  {1} -> {2}".format("OK  " if r.status == "written" else "DRY ", r.job.label, r.output))

	report = batch.run_batch(jobs, out_dir, dry_run=args.dry_run, overwrite=args.overwrite or "replace",
							 on_result=show, saver=save_swap)
	c = report.counts()
	print("Done. {0} {1}, {2} skipped.".format(c.get("dry", 0) + c.get("written", 0),
		"would be written" if args.dry_run else "written", len(report.skipped) + len(report.errors)))


def main(argv=None, bulk_source_dir=None, bulk_output_dir=None):
	"""argv defaults to sys.argv[1:]; the bulk folders to BULK_SOURCE_DIR / BULK_OUTPUT_DIR (the textureswapper shim
	passes its own constants)."""
	if argv is None:
		argv = sys.argv[1:]
	# --bulk mode ignores source/output folders entirely and instead uses
	# BULK_SOURCE_DIR / BULK_OUTPUT_DIR defined near the top of this file, so
	# its CLI signature is just: palette_swap.py <palette> --bulk [flags]
	if "--bulk" in argv:
		parser = argparse.ArgumentParser(
			description="Apply the palette of every image in BULK_SOURCE_DIR onto the fixed shape of <shape>."
		)
		parser.add_argument("shape", help="Image whose shape/silhouette stays fixed for every output")
		parser.add_argument("--bulk", action="store_true", help=argparse.SUPPRESS)
		parser.add_argument(
			"--invert",
			action="store_true",
			help="Invert palette mapping (dark outline / light fill). Recommended for most items.",
		)
		parser.add_argument(
			"--drop-lightest", type=int, default=0, metavar="N",
			help="Leave the N lightest colors out of each palette",
		)
		parser.add_argument(
			"--drop-darkest", type=int, default=0, metavar="N",
			help="Leave the N darkest colors out of each palette (e.g. an outline)",
		)
		parser.add_argument(
			"--drop",
			type=parse_drops,
			default=[],
			help="Comma-separated color classes to leave out of each palette: " + ", ".join(sorted(DROP_CLASSES)),
		)
		parser.add_argument(
			"--interpolate",
			action="store_true",
			help="Blend: even gradient across the palette; a palette as large as the source or larger is averaged in even runs.",
		)
		parser.add_argument(
			"--even",
			action="store_true",
			help="Even rank spacing: source shades spread evenly over the palette; a larger palette is sampled in even runs.",
		)
		parser.add_argument(
			"--preview",
			action="store_true",
			help="Also save a 160x160 upscaled preview alongside each output.",
		)
		add_mode_args(parser)
		add_bulk_batch_args(parser)
		args = parser.parse_args(argv)
		src_dir = args.in_dir or bulk_source_dir
		out_dir = args.out_dir or bulk_output_dir
		if uses_batch(args):
			run_bulk_batch(args, src_dir or BULK_SOURCE_DIR, out_dir or BULK_OUTPUT_DIR)
			return
		run_bulk(args, src_dir, out_dir)
		return

	parser = argparse.ArgumentParser(
		description="Swap the palette of a source image to match a palette image."
	)
	parser.add_argument("source", help="Source image")
	parser.add_argument("palette", help="Palette image (colors to sample from)")
	parser.add_argument("output", help="Output file path")
	parser.add_argument(
		"--invert",
		action="store_true",
		help="Invert palette mapping (dark outline / light fill). Recommended for most items.",
	)
	parser.add_argument(
		"--drop-lightest", type=int, default=0, metavar="N",
		help="Leave the N lightest colors out of the palette",
	)
	parser.add_argument(
		"--drop-darkest", type=int, default=0, metavar="N",
		help="Leave the N darkest colors out of the palette (e.g. an outline)",
	)
	parser.add_argument(
		"--drop",
		type=parse_drops,
		default=[],
		help="Comma-separated color classes to leave out of the palette: " + ", ".join(sorted(DROP_CLASSES)),
	)
	parser.add_argument(
		"--interpolate",
		action="store_true",
		help="Blend: even gradient across the palette; a palette as large as the source or larger is averaged in even runs.",
	)
	parser.add_argument(
		"--even",
		action="store_true",
		help="Even rank spacing: source shades spread evenly over the palette; a larger palette is sampled in even runs.",
	)
	parser.add_argument(
		"--preview",
		action="store_true",
		help="Also save a 160x160 upscaled preview alongside each output.",
	)
	add_mode_args(parser)
	args = parser.parse_args(argv)
	run_single(args)


if __name__ == "__main__":
	main()

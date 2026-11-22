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
                palette where it would leave no colours.
    --drop-darkest N
                Leave the N darkest colors out of the palette (e.g. an item's
                outline, so a soft block template stays soft). Ignored when it
                would leave no colours.
    --drop-lightest N
                The same for the N lightest colors (applied after --drop-darkest).
    --interpolate
                When the palette has fewer unique colors than the image being
                recoloured, force an even gradient: the source shades, ranked by
                brightness, are spaced evenly from the palette's first color to
                its last, blending between neighboring palette colors, instead
                of several source shades snapping onto one palette colour. Keeps
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
from texturetools.core.swap import IMAGE_EXTENSIONS, process_one

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


def run_single(args):
	src_path, pal_path, out_path = args.source, args.palette, args.output

	if not os.path.exists(src_path):
		sys.stderr.write("Error: source file not found: {0}\n".format(src_path))
		sys.exit(1)
	if not os.path.exists(pal_path):
		sys.stderr.write("Error: palette file not found: {0}\n".format(pal_path))
		sys.exit(1)

	pal_img, palette_colors, palette_counts = load_palette_colors(
		pal_path, args.invert, args.drop, args.drop_darkest, args.drop_lightest, with_counts=True)
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

	try:
		out_path, preview_path = process_one(src_arr, palette_colors, out_path, args.preview, args.interpolate,
											 args.even, palette_counts)
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
			_, palette_colors, palette_counts = load_palette_colors(
				item_path, args.invert, args.drop, args.drop_darkest, args.drop_lightest, with_counts=True)
			saved_path, preview_path = process_one(shape_arr, palette_colors, out_path, args.preview, args.interpolate,
												   args.even, palette_counts)
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
		args = parser.parse_args(argv)
		run_bulk(args, bulk_source_dir, bulk_output_dir)
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
	args = parser.parse_args(argv)
	run_single(args)


if __name__ == "__main__":
	main()

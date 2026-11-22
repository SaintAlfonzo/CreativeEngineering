# SPDX-License-Identifier: GPL-3.0-only
"""
scale_tex.py - upscale an image by a whole-number ratio with nearest-neighbour
sampling, so every source pixel becomes an exact N x N block (pixel art stays
crisp, no blur, no new colours). The image mode is kept (palette images stay
palette images) and no gAMA/sRGB/ICC metadata is written.

Usage:
    python scale_tex.py <input> [output] (--factor N | --size S | --size WxH)
                        [--trim] [--canvas S|WxH [--anchor topleft|center]]

Arguments:
    input       Source image
    output      Where to save the result. Default: next to the input, named
                <stem>_<width>.png (square) or <stem>_<width>x<height>.png

Options:
    --factor N  Multiply both sides by N (N >= 1)
    --size S    Target size, S square or WxH. Each side must be the same
                whole-number multiple of the source side; otherwise the script
                stops instead of stretching. With --canvas, --size may instead
                be omitted and the largest factor that fits the canvas is used.
    --trim      Crop to the non-transparent area before scaling (drops
                transparent padding).
    --canvas S  Place the scaled image on a transparent canvas of this size
                (S square or WxH), e.g. art that can't reach the canvas by a
                whole-number factor.
    --anchor    Where the image sits on the canvas: topleft (default) or
                center (odd leftovers go right/bottom).

Examples:
    python scale_tex.py packart/filtered/background/blueprint_large.png --size 512
    python scale_tex.py icon.png icon_x4.png --factor 4
    python scale_tex.py blueprint_large.png --trim --canvas 512
        (48x48 art in a 64x64 file -> 10x = 480, padded to 512)
"""

import argparse
import os
import sys

from PIL import Image

from texturetools.core.scale import *  # noqa: F401,F403 (the old module's names, for importers)
from texturetools.core.scale import (default_output, fit_factor, parse_size, place_on_canvas, resolve_factor,
                                     scale_image, trim)


def main(argv=None):
	parser = argparse.ArgumentParser(
		description="Upscale an image by a whole-number ratio (nearest-neighbour)."
	)
	parser.add_argument("input", help="Source image")
	parser.add_argument("output", nargs="?", help="Output path (default: <stem>_<size>.png)")
	group = parser.add_mutually_exclusive_group()
	group.add_argument("--factor", type=int, help="Whole-number scale factor")
	group.add_argument("--size", help="Target size: S (square) or WxH")
	parser.add_argument("--trim", action="store_true", help="Crop transparent padding first")
	parser.add_argument("--canvas", help="Pad the result onto a transparent canvas: S or WxH")
	parser.add_argument("--anchor", choices=("topleft", "center"), default="topleft",
						help="Placement on the canvas (default: topleft)")
	args = parser.parse_args(argv)
	if args.factor is None and args.size is None and args.canvas is None:
		parser.error("give --factor, --size or --canvas")

	if not os.path.isfile(args.input):
		sys.stderr.write("Error: input file not found: {0}\n".format(args.input))
		sys.exit(1)

	img = Image.open(args.input)
	img.load()
	src = trim(img) if args.trim else img
	try:
		canvas = parse_size(args.canvas) if args.canvas else None
		size = parse_size(args.size) if args.size else None
		if args.factor is None and size is None:
			factor = fit_factor(src.size, canvas)
		else:
			factor = resolve_factor(src.size, args.factor, size)
		out = scale_image(src, factor)
		if canvas:
			out = place_on_canvas(out, canvas, args.anchor)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)
	output = args.output or default_output(args.input, out.size)
	save_kwargs = {}
	if "transparency" in out.info:
		save_kwargs["transparency"] = out.info["transparency"]
	out.save(output, **save_kwargs)

	print("Input:   {0} ({1}x{2}, {3})".format(args.input, img.size[0], img.size[1], img.mode))
	if args.trim:
		print("Trimmed: {0}x{1}".format(*src.size))
	print("Factor:  {0}x ({1}x{2})".format(factor, src.size[0] * factor, src.size[1] * factor))
	print("Saved:   {0} ({1}x{2})".format(output, out.size[0], out.size[1]))


if __name__ == "__main__":
	main()

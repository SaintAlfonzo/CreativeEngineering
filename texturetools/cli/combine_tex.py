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
                centre of the base (padded with transparent pixels, never
                scaled). If the size difference is odd on an axis, the extra
                pixel goes right/bottom (offset = difference // 2). An overlay
                larger than the base on either axis is still an error/skip.

    --lcm       Scale base and overlay up to the least common multiple of their
                sizes before combining (each side separately: lcm of the widths,
                lcm of the heights). Each image gets its own whole-number factor
                (nearest-neighbour, every pixel becomes an exact block), e.g. a
                48x48 base and a 512x512 overlay both become 1536x1536 (base
                32x, overlay 3x). An image whose two factors would differ
                (aspect ratios that don't match) is an error/skip, since that
                would stretch it. Done before --center.

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
import os
import sys

from PIL import Image

from texturetools.core.combine import *  # noqa: F401,F403 (the old module's names, for importers)
from texturetools.core.combine import MODES, combine, combine_dir, fit_overlay, lcm_pair
from texturetools.core.image_io import *  # noqa: F401,F403
from texturetools.core.image_io import load_rgba_array, save_raw


def main(argv=None):
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
		choices=MODES.keys(),
		default="alpha",
		help="Blend mode (default: alpha)",
	)
	parser.add_argument(
		"--center",
		action="store_true",
		help="Allow a smaller overlay; place it in the centre of the base",
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

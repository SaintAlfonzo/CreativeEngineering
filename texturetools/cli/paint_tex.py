# SPDX-License-Identifier: GPL-3.0-only
"""
paint_tex.py - paint a material texture into a shape, keeping the shape's
shading (core/paint.py, the Texture Painter screen's engine). Every flag is a
PaintOptions field; the defaults are paint()'s defaults, so a bare run equals
combine_tex.py --mode paint.

Usage:
    python3 -m texturetools.cli.paint_tex SHAPE MATERIAL OUTPUT [options]
    python3 -m texturetools.cli.paint_tex FIXED --bulk --in-dir DIR --out-dir DIR [--reverse] [options]

Bulk: FIXED is the shape and every image in --in-dir a material (one shape,
many materials); with --reverse FIXED is the material and every image in
--in-dir a shape.

Examples:
    python3 -m texturetools.cli.paint_tex bucket.png granite.png out.png --strength 0.6 --outline keep
    python3 -m texturetools.cli.paint_tex can.png label.png out.png --mode projected --fit contain
    python3 -m texturetools.cli.paint_tex can.png --bulk --in-dir labels --out-dir out --pattern "can_{material}.png"
"""

import argparse
import os
import sys

from texturetools.core import batch
from texturetools.core.image_io import load_rgba_array, save_raw
from texturetools.core.paint import (FITS, MATERIAL_ALPHA, OUTLINES, PAINT_MODES, ROTATIONS, PaintOptions, paint,
									 paint_jobs)
from texturetools.core.palette import parse_color

FLAGS = (  # (PaintOptions field, flag) of the valued options, in help order
	("mode", "--mode"), ("fit", "--fit"), ("offset_x", "--offset-x"), ("offset_y", "--offset-y"), ("scale", "--scale"),
	("rotate", "--rotate"), ("inset", "--inset"), ("strength", "--strength"), ("outline", "--outline"),
	("only_tol", "--only-tol"), ("keep_tol", "--keep-tol"), ("material_alpha", "--material-alpha"))
SWITCHES = (("tile", "--tile"), ("flip_h", "--flip-h"), ("flip_v", "--flip-v"), ("mask_invert", "--mask-invert"),
	("clip", "--clip"))
LISTS = (("only_colors", "--only"), ("keep_colors", "--keep"))


def parse_color_list(value):
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


def _hex(c):
	r, g, b = parse_color(c)
	return "#{0:02x}{1:02x}{2:02x}".format(r, g, b)


def cli_args(opts):
	"""The paint_tex.py flags for opts (defaults omitted), for "Copy as CLI"."""
	d, out = PaintOptions(), []
	for name, flag in FLAGS:
		if getattr(opts, name) != getattr(d, name):
			out += [flag, str(getattr(opts, name))]
	for name, flag in SWITCHES:
		if getattr(opts, name):
			out.append(flag)
	for name, flag in LISTS:
		if getattr(opts, name):
			out += [flag, ",".join(_hex(c) for c in getattr(opts, name))]
	return out


def build_parser():
	d = PaintOptions()
	p = argparse.ArgumentParser(description="Paint a material into a shape, keeping the shape's shading.")
	p.add_argument("inputs", nargs="+", help="SHAPE MATERIAL OUTPUT, or FIXED with --bulk")
	p.add_argument("--mode", choices=PAINT_MODES, default=d.mode, help="inplace (sampled per pixel) or projected")
	p.add_argument("--fit", choices=FITS, default=d.fit, help="projected: stretch, contain or cover the bounding box")
	p.add_argument("--offset-x", type=int, default=d.offset_x, metavar="N", help="Shift the material right")
	p.add_argument("--offset-y", type=int, default=d.offset_y, metavar="N", help="Shift the material down")
	p.add_argument("--scale", type=int, default=d.scale, metavar="N", help="inplace: material pixel covers NxN")
	p.add_argument("--tile", action="store_true", help="Wrap the material instead of clamping to its edge")
	p.add_argument("--flip-h", action="store_true", help="Flip the material left to right")
	p.add_argument("--flip-v", action="store_true", help="Flip the material top to bottom")
	p.add_argument("--rotate", type=int, choices=ROTATIONS, default=d.rotate, help="Rotate the material clockwise")
	p.add_argument("--inset", type=int, default=d.inset, metavar="N", help="Sample N px in from the material's edge")
	p.add_argument("--strength", type=float, default=d.strength, metavar="F", help="Shape shading kept, 0..1")
	p.add_argument("--outline", choices=OUTLINES, default=d.outline, help="shade the outline or keep it")
	p.add_argument("--mask", help="PNG whose alpha > 0 pixels are painted (default: the shape's opaque pixels)")
	p.add_argument("--mask-invert", action="store_true", help="Invert the mask")
	p.add_argument("--only", type=parse_color_list, default=[], metavar="C,C", help="Paint only these shape colors")
	p.add_argument("--only-tol", type=int, default=d.only_tol, metavar="N", help="Tolerance for --only")
	p.add_argument("--keep", type=parse_color_list, default=[], metavar="C,C", help="Never paint these shape colors")
	p.add_argument("--keep-tol", type=int, default=d.keep_tol, metavar="N", help="Tolerance for --keep")
	p.add_argument("--material-alpha", choices=MATERIAL_ALPHA, default=d.material_alpha,
		help="ignore the material's alpha, or skip its transparent pixels")
	p.add_argument("--clip", action="store_true", help="Unpainted pixels become transparent")
	p.add_argument("--bulk", action="store_true", help="One fixed image, every image in --in-dir")
	p.add_argument("--in-dir", help="Bulk folder (materials; shapes with --reverse)")
	p.add_argument("--out-dir", help="Bulk output folder")
	p.add_argument("--reverse", action="store_true", help="Bulk: FIXED is the material, --in-dir holds shapes")
	p.add_argument("--pattern", default="{name}.png",
		help="Output name; fields {name} {ext} {shape} {material} {n} (default {name}.png)")
	p.add_argument("--overwrite", choices=batch.OVERWRITE_POLICIES, default="replace", help="Existing output")
	p.add_argument("--include", action="append", help="Only files matching this glob (repeatable)")
	p.add_argument("--exclude", action="append", help="Leave out files matching this glob (repeatable)")
	p.add_argument("--recursive", action="store_true", help="Scan --in-dir recursively")
	p.add_argument("--dry-run", action="store_true", help="List outputs, write nothing")
	return p


def options_from_args(args):
	d = {name: getattr(args, name) for name, _ in FLAGS + SWITCHES}
	d["only_colors"], d["keep_colors"] = list(args.only), list(args.keep)
	return PaintOptions.from_dict(d)


def _need(path):
	if not os.path.isfile(path):
		sys.stderr.write("Error: file not found: {0}\n".format(path))
		sys.exit(1)


def run_single(args, opts):
	shape, material, output = args.inputs
	for p in (shape, material) + ((args.mask,) if args.mask else ()):
		_need(p)
	mask = load_rgba_array(args.mask) if args.mask else None
	try:
		out = paint(load_rgba_array(shape), load_rgba_array(material), opts, mask)
	except ValueError as e:
		sys.stderr.write("Error: {0}\n".format(e))
		sys.exit(1)
	save_raw(out, output)
	print("Saved: {0}".format(output))


def run_bulk(args, opts):
	fixed = args.inputs[0]
	_need(fixed)
	if args.mask:
		_need(args.mask)
	if not args.in_dir or not os.path.isdir(args.in_dir):
		sys.stderr.write("Error: --in-dir is not a directory: {0}\n".format(args.in_dir))
		sys.exit(1)
	if not args.out_dir:
		sys.stderr.write("Error: --bulk needs --out-dir\n")
		sys.exit(1)
	jobs = paint_jobs(fixed, args.in_dir, args.reverse, opts, args.mask, args.pattern, args.include, args.exclude,
		args.recursive)

	def show(r):
		if r.status in ("skipped", "exists", "error"):
			print("  SKIP  {0}: {1}".format(r.job.label, r.reason))
		else:
			print("  {0}  {1} -> {2}".format("OK  " if r.status == "written" else "DRY ", r.job.label, r.output))

	report = batch.run_batch(jobs, args.out_dir, dry_run=args.dry_run, overwrite=args.overwrite, on_result=show)
	c = report.counts()
	print("Done. {0} {1}, {2} skipped.".format(c.get("dry", 0) + c.get("written", 0),
		"would be written" if args.dry_run else "written", len(report.skipped) + len(report.errors)))


def main(argv=None):
	parser = build_parser()
	args = parser.parse_args(argv)
	want = 1 if args.bulk else 3
	if len(args.inputs) != want:
		parser.error("give FIXED with --bulk" if args.bulk else "give SHAPE MATERIAL OUTPUT")
	try:
		opts = options_from_args(args)
	except ValueError as e:
		parser.error(str(e))
	(run_bulk if args.bulk else run_single)(args, opts)


if __name__ == "__main__":
	main()

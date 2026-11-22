# SPDX-License-Identifier: GPL-3.0-only
"""
upscale_sprites.py

Batch-upscale every sprite/image in a folder by one or more integer factors
(x2, x4, x8, x16, ...) using nearest-neighbor scaling, which keeps pixel art
crisp (no blurring/anti-aliasing).

Usage:
    python upscale_sprites.py INPUT_DIR [OUTPUT_DIR] --factors 2 4 8 16

    # Examples:
    python upscale_sprites.py ./sprites
    python upscale_sprites.py ./sprites ./sprites_upscaled --factors 2 4
    python upscale_sprites.py ./sprites --factors 2 4 8 16 --recursive
    python upscale_sprites.py ./sprites --overwrite-flat  # no per-factor subfolders

Output layout (default):
    OUTPUT_DIR/x2/<same-filename>.png
    OUTPUT_DIR/x4/<same-filename>.png
    OUTPUT_DIR/x8/<same-filename>.png
    ...

Requires: Pillow  (pip install Pillow --break-system-packages)
"""

import argparse
import sys
from pathlib import Path

from texturetools.core.scale import *  # noqa: F401,F403 (the old module's names, for importers)
from texturetools.core.scale import (SUPPORTED_EXTS, find_images, upscale_factors, upscale_image, upscale_output_dir,
									 upscale_plan)


def main(argv=None):
	parser = argparse.ArgumentParser(
		description="Batch-upscale pixel art sprites by integer factors using nearest-neighbor scaling."
	)
	parser.add_argument("input_dir", type=Path, help="Folder containing source sprites")
	parser.add_argument(
		"output_dir",
		type=Path,
		nargs="?",
		default=None,
		help="Folder to write upscaled sprites into (default: <input_dir>_upscaled)",
	)
	parser.add_argument(
		"--factors",
		type=int,
		nargs="+",
		default=[2, 4, 8, 16],
		help="Upscale factors to generate (default: 2 4 8 16)",
	)
	parser.add_argument(
		"--recursive",
		action="store_true",
		help="Recurse into subfolders of input_dir",
	)
	parser.add_argument(
		"--flat",
		action="store_true",
		help="Write all outputs into output_dir directly with a '_x{factor}' filename "
		"suffix instead of creating x2/ x4/ x8/ subfolders",
	)

	args = parser.parse_args(argv)

	input_dir: Path = args.input_dir
	if not input_dir.is_dir():
		print(f"Error: input folder not found: {input_dir}")
		sys.exit(1)

	output_dir: Path = upscale_output_dir(input_dir, args.output_dir)

	factors = upscale_factors(args.factors)
	if not factors:
		print("Error: no valid factors > 1 given.")
		sys.exit(1)

	images = list(find_images(input_dir, args.recursive))
	if not images:
		print(f"No supported images found in {input_dir} (looked for {sorted(SUPPORTED_EXTS)})")
		sys.exit(0)

	print(f"Found {len(images)} sprite(s). Upscaling by factors: {factors}")
	print(f"Output folder: {output_dir}\n")

	total = 0
	for src_path, rel_path, factor, dest_path in upscale_plan(input_dir, output_dir, factors, args.recursive, args.flat):
		try:
			upscale_image(src_path, factor, dest_path)
			total += 1
			print(f"  [x{factor:<3}] {rel_path} -> {dest_path}")
		except Exception as e:
			print(f"  FAILED: {rel_path} (x{factor}): {e}")

	print(f"\nDone. Wrote {total} upscaled image(s) to {output_dir}")


if __name__ == "__main__":
	main()

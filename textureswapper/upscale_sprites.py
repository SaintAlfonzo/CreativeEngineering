#!/usr/bin/env python3
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

try:
    from PIL import Image
except ImportError:
    print("Pillow is required. Install it with:\n  pip install Pillow --break-system-packages")
    sys.exit(1)

SUPPORTED_EXTS = {".png", ".jpg", ".jpeg", ".bmp", ".gif", ".tga", ".webp"}


def find_images(input_dir: Path, recursive: bool):
    pattern = "**/*" if recursive else "*"
    for path in sorted(input_dir.glob(pattern)):
        if path.is_file() and path.suffix.lower() in SUPPORTED_EXTS:
            yield path


def upscale_image(src_path: Path, factor: int, dest_path: Path):
    with Image.open(src_path) as img:
        img = img.convert("RGBA") if img.mode != "RGBA" and "A" in img.mode else img
        new_size = (img.width * factor, img.height * factor)
        # NEAREST preserves hard pixel edges -- essential for pixel art sprites.
        upscaled = img.resize(new_size, resample=Image.NEAREST)
        dest_path.parent.mkdir(parents=True, exist_ok=True)
        upscaled.save(dest_path)


def main():
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

    args = parser.parse_args()

    input_dir: Path = args.input_dir
    if not input_dir.is_dir():
        print(f"Error: input folder not found: {input_dir}")
        sys.exit(1)

    output_dir: Path = args.output_dir or input_dir.parent / f"{input_dir.name}_upscaled"

    factors = sorted(set(f for f in args.factors if f > 1))
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
    for src_path in images:
        rel_path = src_path.relative_to(input_dir)
        for factor in factors:
            if args.flat:
                dest_path = output_dir / f"{rel_path.stem}_x{factor}{rel_path.suffix}"
            else:
                dest_path = output_dir / f"x{factor}" / rel_path

            try:
                upscale_image(src_path, factor, dest_path)
                total += 1
                print(f"  [x{factor:<3}] {rel_path} -> {dest_path}")
            except Exception as e:
                print(f"  FAILED: {rel_path} (x{factor}): {e}")

    print(f"\nDone. Wrote {total} upscaled image(s) to {output_dir}")


if __name__ == "__main__":
    main()

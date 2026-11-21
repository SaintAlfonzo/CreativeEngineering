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


def parse_size(text):
    parts = text.lower().split("x")
    if len(parts) == 1:
        parts = parts * 2
    if len(parts) != 2:
        raise ValueError(text)
    return int(parts[0]), int(parts[1])


def resolve_factor(src_size, factor=None, size=None):
    """Return the integer factor, or raise ValueError with a reason."""
    w, h = src_size
    if factor is not None:
        if factor < 1:
            raise ValueError("factor must be 1 or more")
        return factor
    tw, th = size
    if tw % w or th % h:
        raise ValueError(
            "{0}x{1} is not a whole-number multiple of {2}x{3}".format(tw, th, w, h)
        )
    fx, fy = tw // w, th // h
    if fx != fy:
        raise ValueError(
            "{0}x{1} would scale the sides unevenly ({2}x and {3}x)".format(tw, th, fx, fy)
        )
    if fx < 1:
        raise ValueError("target is smaller than the source")
    return fx


def scale_image(img, factor):
    w, h = img.size
    out = img.resize((w * factor, h * factor), Image.NEAREST)
    out.info = {}
    if "transparency" in img.info:  # palette transparency index
        out.info["transparency"] = img.info["transparency"]
    return out


def trim(img):
    box = img.convert("RGBA").split()[3].getbbox()
    return img.crop(box) if box else img


def fit_factor(src_size, canvas):
    f = min(canvas[0] // src_size[0], canvas[1] // src_size[1])
    if f < 1:
        raise ValueError("source is larger than the canvas")
    return f


def place_on_canvas(img, canvas, anchor):
    cw, ch = canvas
    w, h = img.size
    if w > cw or h > ch:
        raise ValueError("scaled image {0}x{1} is larger than the canvas {2}x{3}".format(w, h, cw, ch))
    x, y = ((cw - w) // 2, (ch - h) // 2) if anchor == "center" else (0, 0)
    out = Image.new("RGBA", canvas, (0, 0, 0, 0))
    out.paste(img.convert("RGBA"), (x, y))
    return out


def default_output(path, size):
    stem, _ = os.path.splitext(path)
    w, h = size
    tag = str(w) if w == h else "{0}x{1}".format(w, h)
    return "{0}_{1}.png".format(stem, tag)


def main():
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
    args = parser.parse_args()
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

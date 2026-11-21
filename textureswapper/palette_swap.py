"""
palette_swap.py - swap the palette of a source image to match a target palette image.

Usage (single file):
    python palette_swap.py <source> <palette> <output> [--invert] [--drop CLASS,...] [--interpolate] [--preview]

Usage (bulk):
    python palette_swap.py <shape> --bulk [--invert] [--drop CLASS,...] [--interpolate] [--preview]

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
    --drop      Comma-separated colour classes to leave out of the palette
                before mapping: brown, white (see DROP_CLASSES). Ignored for a
                palette where it would leave no colours.
    --drop-darkest N
                Leave the N darkest colours out of the palette (e.g. an item's
                outline, so a soft block template stays soft). Ignored when it
                would leave no colours.
    --drop-lightest N
                The same for the N lightest colours (applied after --drop-darkest).
    --interpolate
                When the palette has fewer unique colours than the image being
                recoloured, force an even gradient: the source shades, ranked by
                brightness, are spaced evenly from the palette's first colour to
                its last, blending between neighbouring palette colours, instead
                of several source shades snapping onto one palette colour. Keeps
                shading from a near-uniform palette or a gradient. No effect when
                the palette has as many colours or more, or only one.
    --preview   Also save a 160x160 upscaled preview alongside each output.

Examples:
    python palette_swap.py wire_aluminum.png ingot_silver.png silver_wire.png --invert
    python palette_swap.py gold_nugget.png rose_gold_ingot.png rose_gold_nugget.png --invert
    python palette_swap.py wire_aluminum.png --bulk --invert --preview
    python palette_swap.py strad_wine.png creeper_wine.png creeper_box.png --drop brown
    python palette_swap.py gradient_block.png flat_block.png out.png --interpolate
"""

import argparse
import colorsys
import sys
import os
from PIL import Image
import numpy as np

# ---------------------------------------------------------------------------
# Bulk mode folders -- edit these to point at your own source/output folders.
# ALL image files in BULK_SOURCE_DIR are processed when --bulk is used.
# ---------------------------------------------------------------------------
BULK_SOURCE_DIR = "./in"
BULK_OUTPUT_DIR = "./out"

IMAGE_EXTENSIONS = (".png", ".bmp", ".tga", ".gif", ".webp")

# --drop classes, tested on (hue degrees, saturation, value). The Vinery port's
# BottleLooks.Drop uses the same bounds -- keep the two in step.
DROP_CLASSES = {
    "brown": lambda h, s, v: 10 <= h <= 45 and s >= 0.4 and v <= 0.7,
    "white": lambda h, s, v: s <= 0.15 and v >= 0.85,
}


def drop_colors(colors, drops):
    """Leave out colors matching any of the named DROP_CLASSES (all kept if none would remain)."""
    if not drops:
        return colors

    def dropped(c):
        h, s, v = colorsys.rgb_to_hsv(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)
        return any(DROP_CLASSES[d](h * 360, s, v) for d in drops)

    kept = [c for c in colors if not dropped(c)]
    return kept or colors


def parse_drops(value):
    names = [n.strip().lower() for n in value.split(",") if n.strip()]
    for n in names:
        if n not in DROP_CLASSES:
            raise argparse.ArgumentTypeError(
                "unknown colour class {0!r} (known: {1})".format(n, ", ".join(sorted(DROP_CLASSES)))
            )
    return names


def get_colors_by_brightness(arr, descending=True):
    """Return unique opaque colors sorted by brightness (sum of RGB channels)."""
    pixels = arr.reshape(-1, 4)
    colors = set(map(tuple, pixels[pixels[:, 3] > 0].tolist()))
    return sorted(
        colors, key=lambda c: int(c[0]) + int(c[1]) + int(c[2]), reverse=descending
    )


def lerp_palette(palette_colors, t):
    """Sample the palette as a continuous gradient at t in [0, 1] (RGBA blended linearly)."""
    pos = t * (len(palette_colors) - 1)
    lo = int(pos)
    hi = min(lo + 1, len(palette_colors) - 1)
    f = pos - lo
    a, b = palette_colors[lo], palette_colors[hi]
    return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(4))


def palette_swap(src_arr, palette_colors, interpolate=False):
    """
    Remap every opaque color in src_arr to the nearest palette color by brightness rank.
    Transparent pixels are left untouched.

    interpolate: when the palette has fewer colors than src_arr, spread the source
    shades as an even gradient across the palette: rank each distinct source
    brightness, give rank i of n the position i/(n-1), and blend the two neighbouring
    palette colors there. Every source shade keeps its own output shade, evenly
    stepped (gradients, near-uniform palettes).

    Raises ValueError if src_arr has no opaque pixels, or if palette_colors is empty.
    """
    src_colors = get_colors_by_brightness(src_arr)
    if not src_colors:
        raise ValueError("source image has no opaque pixels to recolor")

    n_palette = len(palette_colors)
    if n_palette == 0:
        raise ValueError("palette image has no opaque pixels to sample colors from")

    def brightness(c):
        return int(c[0]) + int(c[1]) + int(c[2])

    src_bright = [brightness(c) for c in src_colors]
    src_min = min(src_bright)
    src_max = max(src_bright)

    blend = interpolate and 1 < n_palette < len(src_colors)
    # Even gradient: each distinct source brightness level gets an evenly spaced
    # step along the palette by rank, however unevenly the levels are spaced.
    levels = sorted(set(src_bright))
    level_rank = dict((b, i) for i, b in enumerate(levels))

    def map_color(b):
        if blend:
            t = level_rank[b] / float(len(levels) - 1) if len(levels) > 1 else 0.5
            return lerp_palette(palette_colors, t)
        t = (b - src_min) / float(src_max - src_min) if src_max != src_min else 0.5
        idx = int(round(t * (n_palette - 1)))
        idx = max(0, min(n_palette - 1, idx))  # clamp defensively
        return palette_colors[idx]

    color_map = dict((c, map_color(brightness(c))) for c in src_colors)

    result_arr = src_arr.copy()
    for src, dst in color_map.items():
        mask = (
            (src_arr[:, :, 0] == src[0])
            & (src_arr[:, :, 1] == src[1])
            & (src_arr[:, :, 2] == src[2])
            & (src_arr[:, :, 3] == src[3])
        )
        result_arr[mask] = dst

    return result_arr


def load_palette_colors(pal_path, invert, drops=(), drop_darkest=0, drop_lightest=0):
    """Load a palette image once and return its brightness-ordered color list."""
    pal_img = Image.open(pal_path).convert("RGBA")
    pal_arr = np.array(pal_img)
    palette_colors = drop_colors(get_colors_by_brightness(pal_arr), drops)
    if 0 < drop_darkest < len(palette_colors):
        palette_colors = palette_colors[:-drop_darkest]
    if 0 < drop_lightest < len(palette_colors):
        palette_colors = palette_colors[drop_lightest:]
    if not invert:
        palette_colors = list(reversed(palette_colors))
    return pal_img, palette_colors


def process_one(src_arr, palette_colors, out_path, preview, interpolate=False):
    """Apply palette_colors onto an already-loaded src_arr and save the result.
    Raises on failure; caller decides how to handle."""
    result_arr = palette_swap(src_arr, palette_colors, interpolate)
    result_img = Image.fromarray(result_arr, "RGBA")

    out_dir = os.path.dirname(out_path)
    if out_dir and not os.path.exists(out_dir):
        os.makedirs(out_dir, exist_ok=True)

    _, out_ext = os.path.splitext(out_path)
    if out_ext.lower() in (".jpg", ".jpeg"):
        # JPEG has no alpha channel; flatten onto white rather than crashing.
        result_img = result_img.convert("RGB")

    result_img.save(out_path)

    if preview:
        base, ext = os.path.splitext(out_path)
        preview_path = base + "_preview" + ext
        preview_img = result_img.convert("RGBA") if result_img.mode != "RGBA" else result_img
        preview_img.resize((160, 160), Image.NEAREST).save(preview_path)
        return out_path, preview_path

    return out_path, None


def run_single(args):
    src_path, pal_path, out_path = args.source, args.palette, args.output

    if not os.path.exists(src_path):
        sys.stderr.write("Error: source file not found: {0}\n".format(src_path))
        sys.exit(1)
    if not os.path.exists(pal_path):
        sys.stderr.write("Error: palette file not found: {0}\n".format(pal_path))
        sys.exit(1)

    pal_img, palette_colors = load_palette_colors(pal_path, args.invert, args.drop, args.drop_darkest, args.drop_lightest)
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
        print("Interpolate: on (even gradient when the palette has fewer colours than the shape)")
    if args.drop:
        print("Drop:    {0}".format(", ".join(args.drop)))

    try:
        out_path, preview_path = process_one(src_arr, palette_colors, out_path, args.preview, args.interpolate)
    except ValueError as e:
        sys.stderr.write("Error: {0}\n".format(e))
        sys.exit(1)

    print("Saved:   {0}".format(out_path))
    if preview_path:
        print("Preview: {0}".format(preview_path))


def run_bulk(args):
    shape_path, src_dir, out_dir = args.shape, BULK_SOURCE_DIR, BULK_OUTPUT_DIR

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
        print("Interpolate: on (even gradient when the palette has fewer colours than the shape)")
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
            _, palette_colors = load_palette_colors(item_path, args.invert, args.drop, args.drop_darkest, args.drop_lightest)
            saved_path, preview_path = process_one(shape_arr, palette_colors, out_path, args.preview, args.interpolate)
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


def main():
    # --bulk mode ignores source/output folders entirely and instead uses
    # BULK_SOURCE_DIR / BULK_OUTPUT_DIR defined near the top of this file, so
    # its CLI signature is just: palette_swap.py <palette> --bulk [flags]
    if "--bulk" in sys.argv:
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
            help="Leave the N lightest colours out of each palette",
        )
        parser.add_argument(
            "--drop-darkest", type=int, default=0, metavar="N",
            help="Leave the N darkest colours out of each palette (e.g. an outline)",
        )
        parser.add_argument(
            "--drop",
            type=parse_drops,
            default=[],
            help="Comma-separated colour classes to leave out of each palette: " + ", ".join(sorted(DROP_CLASSES)),
        )
        parser.add_argument(
            "--interpolate",
            action="store_true",
            help="Even gradient across each palette when it has fewer colours than <shape>.",
        )
        parser.add_argument(
            "--preview",
            action="store_true",
            help="Also save a 160x160 upscaled preview alongside each output.",
        )
        args = parser.parse_args()
        run_bulk(args)
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
        help="Leave the N lightest colours out of the palette",
    )
    parser.add_argument(
        "--drop-darkest", type=int, default=0, metavar="N",
        help="Leave the N darkest colours out of the palette (e.g. an outline)",
    )
    parser.add_argument(
        "--drop",
        type=parse_drops,
        default=[],
        help="Comma-separated colour classes to leave out of the palette: " + ", ".join(sorted(DROP_CLASSES)),
    )
    parser.add_argument(
        "--interpolate",
        action="store_true",
        help="Even gradient across the palette when it has fewer colours than the source.",
    )
    parser.add_argument(
        "--preview",
        action="store_true",
        help="Also save a 160x160 upscaled preview alongside each output.",
    )
    args = parser.parse_args()
    run_single(args)


if __name__ == "__main__":
    main()

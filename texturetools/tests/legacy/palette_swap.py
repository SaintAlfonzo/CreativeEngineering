# SPDX-License-Identifier: GPL-3.0-only
# FROZEN 2026-11-22 (commit 0315051d): textureswapper/palette_swap.py before the move to texturetools (P0). The oracle
# for the parity tests; never edit.
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
                "unknown color class {0!r} (known: {1})".format(n, ", ".join(sorted(DROP_CLASSES)))
            )
    return names


def get_colors_by_brightness(arr, descending=True):
    """Return unique opaque colors sorted by brightness (sum of RGB channels)."""
    pixels = arr.reshape(-1, 4)
    colors = set(map(tuple, pixels[pixels[:, 3] > 0].tolist()))
    return sorted(
        colors, key=lambda c: int(c[0]) + int(c[1]) + int(c[2]), reverse=descending
    )


def color_counts(arr):
    """Pixel count of every opaque color in arr, as {(r, g, b, a): count}."""
    pixels = arr.reshape(-1, 4)
    pixels = pixels[pixels[:, 3] > 0]
    if not len(pixels):
        return {}
    uniq, counts = np.unique(pixels, axis=0, return_counts=True)
    return dict((tuple(int(v) for v in c), int(n)) for c, n in zip(uniq, counts))


def lerp_palette(palette_colors, t):
    """Sample the palette as a continuous gradient at t in [0, 1] (RGBA blended linearly)."""
    pos = t * (len(palette_colors) - 1)
    lo = int(pos)
    hi = min(lo + 1, len(palette_colors) - 1)
    f = pos - lo
    a, b = palette_colors[lo], palette_colors[hi]
    return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(4))


def palette_bins(palette_colors, n_bins):
    """Split the ordered palette into n_bins contiguous runs, 1 < n_bins <= len(palette): run i holds the colors
    nearest the even position i/(n_bins-1) along the palette (end runs are half width, so the palette's first and
    last colors anchor the ends). Returns [(run colors, index of the color at the position within the run)]."""
    n = len(palette_colors)
    runs = [[] for _ in range(n_bins)]
    for k, c in enumerate(palette_colors):
        runs[int(round(k * (n_bins - 1) / float(n - 1)))].append((k, c))
    out = []
    for i, run in enumerate(runs):
        center = i * (n - 1) / float(n_bins - 1)
        ks = [k for k, _ in run]
        out.append(([c for _, c in run], min(range(len(ks)), key=lambda j: (abs(ks[j] - center), ks[j]))))
    return out


def bin_mean(colors, center, counts):
    """Pixel-weighted mean color of one palette run (each color weight 1 when counts are unknown)."""
    w = [float(counts.get(tuple(c), 1)) if counts else 1.0 for c in colors]
    total = sum(w)
    return tuple(int(round(sum(c[i] * wi for c, wi in zip(colors, w)) / total)) for i in range(4))


def bin_pick(colors, center, counts):
    """The most-used real color of one palette run (the color at the run's position on a tie or without counts)."""
    if not counts:
        return tuple(colors[center])
    order = sorted(range(len(colors)), key=lambda i: (-counts.get(tuple(colors[i]), 0), abs(i - center), i))
    return tuple(colors[order[0]])


def palette_swap(src_arr, palette_colors, interpolate=False, even=False, palette_counts=None):
    """
    Remap every opaque color in src_arr to a palette color by brightness.
    Transparent pixels are left untouched. palette_colors is ordered darkest-first
    for a plain swap (load_palette_colors does that), lightest-first with --invert.

    Default (even=False, interpolate=False): legacy rank map, unchanged. Each source
    color's brightness VALUE is scaled onto the palette index, so bunched source
    shades land on bunched palette entries and a long palette is sampled sparsely.

    The source shades are first collapsed to their distinct brightness levels
    (n of them), ranked darkest to lightest. Rank i of n sits at t = i/(n-1).

    even: even rank spacing. Rank i takes palette index round(t * (len-1)), so the
    shades spread evenly over the palette however unevenly their brightness is
    spaced. When the palette has more colors than the source has levels, the
    palette is split into n contiguous runs, run i around palette position t, and
    level i takes the most-used real color of run i (palette_counts; the color at
    t without counts, which is the plain even pick), so no stretch of the palette
    is skipped, including colors whose hue breaks the brightness order (a dark
    red run among browns).

    interpolate: blend instead of picking. A palette with fewer colors than the
    source has colors is sampled as a gradient at t (unchanged); otherwise (palette
    as large as the source or larger, which used to fall back to the legacy map)
    the palette is split into n runs as above and level i takes run i's
    pixel-weighted mean color. Even rank spacing either way.

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

    legacy_blend = interpolate and 1 < n_palette < len(src_colors)
    levels = sorted(set(src_bright))
    n_levels = len(levels)
    level_rank = dict((b, i) for i, b in enumerate(levels))

    def rank_t(b):
        return level_rank[b] / float(n_levels - 1) if n_levels > 1 else 0.5

    binned = None
    # Legacy blend (interpolate, palette smaller than the source's color count) keeps its output.
    if (even or interpolate) and not legacy_blend and n_palette >= n_levels > 1:
        bins = palette_bins(palette_colors, n_levels)
        pick = bin_mean if interpolate else bin_pick
        binned = [pick(run, center, palette_counts) for run, center in bins]

    def map_color(b):
        if binned is not None:
            return binned[level_rank[b]]
        if legacy_blend:
            return lerp_palette(palette_colors, rank_t(b))
        if even:
            idx = int(round(rank_t(b) * (n_palette - 1)))
            return palette_colors[max(0, min(n_palette - 1, idx))]
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


def load_palette_colors(pal_path, invert, drops=(), drop_darkest=0, drop_lightest=0, with_counts=False):
    """Load a palette image once and return its brightness-ordered color list
    (and, with with_counts, a third value: {color: pixel count} for --even / --interpolate)."""
    pal_img = Image.open(pal_path).convert("RGBA")
    pal_arr = np.array(pal_img)
    palette_colors = drop_colors(get_colors_by_brightness(pal_arr), drops)
    if 0 < drop_darkest < len(palette_colors):
        palette_colors = palette_colors[:-drop_darkest]
    if 0 < drop_lightest < len(palette_colors):
        palette_colors = palette_colors[drop_lightest:]
    if not invert:
        palette_colors = list(reversed(palette_colors))
    if with_counts:
        return pal_img, palette_colors, color_counts(pal_arr)
    return pal_img, palette_colors


def process_one(src_arr, palette_colors, out_path, preview, interpolate=False, even=False, palette_counts=None):
    """Apply palette_colors onto an already-loaded src_arr and save the result.
    Raises on failure; caller decides how to handle."""
    result_arr = palette_swap(src_arr, palette_colors, interpolate, even, palette_counts)
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
    args = parser.parse_args()
    run_single(args)


if __name__ == "__main__":
    main()

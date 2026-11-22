# SPDX-License-Identifier: GPL-3.0-only
# FROZEN 2026-11-22 (commit 0315051d): textureswapper/combine_tex.py before the move to texturetools (P0). The oracle
# for the parity tests; never edit.
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
    python combine_textures.py wooden_bucket.png milk_bucket.png out.png --mode paint \
        --mask masks/milk_fill.png --strength 0
    python combine_textures.py packart/filtered/background/blueprint_large.png \
        packart/filtered/icon/book.png out.png --lcm
"""

import argparse
import math
import sys
import os
from PIL import Image
import numpy as np


def load_rgba_array(path):
    img = Image.open(path).convert("RGBA")
    return np.array(img).astype(np.float32)


def combine_alpha(base_arr, overlay_arr):
    """Standard 'over' alpha compositing, straight (non-premultiplied) alpha,
    done directly on raw stored values -- no gamma linearization."""
    base_rgb, base_a = base_arr[:, :, :3], base_arr[:, :, 3:4] / 255.0
    over_rgb, over_a = overlay_arr[:, :, :3], overlay_arr[:, :, 3:4] / 255.0

    out_a = over_a + base_a * (1 - over_a)
    # Avoid divide-by-zero where out_a is 0 (fully transparent result)
    safe_a = np.where(out_a == 0, 1, out_a)
    out_rgb = (over_rgb * over_a + base_rgb * base_a * (1 - over_a)) / safe_a

    result = np.concatenate([out_rgb, out_a * 255.0], axis=2)
    return result


def combine_add(base_arr, overlay_arr):
    """Additive blend on RGB, alpha combined via standard 'over'."""
    result = combine_alpha(base_arr, overlay_arr)
    added_rgb = np.clip(base_arr[:, :, :3] + overlay_arr[:, :, :3], 0, 255)
    result[:, :, :3] = added_rgb
    return result


def combine_multiply(base_arr, overlay_arr):
    """Multiplicative blend on RGB (darkens), alpha combined via standard 'over'."""
    result = combine_alpha(base_arr, overlay_arr)
    mult_rgb = (base_arr[:, :, :3] / 255.0) * (overlay_arr[:, :, :3] / 255.0) * 255.0
    result[:, :, :3] = mult_rgb
    return result


def load_mask(path, base_arr):
    """Boolean paint mask: alpha > 0 in the mask PNG, or the base's opaque pixels."""
    if path is None:
        return base_arr[:, :, 3] > 0
    m = load_rgba_array(path)
    if m.shape[:2] != base_arr.shape[:2]:
        raise ValueError("mask {0} is {1}, base is {2}".format(path, m.shape[:2][::-1], base_arr.shape[:2][::-1]))
    return m[:, :, 3] > 0


def combine_paint(base_arr, overlay_arr, mask=None, strength=1.0, inset=0, clip=False):
    """Paint-on: overlay painted into the base's masked pixels, keeping the base's
    luminance relative to the masked mean (strength 0..1). See the docstring."""
    if mask is None:
        mask = base_arr[:, :, 3] > 0
    h, w = base_arr.shape[:2]
    ys = np.clip(np.arange(h), inset, h - 1 - inset)
    xs = np.clip(np.arange(w), inset, w - 1 - inset)
    paint = overlay_arr[ys][:, xs]
    lum = base_arr[:, :, 0] * 0.299 + base_arr[:, :, 1] * 0.587 + base_arr[:, :, 2] * 0.114
    mean = lum[mask].mean() if mask.any() else 1.0
    f = 1.0 + (lum / (mean or 1.0) - 1.0) * strength
    result = np.zeros(base_arr.shape, dtype=np.float32) if clip else base_arr.copy()
    result[mask, :3] = paint[mask, :3] * f[mask][:, None]
    result[mask, 3] = base_arr[mask, 3]
    return result


def lcm_size(*arrs):
    """(height, width) that every array's size divides evenly."""
    h = w = 1
    for a in arrs:
        h = h * a.shape[0] // math.gcd(h, a.shape[0])
        w = w * a.shape[1] // math.gcd(w, a.shape[1])
    return h, w


def scale_to(arr, size):
    """Nearest-neighbour upscale arr to size (h, w) by one whole-number
    factor. Returns (scaled, factor), or (None, None) if the two axes would
    need different factors."""
    fy, fx = size[0] // arr.shape[0], size[1] // arr.shape[1]
    if fy != fx:
        return None, None
    return np.repeat(np.repeat(arr, fy, axis=0), fx, axis=1), fy


def lcm_pair(base_arr, overlay_arr):
    """Scale both arrays to their lcm size. Returns (base, overlay, info) or
    raises ValueError naming the image that would be stretched."""
    size = lcm_size(base_arr, overlay_arr)
    b, bf = scale_to(base_arr, size)
    o, of = scale_to(overlay_arr, size)
    for name, arr, f in (("base", base_arr, bf), ("overlay", overlay_arr, of)):
        if f is None:
            raise ValueError(
                "{0} {1} would need different x and y factors to reach {2}".format(
                    name, arr.shape[:2][::-1], size[::-1]
                )
            )
    return b, o, "{0}x{1} (base {2}x, overlay {3}x)".format(size[1], size[0], bf, of)


def center_overlay(base_arr, overlay_arr):
    """Pad overlay_arr with transparent pixels so it sits centred on a canvas
    the size of base_arr. Returns None if the overlay is larger than the base
    on either axis."""
    bh, bw = base_arr.shape[:2]
    oh, ow = overlay_arr.shape[:2]
    if oh > bh or ow > bw:
        return None
    canvas = np.zeros(base_arr.shape, dtype=np.float32)
    y, x = (bh - oh) // 2, (bw - ow) // 2
    canvas[y:y + oh, x:x + ow] = overlay_arr
    return canvas


def fit_overlay(base_arr, overlay_arr, center):
    """Return overlay matched to the base size, or None if it can't be."""
    if overlay_arr.shape == base_arr.shape:
        return overlay_arr
    return center_overlay(base_arr, overlay_arr) if center else None


MODES = {
    "alpha": combine_alpha,
    "add": combine_add,
    "multiply": combine_multiply,
    "paint": combine_paint,
}


IMAGE_EXTS = {".png", ".jpg", ".jpeg", ".bmp", ".tga", ".gif", ".webp"}


def save_raw(result_arr, output):
    """Write raw pixel data only -- no gAMA/sRGB/ICC chunks get carried over,
    so colors stay exactly as computed."""
    result_arr = np.clip(result_arr, 0, 255).astype(np.uint8)
    result_img = Image.new("RGBA", (result_arr.shape[1], result_arr.shape[0]))
    result_img.putdata([tuple(p) for p in result_arr.reshape(-1, 4)])
    result_img.save(output)


def combine(mode, base_arr, overlay_arr, paint_opts=None):
    """Run one mode; paint_opts = dict(mask=path, strength=, inset=, clip=) for paint."""
    if mode != "paint":
        return MODES[mode](base_arr, overlay_arr)
    o = dict(paint_opts or {})
    o["mask"] = load_mask(o.get("mask"), base_arr)
    return combine_paint(base_arr, overlay_arr, **o)


def combine_dir(base, overlay_dir, output_dir, pattern="{name}.png", mode="alpha", center=False, lcm=False,
                paint_opts=None):
    """Combine base with every image in overlay_dir, saving each result to
    output_dir under pattern.format(name=, ext=, base=). Returns the number
    of files written."""
    base_arr = load_rgba_array(base)
    base_stem = os.path.splitext(os.path.basename(base))[0]
    os.makedirs(output_dir, exist_ok=True)

    written = 0
    for fname in sorted(os.listdir(overlay_dir)):
        src = os.path.join(overlay_dir, fname)
        stem, ext = os.path.splitext(fname)
        if not os.path.isfile(src) or ext.lower() not in IMAGE_EXTS:
            continue
        overlay_arr = load_rgba_array(src)
        pair_base = base_arr
        if lcm:
            try:
                pair_base, overlay_arr, info = lcm_pair(base_arr, overlay_arr)
            except ValueError as e:
                sys.stderr.write("Skip:    {0}: {1}\n".format(src, e))
                continue
        fitted = fit_overlay(pair_base, overlay_arr, center)
        if fitted is None:
            sys.stderr.write(
                "Skip:    {0} is {1}, base is {2}\n".format(
                    src, overlay_arr.shape[:2][::-1], base_arr.shape[:2][::-1]
                )
            )
            continue
        out = os.path.join(output_dir, pattern.format(name=stem, ext=ext[1:], base=base_stem))
        save_raw(combine(mode, pair_base, fitted, paint_opts), out)
        print("Saved:   {0}{1}".format(out, "  " + info if lcm else ""))
        written += 1

    print("Mode:    {0}".format(mode))
    print("Base:    {0}".format(base))
    print("Wrote {0} file(s) to {1}".format(written, output_dir))
    return written


def main():
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
    args = parser.parse_args()
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

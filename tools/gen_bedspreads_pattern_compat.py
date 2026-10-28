#!/usr/bin/env python3
"""
Generates creativeengineering:entity/<namespace>/<pattern> compat textures for every
registered banner pattern that bedspreads' own DecoratedBedBlockEntityRenderer doesn't
already bundle a hand-tuned texture for.

Why: bedspreads renders its "quilt pattern" overlay using vanilla's own bed ModelPart
(headboard/footboard/mattress geometry), NOT a banner-flag model. Its curated compat
textures (assets/bedspreads/textures/entity/<ns>/<id>.png) are hand-redrawn/recentered
copies of each pattern's glyph, repositioned to land correctly on that geometry's UV
layout. A pattern's *real* entity/banner/<id>.png (banner-flag-shaped UV) lands in a
totally different, unrelated spot on the bed model -> offset/mangled rendering.

This script empirically derives bedspreads' placement convention by measuring its own
bundled textures (all emblem-style patterns bottom-anchored at x=14 with bottom edge
~y=42, or full-bleed patterns filling roughly the (2,14)-(38,44) box) and reapplies the
same transform to every other installed mod's real pattern texture, so third-party
patterns land in approximately the same place bedspreads' hand-tuned ones do.

Run from the instance root:
    python3 gen_bedspreads_pattern_compat.py
"""
import glob
import os
import re
import sys
import zipfile
from io import BytesIO

from PIL import Image
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pack_paths  # noqa: E402

INSTANCE_ROOT = str(pack_paths.INSTANCE)
MODS_DIR = os.path.join(INSTANCE_ROOT, "mods")
BEDSPREADS_JAR = os.path.join(MODS_DIR, "bedspreads-forge-6.0.0+1.19.2.jar")
VANILLA_JAR = pack_paths.vanilla_jar()
OUT_ROOT = os.path.join(
    INSTANCE_ROOT,
    "development/patcherMod/src/main/resources/assets/creativeengineering/textures/entity",
)

PATTERN_RE = re.compile(r"^assets/([a-z0-9_.\-]+)/textures/entity/banner/([a-z0-9_./\-]+)\.png$")

# Empirically measured from bedspreads' own bundled compat textures (minecraft/,
# alexsmobs/, iceandfire/, rats/, additionalbanners/).
FULL_BLEED_BOX = (2, 14, 38, 44)          # (x0, y0, x1, y1) target box for wide/geometric patterns
FULL_BLEED_WIDTH_THRESHOLD = 24            # natural glyph width >= this -> "full-bleed" classification
EMBLEM_MAX_W = 14
EMBLEM_MAX_H = 20
EMBLEM_CENTER_X = 14
EMBLEM_BOTTOM_Y = 42

CANVAS = 64


def first_glyph_bbox(im: Image.Image):
    """Isolate the first (leftmost) connected blob of non-transparent pixels.

    Emblem-style patterns are drawn twice (front/back banner faces) side by side;
    full-bleed geometric patterns are drawn once spanning nearly the whole canvas.
    Either way the first blob is one clean instance of the pattern's real art.
    """
    arr = np.array(im.convert("RGBA"))
    alpha = arr[:, :, 3]
    cols = np.where((alpha > 0).any(axis=0))[0]
    if len(cols) == 0:
        return None
    gaps = np.where(np.diff(cols) > 1)[0]
    first_end = cols[gaps[0]] if len(gaps) else cols[-1]
    first_start = cols[0]
    sub = alpha[:, first_start:first_end + 1]
    rows = np.where((sub > 0).any(axis=1))[0]
    return (int(first_start), int(rows[0]), int(first_end), int(rows[-1]))


def fit_scale(natural_w, natural_h, max_w, max_h):
    return min(max_w / natural_w, max_h / natural_h, 1.0)


def recentered_pattern(png_bytes: bytes):
    im = Image.open(BytesIO(png_bytes)).convert("RGBA")
    if im.size != (CANVAS, CANVAS):
        im = im.resize((CANVAS, CANVAS), Image.NEAREST)

    bbox = first_glyph_bbox(im)
    if bbox is None:
        return im  # fully transparent source, nothing to reposition

    x0, y0, x1, y1 = bbox
    glyph = im.crop((x0, y0, x1 + 1, y1 + 1))
    natural_w, natural_h = glyph.size

    if natural_w >= FULL_BLEED_WIDTH_THRESHOLD:
        # bedspreads doesn't preserve aspect ratio for these - it stretches the source glyph to
        # exactly fill its target box (confirmed by measuring its own bundled border.png: content
        # fills (2,14)-(38,44) exactly, a different w/h ratio than the 42x41 vanilla source). A
        # uniform, aspect-preserving fit centers the result with a few px of margin instead,
        # which reads as "shifted" next to bedspreads' own edge-to-edge patterns.
        bx0, by0, bx1, by1 = FULL_BLEED_BOX
        box_w, box_h = bx1 - bx0, by1 - by0
        resized = glyph.resize((box_w, box_h), Image.LANCZOS)
        paste_x, paste_y = bx0, by0
    else:
        scale = fit_scale(natural_w, natural_h, EMBLEM_MAX_W, EMBLEM_MAX_H)
        new_w, new_h = max(1, round(natural_w * scale)), max(1, round(natural_h * scale))
        resized = glyph.resize((new_w, new_h), Image.LANCZOS)
        paste_x = EMBLEM_CENTER_X - new_w // 2
        paste_y = EMBLEM_BOTTOM_Y - new_h

    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    canvas.alpha_composite(resized, (paste_x, paste_y))
    return canvas


def load_bedspreads_covered():
    covered = set()
    with zipfile.ZipFile(BEDSPREADS_JAR) as zf:
        for name in zf.namelist():
            m = re.match(r"^assets/bedspreads/textures/entity/([a-z0-9_.\-]+)/([a-z0-9_./\-]+)\.png$", name)
            if m:
                covered.add((m.group(1), m.group(2)))
    return covered


def iter_mod_jars():
    for path in sorted(glob.glob(os.path.join(MODS_DIR, "*.jar"))):
        if os.path.samefile(path, BEDSPREADS_JAR):
            continue
        yield path
    if VANILLA_JAR:
        yield str(VANILLA_JAR)


def main():
    covered = load_bedspreads_covered()
    print(f"bedspreads already covers {len(covered)} patterns")

    generated = 0
    skipped_covered = 0
    for jar_path in iter_mod_jars():
        try:
            zf = zipfile.ZipFile(jar_path)
        except (zipfile.BadZipFile, FileNotFoundError):
            continue
        with zf:
            for name in zf.namelist():
                m = PATTERN_RE.match(name)
                if not m:
                    continue
                namespace, pattern_id = m.group(1), m.group(2)
                if "/" in pattern_id:
                    continue  # not a real registered pattern (e.g. supplementaries' own "flags/" mirror)
                if (namespace, pattern_id) in covered:
                    skipped_covered += 1
                    continue
                out_path = os.path.join(OUT_ROOT, namespace, f"{pattern_id}.png")
                if os.path.exists(out_path):
                    continue  # already generated by an earlier run/jar for this namespace
                png_bytes = zf.read(name)
                out_im = recentered_pattern(png_bytes)
                os.makedirs(os.path.dirname(out_path), exist_ok=True)
                out_im.save(out_path)
                generated += 1
                print(f"  generated {namespace}:{pattern_id} from {os.path.basename(jar_path)}")

    print(f"\nDone. generated={generated} skipped(bedspreads-covered)={skipped_covered}")


if __name__ == "__main__":
    main()

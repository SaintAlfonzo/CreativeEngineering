#!/usr/bin/env python3
"""
Generates creativeengineering:entity/elytra_banner/<namespace>/<pattern> (and the
_low resolution counterpart) compat textures for every registered banner pattern
that customizableelytra doesn't already bundle curated wing-shaped art for.

Same underlying problem as tools/gen_bedspreads_pattern_compat.py: customizableelytra
renders patterns onto its own 128x64 (or 64x32 "low quality") wing-shaped canvas, not
a banner flag's 64x64 layout, and its curated textures
(assets/customizableelytra/textures/entity/elytra_banner/<id>.png - flat, no per-mod
namespace folder) are hand-repositioned to land on that canvas. A pattern's real
entity/banner/<id>.png is authored for the flag's own UV footprint and looks wrong
pasted onto the wing directly.

Placement convention, derived empirically by measuring customizableelytra's own
curated set (see the two clusters of bbox centers found by inspecting e.g.
elytra_banner/creeper.png vs elytra_banner/piglin.png vs elytra_banner/base.png):
every pattern is cropped to its natural single-glyph size (no scale-up), fit down to
fit within a small box if needed (preserving aspect ratio), and centered at a fixed
point. A handful of vanilla patterns (base/border/curly_border/bricks) get a
hand-picked wider full-wing treatment instead, but none of the mods we're filling
gaps for use those names, and customizableelytra's own "cross" pattern (also
naturally full-width) gets the small centered treatment same as everything else -
so every generated pattern here uses that one convention.

Run from the instance root:
    python3 gen_elytra_pattern_compat.py
"""
import glob
import os
import re
import zipfile
from io import BytesIO

import numpy as np
from PIL import Image

INSTANCE_ROOT = "/home/liam/.local/share/atlauncher/instances/CreativeEngineering2"
MODS_DIR = os.path.join(INSTANCE_ROOT, "mods")
ELYTRA_JAR = os.path.join(MODS_DIR, "customizableelytra-1.19.0-1.7.4.jar")
VANILLA_JAR = "/home/liam/.minecraft/versions/1.19.2/1.19.2.jar"
OUT_ROOT = os.path.join(
    INSTANCE_ROOT,
    "development/patcherMod/src/main/resources/assets/creativeengineering/textures/entity",
)

PATTERN_RE = re.compile(r"^assets/([a-z0-9_.\-]+)/textures/entity/banner/([a-z0-9_./\-]+)\.png$")

# Empirically measured from customizableelytra's own bundled elytra_banner/*.png set
# (e.g. creeper.png -> bbox (72,14)-(90,32), piglin.png -> (73,20)-(89,29), both an
# unscaled crop of the pattern's natural glyph, centered at ~(81,23)).
NORMAL_MAX_W, NORMAL_MAX_H = 20, 40
NORMAL_CENTER = (81, 23)
NORMAL_CANVAS = (128, 64)

# elytra_banner_low is an exact half-scale canvas; halve the same box/center for it.
LOW_MAX_W, LOW_MAX_H = 10, 20
LOW_CENTER = (40, 11)
LOW_CANVAS = (64, 32)


def first_glyph_bbox(im: Image.Image):
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


def centered_pattern(png_bytes: bytes, canvas_size, max_w, max_h, center):
    im = Image.open(BytesIO(png_bytes)).convert("RGBA")
    if im.size != (64, 64):
        im = im.resize((64, 64), Image.NEAREST)

    bbox = first_glyph_bbox(im)
    canvas = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    if bbox is None:
        return canvas  # fully transparent source

    x0, y0, x1, y1 = bbox
    glyph = im.crop((x0, y0, x1 + 1, y1 + 1))
    natural_w, natural_h = glyph.size

    scale = min(max_w / natural_w, max_h / natural_h, 1.0)
    new_w, new_h = max(1, round(natural_w * scale)), max(1, round(natural_h * scale))
    resized = glyph.resize((new_w, new_h), Image.LANCZOS) if scale != 1.0 else glyph

    cx, cy = center
    paste_x, paste_y = cx - new_w // 2, cy - new_h // 2
    canvas.alpha_composite(resized, (paste_x, paste_y))
    return canvas


def load_elytra_covered():
    covered = set()
    with zipfile.ZipFile(ELYTRA_JAR) as zf:
        for name in zf.namelist():
            m = re.match(r"^assets/customizableelytra/textures/entity/elytra_banner/([a-z0-9_.\-]+)\.png$", name)
            if m:
                covered.add(m.group(1))
    return covered


def iter_mod_jars():
    for path in sorted(glob.glob(os.path.join(MODS_DIR, "*.jar"))):
        if os.path.samefile(path, ELYTRA_JAR):
            continue
        yield path
    yield VANILLA_JAR


def main():
    covered = load_elytra_covered()
    print(f"customizableelytra already covers {len(covered)} pattern ids (flat, namespace-agnostic)")

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
                    continue  # not a real registered pattern (e.g. supplementaries' "flags/" mirror)
                if pattern_id in covered:
                    skipped_covered += 1
                    continue

                normal_out = os.path.join(OUT_ROOT, "elytra_banner", namespace, f"{pattern_id}.png")
                low_out = os.path.join(OUT_ROOT, "elytra_banner_low", namespace, f"{pattern_id}.png")
                if os.path.exists(normal_out):
                    continue  # already generated by an earlier jar for this namespace/id

                png_bytes = zf.read(name)
                normal_im = centered_pattern(png_bytes, NORMAL_CANVAS, NORMAL_MAX_W, NORMAL_MAX_H, NORMAL_CENTER)
                low_im = centered_pattern(png_bytes, LOW_CANVAS, LOW_MAX_W, LOW_MAX_H, LOW_CENTER)

                os.makedirs(os.path.dirname(normal_out), exist_ok=True)
                os.makedirs(os.path.dirname(low_out), exist_ok=True)
                normal_im.save(normal_out)
                low_im.save(low_out)

                generated += 1
                print(f"  generated {namespace}:{pattern_id} from {os.path.basename(jar_path)}")

    print(f"\nDone. generated={generated} skipped(elytra-covered)={skipped_covered}")


if __name__ == "__main__":
    main()

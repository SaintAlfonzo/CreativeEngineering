#!/usr/bin/env python3
"""Generate the potion-bucket item textures as two layers each, so the item can be
dynamically tinted per-potion at runtime (see development/creativeBrewing/src/main/resources/kubejs_startup/creativebrewing/potion_buckets.js
.color(...)) instead of baking one fixed colour into the PNG.

Takes vanilla `water_bucket.png`, splits it into:
  <id>.png         - layer0, the steel bucket body untouched; liquid pixels transparent.
  <id>_liquid.png  - layer1, the liquid swirl only (recoloured to neutral grey so the
                     runtime tint multiplies cleanly onto it), body pixels transparent.
The item model (Creative Brewing jar, assets/creativebrewing/models/item/<id>.json) maps these two
files to layer0/layer1; layer0 is left untinted (color() returns white for layer 0) and
layer1 is tinted with the bucket's potion colour.

All three bucket tiers share the same vanilla shape, so the same body/liquid split is
written under each of the three item ids (identical content today, kept as separate
files per id in case a tier's shape ever needs to diverge later).

Run from the instance root:  python3 tools/potion_bucket_texture.py
Needs Pillow and the vanilla 1.19.2 client-extra jar (for water_bucket.png).
"""
import io
import sys
import zipfile
from pathlib import Path
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import pack_paths  # noqa: E402

CLIENT_EXTRA = pack_paths.vanilla_jar()   # any vanilla jar with the assets (tools/pack_paths.py)
OUT = Path("development/creativeBrewing/src/main/resources/assets/creativebrewing/textures/item")

IDS = ["potion_bucket", "splash_potion_bucket", "lingering_potion_bucket"]


def load_vanilla(name):
    with zipfile.ZipFile(CLIENT_EXTRA) as z:
        data = z.read(f"assets/minecraft/textures/item/{name}")
    return Image.open(io.BytesIO(data)).convert("RGBA")


def is_liquid(r, g, b):
    # vanilla water_bucket liquid is distinctly blue; the steel body is grey (r~=g~=b)
    return b > r + 10 and b > g + 5


def split(base):
    w, h = base.size
    body = Image.new("RGBA", (w, h))
    liquid = Image.new("RGBA", (w, h))
    px = base.load()
    bp = body.load()
    lp = liquid.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                bp[x, y] = (0, 0, 0, 0)
                lp[x, y] = (0, 0, 0, 0)
                continue
            if is_liquid(r, g, b):
                bp[x, y] = (0, 0, 0, 0)
                # neutral grey at the source pixel's luminance, so a white (no-op) tint
                # reproduces the original shading and a coloured tint multiplies cleanly
                lum = min(255, int((0.299 * r + 0.587 * g + 0.114 * b) * 1.6))
                lp[x, y] = (lum, lum, lum, a)
            else:
                bp[x, y] = (r, g, b, a)
                lp[x, y] = (0, 0, 0, 0)
    return body, liquid


def main():
    base = load_vanilla("water_bucket.png")
    body, liquid = split(base)
    OUT.mkdir(parents=True, exist_ok=True)
    for name in IDS:
        body_dst = OUT / f"{name}.png"
        liquid_dst = OUT / f"{name}_liquid.png"
        body.save(body_dst)
        liquid.save(liquid_dst)
        print("wrote", body_dst)
        print("wrote", liquid_dst)


if __name__ == "__main__":
    main()

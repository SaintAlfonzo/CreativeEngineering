#!/usr/bin/env python3
"""
echo_texture.py — build Create-Deep-Dark-style animated "echo" item textures.

Create: Deep Dark's echo_sword.png is a 16x192 vertical strip of 12 frames. The
sword silhouette is identical in every frame; only the pixels painted in a fixed
12-step teal "glow ramp" change — each frame every glow pixel steps ONE position
along the ramp (wrapping), so the glow appears to crawl. The .png.mcmeta plays
that strip:

    { "animation": { "frametime": 2, "frames": [11,10,9,8,7,6,5,4,3,2,1,0] } }

i.e. 2 ticks/frame, reversed order (so the glow crawls the other way), looping.

This script reproduces that. Two modes:

  python3 tools/echo_texture.py animate <frame0.png> [more.png ...]
      <frame0.png> is a single 16x16 sprite you drew. Glow parts must use the
      exact RAMP colours below; everything else (handle, outline) is left static.
      Writes  <name>.png  (16x192)  +  <name>.png.mcmeta  next to the input,
      overwriting the 16x16 source. Re-run any time you edit the art.

  python3 tools/echo_texture.py starter
      Rough auto-conversion of the vanilla DIAMOND pickaxe/axe/shovel/hoe shapes
      into the echo palette (diamond's gem head is already teal, so the head/handle
      split is clean), written as 16x16 frame-0 sprites to
      kubejs/assets/creativeengineering/textures/item/echo_<tool>.png .
      A STARTING POINT — open each in an editor, clean up the head, then run
      `animate` on it.  Needs diamond_<tool>.png next to this script (unzip
      assets/minecraft/textures/item/ from the 1.19.2 client jar) or set
      DIAMOND_DIR.

Needs Pillow  (pip install pillow).
"""
import sys, os, json, struct
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
INSTANCE = os.path.dirname(HERE)
ASSET_DIR = os.path.join(INSTANCE, "kubejs", "assets", "creativeengineering", "textures", "item")

# ── the echo glow ramp, dark → bright (index 0 → 11) ────────────────────────
# Painted from Create: Deep Dark's echo_sword.png frame 0. Use these EXACT
# colours for anything that should pulse.
RAMP = [
    (5, 16, 16), (9, 28, 28), (11, 34, 34), (14, 43, 43),
    (21, 64, 64), (27, 82, 82), (28, 85, 85), (32, 97, 97),
    (36, 109, 109), (44, 131, 131), (45, 135, 135), (49, 148, 148),
]
RAMP_IDX = {c: i for i, c in enumerate(RAMP)}

# static echo palette (never animated)
OUTLINE   = [(11, 32, 32), (33, 33, 33), (45, 45, 45)]   # dark fill / edges
HANDLE    = [(53, 26, 12), (84, 42, 20), (134, 72, 41)]   # stick / grip, dark→light

MCMETA = {"animation": {"frametime": 2, "frames": list(range(11, -1, -1))}}
FRAMES = 12


def _lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def _nearest(c, palette):
    return min(palette, key=lambda p: sum((a - b) ** 2 for a, b in zip(p, c[:3])))


def animate(paths):
    for path in paths:
        src = Image.open(path).convert("RGBA")
        w, _ = src.size
        if w != 16:
            print(f"  ! {path}: width {w} != 16, skipping"); continue
        frame0 = src.crop((0, 0, 16, 16))
        px0 = list(frame0.getdata())

        strip = Image.new("RGBA", (16, 16 * FRAMES), (0, 0, 0, 0))
        for f in range(FRAMES):
            fpx = []
            for p in px0:
                if p[3] == 0:
                    fpx.append(p); continue
                key = (p[0], p[1], p[2])
                if key in RAMP_IDX:                       # a glow pixel → rotate
                    fpx.append((*RAMP[(RAMP_IDX[key] + f) % FRAMES], p[3]))
                else:                                    # static
                    fpx.append(p)
            fimg = Image.new("RGBA", (16, 16)); fimg.putdata(fpx)
            strip.paste(fimg, (0, f * 16))

        out = os.path.splitext(path)[0]
        if out.endswith(".png"):
            out = out[:-4]
        out += ".png"
        strip.save(out)
        with open(out + ".mcmeta", "w") as fh:
            json.dump(MCMETA, fh, indent=2)
        glow = sum(1 for p in px0 if p[3] and (p[0], p[1], p[2]) in RAMP_IDX)
        print(f"  {os.path.basename(out)}  ({glow} glow px)  + .mcmeta")


def _recolor_diamond(tool_img):
    """Rough diamond-shape → echo-palette conversion for a frame-0 starter.
       Diamond's gem head is teal → head, brown stick → handle. Clean split."""
    out = []
    for c in tool_img.convert("RGBA").getdata():
        if c[3] == 0:
            out.append(c); continue
        r, g, b = c[:3]
        if b > r + 10:                                          # the teal gem head
            t = max(0.0, min(1.0, (_lum(c) - 24) / (180 - 24)))
            out.append((*RAMP[round(t * (FRAMES - 1))], c[3]))
        elif _lum(c) < 22:
            out.append((*OUTLINE[1], c[3]))                     # near-black → edge
        else:
            out.append((*_nearest(c, HANDLE), c[3]))            # brown stick
    img = Image.new("RGBA", tool_img.size); img.putdata(out)
    return img


def starter():
    ddir = os.environ.get("DIAMOND_DIR", HERE)
    os.makedirs(ASSET_DIR, exist_ok=True)
    for tool in ("pickaxe", "axe", "shovel", "hoe"):
        srcp = os.path.join(ddir, f"diamond_{tool}.png")
        if not os.path.exists(srcp):
            print(f"  ! missing {srcp}  (unzip assets/minecraft/textures/item/ from the client jar)")
            continue
        dst = os.path.join(ASSET_DIR, f"echo_{tool}.png")
        _recolor_diamond(Image.open(srcp)).save(dst)
        print(f"  {dst}  (frame-0 starter — refine, then: echo_texture.py animate {dst})")


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "animate" and len(sys.argv) >= 3:
        animate(sys.argv[2:])
    elif len(sys.argv) == 2 and sys.argv[1] == "starter":
        starter()
    else:
        print(__doc__)
        sys.exit(1)

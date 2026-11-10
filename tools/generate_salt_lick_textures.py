#!/usr/bin/env python3
"""One-shot: the three worn salt lick textures (plan_hearth_crow_salt.md S1, hearth_and_harvest.md 5.7 N23-9).

The Salt's raw rock salt block texture carries the wear of Hearth and Harvest 1.4.1's own stages: each upstream stage is
divided per pixel by the upstream unworn block, and that brightness ratio is applied to the rock salt, plus a pull toward the
texture's mean colour (the licked-smooth look) and a slight darkening. The waxed forms use the same files.

Not a /cdevgen generator. Output goes into the backportedHearth member. Needs Pillow.
"""
import io
import os
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SALT_JAR = os.path.join(ROOT, 'server_mods/salt-1.19.2-1.2.4.1.jar')
BASE = 'assets/salt/textures/block/raw_rock_salt_block.png'
UPSTREAM = os.path.join(ROOT, 'DecompiledMods/hearthandharvest-1.21.1-1.4.1/assets/hearthandharvest/textures/block')
OUT = os.path.join(ROOT, 'development/creativeDelight/members/hearth/src/main/resources/assets/hearthandharvest/textures/block')
# stage: (pull toward the mean, overall brightness)
STAGES = {'lightly_licked': (0.15, 0.97), 'well_licked': (0.35, 0.93), 'heavily_licked': (0.55, 0.88)}


def load_base():
    with zipfile.ZipFile(SALT_JAR) as jar:
        return Image.open(io.BytesIO(jar.read(BASE))).convert('RGBA')


def mean_colour(image):
    pixels = list(image.getdata())
    return tuple(sum(p[c] for p in pixels) // len(pixels) for c in range(3))


def luma(rgb):
    return 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]


def main():
    base = load_base()
    mean = mean_colour(base)
    original = Image.open(os.path.join(UPSTREAM, 'salt_block.png')).convert('RGB')
    os.makedirs(OUT, exist_ok=True)
    for stage, (pull, brightness) in STAGES.items():
        worn = Image.open(os.path.join(UPSTREAM, stage + '_salt_block.png')).convert('RGB')
        result = base.copy()
        for y in range(base.height):
            for x in range(base.width):
                r, g, b, a = base.getpixel((x, y))
                ratio = luma(worn.getpixel((x, y))) / max(1.0, luma(original.getpixel((x, y))))
                channels = [(1.0 - pull) * c + pull * m for c, m in zip((r, g, b), mean)]
                channels = [max(0, min(255, round(c * ratio * brightness))) for c in channels]
                result.putpixel((x, y), (channels[0], channels[1], channels[2], a))
        result.save(os.path.join(OUT, stage + '_salt_block.png'))


if __name__ == '__main__':
    main()

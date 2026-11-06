#!/usr/bin/env python3
"""Test source for Creative Compat's [paintings] extraSources (scratch rows SQ-PA-03, CQ-PA-02).

Writes a resource-pack style zip whose only painting art is a solid magenta
assets/toomanypaintings/textures/painting/afternoon.png (32x32, the variant's 2x2 blocks at 16 px), so the built set
must take it over Too Many Paintings' own art and log "art for toomanypaintings:afternoon from <zip> (over ...)".

    python3 tools/scratch_query/painting_test_zip.py OUT.zip
"""
import io
import sys
import zipfile

from PIL import Image


def main(out):
    img = Image.new('RGBA', (32, 32), (255, 0, 255, 255))
    buf = io.BytesIO()
    img.save(buf, 'PNG')
    with zipfile.ZipFile(out, 'w') as z:
        z.writestr('pack.mcmeta', '{"pack": {"pack_format": 9, "description": "painting extraSources test"}}')
        z.writestr('assets/toomanypaintings/textures/painting/afternoon.png', buf.getvalue())
    print('wrote', out)


if __name__ == '__main__':
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])

#!/usr/bin/env python3
"""Every painting variant -> Immersive Paintings' Datapacks page.

Immersive Paintings loads extra built-in paintings from data/<ns>/paintings/**/<name>.png plus a
sibling <name>.json ({name, author, width, height, resolution}; width/height in blocks, resolution in
pixels per block). This copies every registered painting variant there - vanilla (including the
unplaceable earth/wind/fire/water), the painting packs, and the odd painting other mods add - so all of
them can be picked in the Immersive Paintings screen.

Variant ids and sizes come from /dumppaintings (kubejs/exported/paintings.json). Textures are read
from assets/<ns>/textures/painting/<id>.png, lowest priority first: the vanilla client jar, mods/*.jar,
kubejs/assets, then the resource-pack zips enabled in options.txt - so a pack that retextures a
painting is what Immersive shows too.

Paintings smaller than 32 px per block are scaled up (nearest neighbour, whole-number factor) to 32:
the Datapacks page opens filtered to resolution 32, and a 16 px painting would be hidden until the
player changes the filter. Pixel art stays sharp. Larger ones keep their own resolution.

Output: kubejs/data/creativeengineering/paintings/<ns>/<id>.png + .json. The folder is generated only
and is rebuilt from scratch on every run. In game a painting's id is
creativeengineering:paintings/<ns>/<id>.png, so the screen's search box matches mod ids and names.
Writeup and tests: feature_writeup/immersive_paintings.md.

    python3 tools/gen_immersive_paintings.py
"""
import io
import json
import re
import shutil
import sys
import zipfile

from PIL import Image

import lang_index

DUMP = lang_index.INSTANCE / 'kubejs/exported/paintings.json'
OUT = lang_index.INSTANCE / 'kubejs/data/creativeengineering/paintings'
KUBEJS_ASSETS = lang_index.INSTANCE / 'kubejs/assets'
# Fallback when ~/.minecraft has no 1.19.2 jar: the launcher's split client jar holds the assets.
VANILLA_EXTRA = (lang_index.Path.home() / '.local/share/atlauncher/libraries/net/minecraft/client'
                 / '1.19.2-20220805.130853/client-1.19.2-20220805.130853-extra.jar')
TEXTURE_RE = re.compile(r'^assets/([^/]+)/textures/painting/(.+)\.png$')
TARGET_RES = 32
# 1.19.2 has no painting title/author lang; these are the credits vanilla added in 1.21.
VANILLA_AUTHOR = 'Kristoffer Zetterstrand'
VANILLA_AUTHOR_OVERRIDES = {'earth': 'Mojang', 'wind': 'Mojang', 'water': 'Mojang', 'fire': 'Mojang',
                            'wither': 'Mojang'}
DISPLAY_NAME_RE = re.compile(r'^\s*displayName\s*=\s*"([^"]*)"', re.M)


def zip_textures(path, label, textures, owners):
    try:
        with zipfile.ZipFile(path) as z:
            for name in z.namelist():
                m = TEXTURE_RE.match(name)
                if m:
                    key = '%s:%s' % m.groups()
                    textures[key] = (label, z.read(name))
                    owners.setdefault(m.group(1), path)
    except zipfile.BadZipFile:
        print('WARNING: unreadable zip %s' % path)


def collect_textures():
    """{ns:id: (source label, png bytes)} after pack priority, and {ns: first jar that has it}."""
    textures, owners = {}, {}
    vanilla = lang_index.VANILLA_JAR if lang_index.VANILLA_JAR.exists() else VANILLA_EXTRA
    if not vanilla.exists():
        sys.exit('no vanilla 1.19.2 client jar (%s or %s)' % (lang_index.VANILLA_JAR, VANILLA_EXTRA))
    zip_textures(vanilla, 'vanilla', textures, {})
    for jar in sorted(lang_index.MODS.glob('*.jar')):
        zip_textures(jar, jar.name, textures, owners)
    for png in sorted(KUBEJS_ASSETS.glob('*/textures/painting/**/*.png')):
        rel = png.relative_to(KUBEJS_ASSETS)
        ns, sub = rel.parts[0], '/'.join(rel.parts[3:])[:-len('.png')]
        textures['%s:%s' % (ns, sub)] = ('kubejs/assets', png.read_bytes())
    for z in lang_index.enabled_pack_zips():
        if z.exists():
            zip_textures(z, 'resourcepacks/' + z.name, textures, {})
    return textures, owners


def mod_display_name(jar):
    try:
        with zipfile.ZipFile(jar) as z:
            m = DISPLAY_NAME_RE.search(z.read('META-INF/mods.toml').decode('utf-8', 'replace'))
            return m.group(1) if m else None
    except (KeyError, zipfile.BadZipFile):
        return None


SECTION_AUTHOR_RE = re.compile(r'^([a-z0-9_]+)\.author\.[a-z0-9_]+$')
SECTION_TITLE_RE = re.compile(r'^painting\.([a-z0-9_]+)\.(.+)\.title$')


def section_authors():
    """DarkPaintings credits most artists by section: a '<ns>.author.<x>' key, then that artist's
    'painting.<ns>.<id>.title' keys, in file order. Returns {ns:id: author} read that way."""
    out = {}
    for jar in sorted(lang_index.MODS.glob('*.jar')):
        try:
            with zipfile.ZipFile(jar) as z:
                for name in z.namelist():
                    if not name.endswith('/lang/en_us.json'):
                        continue
                    try:
                        data = json.loads(z.read(name))   # dicts keep file order
                    except ValueError:
                        continue
                    current = None
                    for k, val in data.items():
                        m = SECTION_AUTHOR_RE.match(k)
                        if m:
                            current = (m.group(1), val)
                            continue
                        m = SECTION_TITLE_RE.match(k)
                        if m and current and current[0] == m.group(1):
                            out['%s:%s' % m.groups()] = current[1]
        except zipfile.BadZipFile:
            pass
    return out


def real_author(name):
    return name if name and name.strip().lower() not in ('unknown', '-', '') else None


def title_case(path):
    return ' '.join(w.capitalize() for w in path.rsplit('/', 1)[-1].split('_') if w)


def main():
    if not DUMP.exists():
        sys.exit('%s missing: run /dumppaintings in game first' % DUMP.relative_to(lang_index.INSTANCE))
    variants = json.loads(DUMP.read_text())['paintings']
    textures, owners = collect_textures()
    names, _ = lang_index.build()
    sections = section_authors()
    display = {}

    if OUT.exists():
        shutil.rmtree(OUT)
    written, upscaled, by_ns, problems, notes = 0, 0, {}, [], []
    for vid in sorted(variants):
        v = variants[vid]
        ns, path = vid.split(':', 1)
        bw, bh = v['width'] // 16, v['height'] // 16
        if bw < 1 or bh < 1 or v['width'] % 16 or v['height'] % 16:
            problems.append('%s: size %dx%d px is not whole blocks, skipped' % (vid, v['width'], v['height']))
            continue
        if vid not in textures:
            problems.append('%s: no texture assets/%s/textures/painting/%s.png anywhere, skipped' % (vid, ns, path))
            continue
        label, raw = textures[vid]
        img = Image.open(io.BytesIO(raw)).convert('RGBA')
        res = img.width // bw
        if res >= 1 and img.width % bw == 0 and img.height > res * bh and img.height % (res * bh) == 0:
            img = img.crop((0, 0, img.width, res * bh))   # animated strip: first frame
        if res < 1 or img.width != res * bw or img.height != res * bh:
            # Vanilla stretches any texture over the whole painting; do the same at the variant's size.
            notes.append('%s: %dx%d texture is off the %dx%d block grid (%s), stretched to %dx%d like vanilla'
                         % (vid, img.width, img.height, bw, bh, label, v['width'], v['height']))
            img = img.resize((v['width'], v['height']), Image.NEAREST)
            res = 16
        if res < TARGET_RES and TARGET_RES % res == 0:
            img = img.resize((bw * TARGET_RES, bh * TARGET_RES), Image.NEAREST)
            res = TARGET_RES
            upscaled += 1

        if ns == 'minecraft':
            author = VANILLA_AUTHOR_OVERRIDES.get(path, VANILLA_AUTHOR)
        else:
            author = (real_author(names.get('painting.%s.%s.author' % (ns, path)))
                      or real_author(names.get('painting.%s.%s.artist' % (ns, path)))
                      or real_author(sections.get(vid)))
            if not author:
                if ns not in display:
                    display[ns] = mod_display_name(owners[ns]) if ns in owners else None
                author = display[ns] or ns
        name = names.get('painting.%s.%s.title' % (ns, path)) or title_case(path)

        dest = OUT / ns / path
        dest.parent.mkdir(parents=True, exist_ok=True)
        img.save(dest.with_suffix('.png'), optimize=True)
        meta = {'name': name, 'author': author, 'width': bw, 'height': bh, 'resolution': res}
        dest.with_suffix('.json').write_text(json.dumps(meta, indent='\t', ensure_ascii=False) + '\n')
        written += 1
        by_ns[ns] = by_ns.get(ns, 0) + 1

    print('%d of %d painting variants -> %s (%d scaled up to %dpx/block)'
          % (written, len(variants), OUT.relative_to(lang_index.INSTANCE), upscaled, TARGET_RES))
    for ns in sorted(by_ns):
        print('  %-28s %d' % (ns, by_ns[ns]))
    for n in notes:
        print('  note: ' + n)
    for p in problems:
        print('  ! ' + p)
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())

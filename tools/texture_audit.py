#!/usr/bin/env python3
"""
texture_audit.py - find item/block textures in mod jars that nothing uses.

A texture counts as used if its path appears in any model/blockstate/data JSON,
resource pack (resourcepacks/*.zip and folders), KubeJS file, or as a string in
any .class file (jar-in-jar included). Textures whose file name matches a
registered item id (kubejs/exported/items_by_mod) are also treated as used,
since those are usually drawn by code-built models.

What is left is a candidate list, not proof: code that builds paths by string
concatenation (fluid "_flow", connected "_connected", IE shader layers, belt
scroll colours) still shows up. See feature_writeup/unused_textures.md.

Usage:
    python3 tools/texture_audit.py                  # writes feature_writeup/unused_textures/all_unused.tsv
    python3 tools/texture_audit.py --sheet LIST OUT # contact sheet PNG (needs Pillow) from a
                                                    # "namespace textures/path" list such as food.txt
"""
import glob, io, json, os, re, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_DIR = os.path.join(ROOT, 'feature_writeup', 'unused_textures')
TSV = os.path.join(OUT_DIR, 'all_unused.tsv')
TEX_RE = re.compile(r'^assets/([^/]+)/textures/((?:item|items|block|blocks)/.+)\.png$')
REF_RE = re.compile(rb'([a-z0-9_.\-]+:)?((?:item|items|block|blocks)/[a-z0-9_./\-]+)')

textures = {}   # (ns, path) -> jar label
refs = set()    # (ns or None, path) from data and resource files
code = set()    # (ns or None, path) from .class strings


def add_refs(data, into):
    for m in REF_RE.finditer(data):
        ns = m.group(1).decode()[:-1] if m.group(1) else None
        into.add((ns, re.sub(r'\.png$', '', m.group(2).decode())))


def walk_zip(z, label, collect):
    for n in z.namelist():
        if n.endswith('.jar') and n.startswith('META-INF/'):
            try:
                walk_zip(zipfile.ZipFile(io.BytesIO(z.read(n))), label + '!' + os.path.basename(n), collect)
            except zipfile.BadZipFile:
                pass
            continue
        m = TEX_RE.match(n)
        if m and collect:
            textures.setdefault((m.group(1), m.group(2)), label)
        elif n.endswith('.json') and n.startswith(('assets/', 'data/')):
            add_refs(z.read(n), refs)
        elif n.endswith('.class'):
            add_refs(z.read(n), code)
        elif n.startswith('assets/') and n.endswith(('.properties', '.js', '.mcmeta', '.txt', '.toml')):
            add_refs(z.read(n), refs)


def scan():
    for j in sorted(glob.glob(ROOT + '/mods/*.jar')):
        walk_zip(zipfile.ZipFile(j), os.path.basename(j), True)
    for j in glob.glob(ROOT + '/resourcepacks/*.zip'):
        walk_zip(zipfile.ZipFile(j), os.path.basename(j), False)
    for d in ('kubejs', 'resourcepacks/assets', 'resources'):
        for dp, _, fs in os.walk(os.path.join(ROOT, d)):
            for f in fs:
                if f.endswith(('.json', '.js', '.properties')):
                    add_refs(open(os.path.join(dp, f), 'rb').read(), refs)
    ids = set()
    for f in glob.glob(ROOT + '/kubejs/exported/items_by_mod/*.json'):
        ids.update(json.load(open(f)).get('items', []))
    rows = []
    for (ns, p), jar in sorted(textures.items()):
        if ns == 'minecraft' or (ns, p) in refs:
            continue
        if (ns, p) in code or (None, p) in code or f'{ns}:{p.split("/")[-1]}' in ids:
            continue
        rows.append((ns, p, jar))
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(TSV, 'w') as f:
        f.write('namespace\ttexture\tjar\n')
        for r in rows:
            f.write('\t'.join(r) + '\n')
    print(f'{len(textures)} item/block textures, {len(rows)} unused -> {os.path.relpath(TSV, ROOT)}')


def sheet(list_path, out):
    from PIL import Image, ImageDraw
    jars = {}
    for line in open(TSV).read().splitlines()[1:]:
        ns, p, jar = line.split('\t')
        jars[(ns, p)] = jar
    cells = []
    for line in open(list_path):
        if not line.strip() or line.startswith('#'):
            continue
        ns, p = line.split()[:2]
        jar = jars.get((ns, p))
        if not jar:
            print('not in all_unused.tsv:', ns, p)
            continue
        z = zipfile.ZipFile(os.path.join(ROOT, 'mods', jar.split('!')[0]))
        name = f'assets/{ns}/textures/{p}.png'
        for inner in jar.split('!')[1:]:
            z = zipfile.ZipFile(io.BytesIO(z.read(next(n for n in z.namelist() if n.endswith('/' + inner)))))
        im = Image.open(io.BytesIO(z.read(name))).convert('RGBA')
        w, h = im.size
        if h > w:
            im = im.crop((0, 0, w, w))  # first animation frame
        cells.append((f'{ns}:{p.split("/", 1)[1]}', im.resize((64, 64), Image.NEAREST)))
    cols, cw, ch = 6, 170, 92
    img = Image.new('RGBA', (cols * cw, ((len(cells) + cols - 1) // cols) * ch), (60, 60, 60, 255))
    d = ImageDraw.Draw(img)
    for i, (lab, im) in enumerate(cells):
        x, y = (i % cols) * cw, (i // cols) * ch
        img.paste(im, (x + 53, y + 4), im)
        d.text((x + 3, y + 72), lab[-28:], fill=(255, 255, 255, 255))
    img.save(out)
    print(len(cells), 'cells ->', out)


if __name__ == '__main__':
    if len(sys.argv) == 4 and sys.argv[1] == '--sheet':
        sheet(sys.argv[2], sys.argv[3])
    else:
        scan()

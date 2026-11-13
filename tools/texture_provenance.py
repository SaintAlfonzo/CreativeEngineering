#!/usr/bin/env python3
"""
texture_provenance.py - where every pack-owned texture came from, and which ones break the texture rule.

Texture rule (feature_writeup/texture_sources.md section 1): every texture is human-made (upstream art or the
owner's own), or made from human art by the owner's tools (textureswapper/palette_swap.py, combine_tex.py,
scale_tex.py, upscale_sprites.py) on a source and palette a human picked. Nothing drawn or transformed by AI.

What it does, for every PNG under development/**/textures and kubejs/assets/**/textures (build output skipped):
  1. Pixel match against every third-party texture on disk (mods/, server_mods/, devmods/, disabledmods/,
     jar_archive/, DecompiledMods/, *_upstream/, development/**/libs, vanilla 1.19.2, resourcepacks/*.zip;
     the pack's own jars are skipped). Exact RGBA match = copy.
  2. Otherwise the same alpha silhouette as a third-party sprite (or, for opaque squares, a third-party texture
     sharing a name word): colour-map test. Each source colour going to exactly one output colour = recolour;
     pixels off that map = edited (painted by someone). Nothing similar = unmatched.
  3. Transcript evidence (optional, --transcripts DIR, Claude Code session logs, which start 2026-10-14):
     calls that named the file and ran the owner's tools (owned), ran ad-hoc pixel code (paint), or a script.
  4. feature_writeup/texture_sources/sources.tsv (hand-kept, first matching glob wins) sets origin, source,
     tool, use and status; rows it does not cover get an automatic origin from 1-3.

Writes feature_writeup/texture_sources/ledger.tsv and prints counts per status.

    python3 tools/texture_provenance.py [--transcripts DIR]
    python3 tools/texture_provenance.py --sheet swap,redo OUT.png              # contact sheet from the ledger
    python3 tools/texture_provenance.py --sheet confirm:edited,unmatched OUT.png

Statuses: ok | redo (AI transform of human art: redo with the owner's tools from the recorded source) |
swap (AI art: replace with human art) | confirm (no evidence either way: owner to say). Needs Pillow.
"""
import argparse, fnmatch, glob, hashlib, io, json, os, re, sys, zipfile
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOC = os.path.join(ROOT, 'feature_writeup', 'texture_sources')
SOURCES = os.path.join(DOC, 'sources.tsv')
LEDGER = os.path.join(DOC, 'ledger.tsv')
OWN_JAR = re.compile(r'^(backported|repacked|creative|exposureBackport|solraspberrypie|babyMobs|moreMooblooms|'
                     r'incubationAdditions|patcherMod)', re.I)
STOP = {'block', 'blocks', 'side', 'top', 'bottom', 'front', 'back', 'item', 'stage', 'base', 'overlay', 'inner',
        'outer', 'texture', 'interior', 'full', 'level', 'end', 'raw', 'cooked', 'small', 'large', 'medium'}


def rgba(data):
    try:
        return Image.open(io.BytesIO(data)).convert('RGBA')
    except Exception:
        return None


def mask_key(im):
    a = bytes(1 if p > 0 else 0 for p in im.getchannel('A').tobytes())
    return hashlib.sha1(f'{im.size}'.encode() + a).hexdigest(), a.count(1)


def words(name):
    return {w for w in re.split(r'[_\-\d]+', name) if len(w) > 2 and w not in STOP}


class Index:
    def __init__(self):
        self.pix, self.mask, self.img, self.by_word = {}, {}, {}, {}

    def add(self, label, data):
        im = rgba(data)
        if im is None:
            return
        self.pix.setdefault(hashlib.sha1(im.tobytes()).hexdigest(), label)
        if im.size[0] * im.size[1] > 65536:
            return
        self.img[label] = im
        frames = [im]
        w, h = im.size
        if h > w and h % w == 0:
            frames.append(im.crop((0, 0, w, w)))
        for f in frames:
            k, n = mask_key(f)
            if n > 8:
                self.mask.setdefault(k, []).append(label)
        for wd in words(os.path.basename(label)[:-4]):
            self.by_word.setdefault(wd, []).append(label)

    def walk_zip(self, z, label, depth=0):
        for n in z.namelist():
            if n.endswith('.png') and '/textures/' in n:
                self.add(f'{label}!{n}', z.read(n))
            elif n.endswith('.jar') and depth < 2:
                try:
                    self.walk_zip(zipfile.ZipFile(io.BytesIO(z.read(n))), f'{label}!{os.path.basename(n)}', depth + 1)
                except Exception:
                    pass


def build_index():
    idx = Index()
    jars = []
    for d in ['mods', 'server_mods', 'client_mods', 'disabledmods', 'devmods', 'jar_archive', 'DecompiledMods',
              'jarmods']:
        jars += glob.glob(os.path.join(ROOT, d, '**', '*.jar'), recursive=True)
    jars += glob.glob(os.path.join(ROOT, '*_upstream', '**', '*.jar'), recursive=True)
    jars += [p for p in glob.glob(os.path.join(ROOT, 'development', '**', '*.jar'), recursive=True)
             if '/build/' not in p and 'gradle' not in p]
    jars += [os.path.expanduser('~/.minecraft/versions/1.19.2/1.19.2.jar')]
    jars += glob.glob(os.path.join(ROOT, 'resourcepacks', '*.zip'))
    for j in sorted(set(jars)):
        if OWN_JAR.match(os.path.basename(j)) or not os.path.isfile(j):
            continue
        try:
            idx.walk_zip(zipfile.ZipFile(j), os.path.relpath(j, ROOT))
        except Exception as e:
            print('skip', j, e, file=sys.stderr)
    for d in ['DecompiledMods'] + [os.path.basename(p) for p in glob.glob(os.path.join(ROOT, '*_upstream'))]:
        for p in glob.glob(os.path.join(ROOT, d, '**', 'textures', '**', '*.png'), recursive=True):
            idx.add(os.path.relpath(p, ROOT), open(p, 'rb').read())
    return idx


def colour_map(src, out):
    """(consistency, off-map pixels, share of pixels unchanged) of out read as a per-colour recolour of src."""
    if out.size != src.size:
        out = out.crop((0, 0) + src.size)
    m, same, tot = {}, 0, 0
    for a, b in zip(src.getdata(), out.getdata()):
        if a[3] == 0:
            continue
        tot += 1
        same += a == b
        m.setdefault(a, {}).setdefault(b, 0)
        m[a][b] += 1
    if not tot:
        return 0, 0, 0
    ok = sum(max(v.values()) for v in m.values())
    return ok / tot, tot - ok, same / tot


def classify(idx, path):
    im = Image.open(os.path.join(ROOT, path)).convert('RGBA')
    h = hashlib.sha1(im.tobytes()).hexdigest()
    if h in idx.pix:
        return 'copy', idx.pix[h], ''
    cands = []
    w, ht = im.size
    for f in ([im] + ([im.crop((0, 0, w, w))] if ht > w and ht % w == 0 else [])):
        k, n = mask_key(f)
        opaque = n == f.size[0] * f.size[1]
        if not opaque:
            cands += idx.mask.get(k, [])[:60]
        else:   # an opaque square matches every opaque square: use name words instead
            for wd in words(os.path.basename(path)[:-4]):
                cands += [c for c in idx.by_word.get(wd, [])[:400] if idx.img[c].size == f.size]
    best = None
    for c in dict.fromkeys(cands):
        r = colour_map(idx.img[c], im)
        if best is None or (r[0], r[2]) > (best[1][0], best[1][2]):
            best = (c, r)
    if best is None or best[1][0] < 0.6:
        return 'unmatched', '', ''
    c, (cons, off, same) = best
    cls = 'recolour' if cons >= 0.999 else 'edited'
    return cls, c, f'consistency={cons:.3f} offmap_px={off} unchanged={same:.2f}'


def transcript_evidence(tdir):
    """png basename -> set of 'owned' / 'paint' / 'script' from Claude Code session logs."""
    ev = {}
    draw = re.compile(r'putpixel|ImageDraw|\.rectangle\(|\.ellipse\(|\.polygon\(|px\[[^\]]+\]\s*=(?!=)|load\(\)\[')
    for f in glob.glob(os.path.join(tdir, '*.jsonl')):
        for line in open(f, errors='replace'):
            if '"tool_use"' not in line or '.png' not in line:
                continue
            try:
                o = json.loads(line)
            except Exception:
                continue
            for c in (o.get('message') or {}).get('content') or []:
                if not isinstance(c, dict) or c.get('type') != 'tool_use':
                    continue
                inp = c.get('input') or {}
                t = inp.get('command') or inp.get('content') or inp.get('new_string') or ''
                if not isinstance(t, str) or '.jsonl' in t or 'transcript' in t:
                    continue
                if re.search(r'palette_swap|combine_tex|scale_tex|upscale_sprites', t):
                    kind = 'owned'
                elif draw.search(t) and re.search(r'\.save\(', t):
                    kind = 'paint'
                elif re.search(r'(?:make|gen|generate)_[a-z_]+\.py|_texture\.py', t):
                    kind = 'script'
                else:
                    continue
                for n in set(re.findall(r'([A-Za-z0-9_\-]+)\.png', t)):
                    ev.setdefault(n, set()).add(kind)
    return ev


def load_sources():
    rows = []
    if os.path.isfile(SOURCES):
        for line in open(SOURCES):
            if line.startswith('#') or not line.strip() or line.startswith('pattern\t'):
                continue
            p = (line.rstrip('\n').split('\t') + [''] * 7)[:7]
            rows.append(p)
    return rows


def auto(cls, match, ev):
    if cls == 'copy':
        return ('vanilla' if '1.19.2.jar' in match else 'upstream'), 'ok', ''
    if ev == {'owned'} or ('owned' in ev and 'paint' not in ev):
        return 'owned-tool', 'ok', 'transcript: made with the owner\'s tools'
    if 'paint' in ev and 'owned' not in ev:
        return 'ai-transform', 'redo', 'transcript: ad-hoc pixel script, not the owner\'s tools'
    if 'paint' in ev:
        return 'mixed', 'confirm', 'transcript: owner\'s tools and ad-hoc pixel code both touched it'
    return 'unknown', 'confirm', ''


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--transcripts', default=os.path.expanduser(
        '~/.claude/projects/-home-liam--local-share-atlauncher-instances-CreativeEngineering2'))
    ap.add_argument('--sheet', nargs=2, metavar=('STATUS', 'OUT'))
    a = ap.parse_args()

    if a.sheet and os.path.isfile(LEDGER):
        return sheet(a.sheet[0], a.sheet[1])

    own = []
    for base in ['development', 'kubejs/assets']:
        for p in glob.glob(os.path.join(ROOT, base, '**', 'textures', '**', '*.png'), recursive=True):
            rp = os.path.relpath(p, ROOT)
            if not re.search(r'/(build|bin|run|jar_archive)/', rp):
                own.append(rp)
    idx = build_index()
    ev = transcript_evidence(a.transcripts) if os.path.isdir(a.transcripts) else {}
    src = load_sources()
    os.makedirs(DOC, exist_ok=True)
    counts = {}
    with open(LEDGER, 'w') as f:
        f.write('path\tpixel_class\tpixel_match\tpixel_detail\ttranscript\torigin\tsource\ttool\tused_for\tstatus\tnote\n')
        for rp in sorted(own):
            cls, match, detail = classify(idx, rp)
            e = ev.get(os.path.basename(rp)[:-4], set())
            row = next((r for r in src if fnmatch.fnmatch(rp, r[0])), None)
            if row:
                origin, source, tool, use, status, note = row[1:]
            else:
                origin, status, note = auto(cls, match, e)
                source, tool, use = (match if cls != 'unmatched' else ''), '', ''
            counts[status] = counts.get(status, 0) + 1
            f.write('\t'.join([rp, cls, match, detail, '+'.join(sorted(e)), origin, source, tool, use, status, note])
                    + '\n')
    print('ledger:', os.path.relpath(LEDGER, ROOT), ' '.join(f'{k}={v}' for k, v in sorted(counts.items())))


def sheet(status, out):
    rows = [l.split('\t') for l in open(LEDGER).read().splitlines()[1:]]
    want, _, classes = status.partition(':')   # e.g. "swap,redo" or "confirm:edited,unmatched"
    rows = [r for r in rows if r[9] in want.split(',') and (not classes or r[1] in classes.split(','))]
    cell, cols = 48, 12
    from PIL import ImageDraw
    img = Image.new('RGBA', (cols * cell, ((len(rows) + cols - 1) // cols) * (cell + 10)), (60, 60, 60, 255))
    d = ImageDraw.Draw(img)
    for i, r in enumerate(rows):
        t = Image.open(os.path.join(ROOT, r[0])).convert('RGBA')
        if t.size[1] > t.size[0]:
            t = t.crop((0, 0, t.size[0], t.size[0]))
        t.thumbnail((cell - 8, cell - 8), Image.NEAREST)
        t = t.resize((cell - 8, cell - 8), Image.NEAREST)
        x, y = (i % cols) * cell + 4, (i // cols) * (cell + 10) + 2
        img.alpha_composite(t, (x, y))
        d.text((x - 2, y + cell - 8), os.path.basename(r[0])[:-4][:8], fill=(255, 255, 255, 255))
    img.save(out)
    print(out, len(rows), 'textures')


if __name__ == '__main__':
    main()

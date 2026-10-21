#!/usr/bin/env python3
"""Leaf paths in structure templates: for every .nbt template (mod jars, kubejs/data, datapacks) that places
natural leaves (persistent=false), the longest face-connected leaf path to a log, so CEConfig [leaf_decay]
maxDistance can be checked against them (feature_writeup/mob_block_tweaks.md section 2.5).

    python3 tools/leaf_path_scan.py [--all]

Logs = palette names containing log / wood / stem / hyphae (not stripped, not planks). Paths run through any leaves
(persistent ones carry the path too, as in vanilla). "none" = natural leaves with no face path to a log in the
template (they decay whatever the range). Prints a histogram of per-template maxima and every template with a leaf
past 6 (vanilla's reach); --all lists every template. Tree *features* (not templates) are measured in game with
tools/leaf_path_probe.js.
"""
import sys, os, gzip, struct, zipfile, glob, collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def nbt(b, i, t):
    """Minimal NBT payload reader (tag type t at offset i) -> (value, next offset); arrays other than lists are skipped."""
    if t == 1: return struct.unpack_from('>b', b, i)[0], i + 1
    if t == 2: return struct.unpack_from('>h', b, i)[0], i + 2
    if t == 3: return struct.unpack_from('>i', b, i)[0], i + 4
    if t == 4: return struct.unpack_from('>q', b, i)[0], i + 8
    if t == 5: return None, i + 4
    if t == 6: return None, i + 8
    if t == 7: return None, i + 4 + struct.unpack_from('>i', b, i)[0]
    if t == 8:
        n = struct.unpack_from('>H', b, i)[0]
        return b[i + 2:i + 2 + n].decode('utf8', 'replace'), i + 2 + n
    if t == 9:
        et = b[i]; n = struct.unpack_from('>i', b, i + 1)[0]; i += 5; out = []
        for _ in range(n):
            v, i = nbt(b, i, et); out.append(v)
        return out, i
    if t == 10:
        d = {}
        while True:
            tt = b[i]; i += 1
            if tt == 0: return d, i
            n = struct.unpack_from('>H', b, i)[0]; name = b[i + 2:i + 2 + n].decode('utf8', 'replace'); i += 2 + n
            v, i = nbt(b, i, tt); d[name] = v
    if t == 11: return None, i + 4 + 4 * struct.unpack_from('>i', b, i)[0]
    if t == 12: return None, i + 4 + 8 * struct.unpack_from('>i', b, i)[0]
    raise ValueError('bad tag %d' % t)


FACES = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def read_template(data):
    try:
        b = gzip.decompress(data)
    except Exception:
        return None
    if not b or b[0] != 10:
        return None
    n = int.from_bytes(b[1:3], 'big')
    try:
        root, _ = nbt(b, 3 + n, 10)
    except (ValueError, struct.error, IndexError):
        return None
    return root


def is_log(name):
    short = name.split(':')[-1]
    return any(s in short for s in ('log', 'wood', 'stem', 'hyphae')) and 'stripped' not in short and 'planks' not in short


def analyse(root):
    pals = root.get('palettes') or [root.get('palette') or []]
    pal = pals[0] if pals else []
    if not isinstance(pal, list):
        return None
    kind = {}
    for i, p in enumerate(pal):
        if not isinstance(p, dict):
            continue
        name = p.get('Name', '')
        props = p.get('Properties') or {}
        if name.endswith('leaves') and 'distance' in props:
            kind[i] = 'L' if props.get('persistent') == 'false' else 'P'
        elif is_log(name):
            kind[i] = 'W'
    if 'L' not in kind.values():
        return None
    pos = {}
    for bl in root.get('blocks') or []:
        if isinstance(bl, dict) and bl.get('pos') and bl.get('state') in kind:
            pos[tuple(bl['pos'])] = kind[bl['state']]
    dist = {p: 0 for p, k in pos.items() if k == 'W'}
    queue = list(dist)
    for c in queue:
        for d in FACES:
            n = (c[0] + d[0], c[1] + d[1], c[2] + d[2])
            if pos.get(n) in ('L', 'P') and n not in dist:
                dist[n] = dist[c] + 1
                queue.append(n)
    ds = [dist.get(p) for p, k in pos.items() if k == 'L']
    if not ds:
        return None
    con = [d for d in ds if d is not None]
    return {'leaves': len(ds), 'max': max(con) if con else 0, 'over6': sum(d > 6 for d in con),
            'over12': sum(d > 12 for d in con), 'none': sum(d is None for d in ds)}


def templates():
    for jar in sorted(glob.glob(os.path.join(ROOT, 'mods', '*.jar'))):
        try:
            z = zipfile.ZipFile(jar)
        except Exception:
            continue
        for n in z.namelist():
            if n.endswith('.nbt') and ('/structures/' in n or '/structure/' in n):
                yield os.path.basename(jar) + '!' + n, z.read(n)
    for sub in ('kubejs/data', 'datapacks', 'global_packs'):
        for f in glob.glob(os.path.join(ROOT, sub, '**', '*'), recursive=True):
            rel = os.path.relpath(f, ROOT)
            if f.endswith('.nbt'):
                with open(f, 'rb') as fh:
                    yield rel, fh.read()
            elif f.endswith('.zip'):
                try:
                    z = zipfile.ZipFile(f)
                except Exception:
                    continue
                for n in z.namelist():
                    if n.endswith('.nbt'):
                        yield rel + '!' + n, z.read(n)


def main():
    show_all = '--all' in sys.argv
    rows = []
    for label, data in templates():
        root = read_template(data)
        a = analyse(root) if root else None
        if a:
            rows.append((label, a))
    print('templates with natural leaves:', len(rows))
    print('per-template longest path:', dict(sorted(collections.Counter(a['max'] for _, a in rows).items())))
    print('templates with a leaf past 6:', sum(1 for _, a in rows if a['over6']),
          ' past 12:', sum(1 for _, a in rows if a['over12']))
    for label, a in sorted(rows, key=lambda r: -r[1]['max']):
        if show_all or a['over6']:
            print(f"{a['max']:3d}  >6 {a['over6']:4d}  >12 {a['over12']:4d}  none {a['none']:4d}  leaves {a['leaves']:5d}  {label}")


if __name__ == '__main__':
    main()

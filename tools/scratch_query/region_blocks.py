#!/usr/bin/env python3
"""Offline block and entity census of a scratch world's region files (scratch queue SQ-MD-01, SQ-FGD-12 to -15).

    region_blocks.py blocks LEVEL REGEX [--box X0 Z0 X1 Z1] [--band 8] [--near REGEX2] [--fail-above Y]
                                        [--fail-depth-below N] [--json FILE]
    region_blocks.py entities LEVEL REGEX [--box X0 Z0 X1 Z1] [--field NAME] [--json FILE]

LEVEL is the world folder (OUT/srv/<level-name>). Only chunks with Status full are read.
blocks: every block whose id matches REGEX (Python, searched in the palette name), counted by id and
  - Y band (BAND layers per band, default 8), by biome (the section's 4x4x4 biome cell),
  - depth = blocks below the chunk's WORLD_SURFACE heightmap top (0 = the top block itself; < 13 = "near the surface"),
  - chunks holding at least one match by the chunk's surface biome (centre column), against all chunks of that biome,
  - --near REGEX2: how many matches have one of their 6 neighbours matching REGEX2 (same chunk only).
  --fail-above Y: exit 1 when a match sits above Y.  --fail-depth-below N: exit 1 when a match has depth < N.
entities: entities in LEVEL/entities whose id matches REGEX, by id, biome (from the region chunk under them) and the
  value of NBT field --field (e.g. Type for mooshrooms).
Prints a text summary; --json writes the full numbers. Pure reader: changes nothing.
"""
import collections, glob, json, os, re, struct, sys, zlib

MIN_Y = -64


def nbt(b, i, t):
    if t == 1: return struct.unpack_from('>b', b, i)[0], i + 1
    if t == 2: return struct.unpack_from('>h', b, i)[0], i + 2
    if t == 3: return struct.unpack_from('>i', b, i)[0], i + 4
    if t == 4: return struct.unpack_from('>q', b, i)[0], i + 8
    if t == 5: return struct.unpack_from('>f', b, i)[0], i + 4
    if t == 6: return struct.unpack_from('>d', b, i)[0], i + 8
    if t == 7: n = struct.unpack_from('>i', b, i)[0]; return None, i + 4 + n
    if t == 8: n = struct.unpack_from('>H', b, i)[0]; return b[i + 2:i + 2 + n].decode('utf8', 'replace'), i + 2 + n
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
    if t == 11: n = struct.unpack_from('>i', b, i)[0]; return list(struct.unpack_from('>%di' % n, b, i + 4)), i + 4 + 4 * n
    if t == 12: n = struct.unpack_from('>i', b, i)[0]; return list(struct.unpack_from('>%dq' % n, b, i + 4)), i + 4 + 8 * n
    raise ValueError('nbt type %d' % t)


def region_xz(path):
    m = re.search(r'r\.(-?\d+)\.(-?\d+)\.mca$', path)
    return (int(m.group(1)), int(m.group(2))) if m else None


def chunks(path, box=None):
    """Chunk roots of a region file; with box, chunks outside it are skipped before decompressing."""
    rxz = region_xz(path)
    if box and rxz and not any(in_box(rxz[0] * 32 + i, rxz[1] * 32 + j, box) for i in (0, 31) for j in (0, 31)) \
            and not in_box_region(rxz, box):
        return
    data = open(path, 'rb').read()
    for k in range(1024):
        off = int.from_bytes(data[k * 4:k * 4 + 3], 'big')
        if not off: continue
        if box and rxz and not in_box(rxz[0] * 32 + (k & 31), rxz[1] * 32 + (k >> 5), box): continue
        p = off * 4096
        if p + 5 > len(data): continue
        ln = struct.unpack_from('>i', data, p)[0]; c = data[p + 4]
        if c != 2: continue
        try:
            raw = zlib.decompress(data[p + 5:p + 4 + ln])
        except Exception:
            continue
        d, _ = nbt(raw, 3 + struct.unpack_from('>H', raw, 1)[0], 10)
        yield d


def unpack(longs, bits, n):
    per = 64 // bits; mask = (1 << bits) - 1; out = []
    for L in longs:
        L &= (1 << 64) - 1
        for j in range(per):
            out.append((L >> (j * bits)) & mask)
            if len(out) == n: return out
    return out + [0] * (n - len(out))


def bits_for(n, lo):
    return max(lo, (n - 1).bit_length())


def section_blocks(sec):
    bs = sec.get('block_states') or {}
    pal = [e.get('Name', '?') for e in bs.get('palette') or []]
    if not pal: return [], None
    if len(pal) == 1 or not bs.get('data'): return pal, [0] * 4096
    return pal, unpack(bs['data'], bits_for(len(pal), 4), 4096)


def section_biomes(sec):
    bi = sec.get('biomes') or {}
    pal = bi.get('palette') or ['?']
    if len(pal) == 1 or not bi.get('data'): return pal, [0] * 64
    return pal, unpack(bi['data'], bits_for(len(pal), 1), 64)


def in_box(cx, cz, box):
    if not box: return True
    x0, z0, x1, z1 = box
    return min(x0, x1) <= cx * 16 + 15 and cx * 16 <= max(x0, x1) and min(z0, z1) <= cz * 16 + 15 and cz * 16 <= max(z0, z1)


def in_box_region(rxz, box):
    x0, z0, x1, z1 = box
    return min(x0, x1) <= rxz[0] * 512 + 511 and rxz[0] * 512 <= max(x0, x1) and min(z0, z1) <= rxz[1] * 512 + 511 \
        and rxz[1] * 512 <= max(z0, z1)


def full(d):
    return str(d.get('Status', '')).replace('minecraft:', '') == 'full'


def surface(d):
    hm = (d.get('Heightmaps') or {}).get('WORLD_SURFACE')
    return unpack(hm, 9, 256) if hm else None


def biome_at(secs, x, y, z):
    s = secs.get(y >> 4)
    if s is None: return '?'
    pal, idx = section_biomes(s)
    return pal[idx[(((y & 15) >> 2) * 4 + ((z & 15) >> 2)) * 4 + ((x & 15) >> 2)]]


def cmd_blocks(level, rx, box, band, near, fail_above, fail_depth):
    by_id, by_band, by_biome, by_depth = collections.Counter(), collections.Counter(), collections.Counter(), collections.Counter()
    chunks_hit, chunks_all = collections.Counter(), collections.Counter()
    near_n, total, above, shallow, max_y, min_depth = 0, 0, 0, 0, None, None
    nrx = re.compile(near) if near else None
    for f in sorted(glob.glob(os.path.join(level, 'region', '*.mca'))):
        for d in chunks(f, box):
            cx, cz = d.get('xPos', 0), d.get('zPos', 0)
            if not full(d) or not in_box(cx, cz, box): continue
            secs = {s.get('Y'): s for s in d.get('sections') or []}
            hm = surface(d)
            top = (hm[8 * 16 + 8] + MIN_Y - 1) if hm else 64
            sb = biome_at(secs, 8, top, 8)
            chunks_all[sb] += 1
            hit = False
            decoded = {}
            for sy, s in secs.items():
                pal, _ = section_blocks(s) if (s.get('block_states') or {}).get('palette') else ([], None)
                if not any(rx.search(p) for p in pal): continue
                pal, idx = section_blocks(s)
                decoded[sy] = (pal, idx)
                for k, v in enumerate(idx):
                    name = pal[v]
                    if not rx.search(name): continue
                    x, z, y = k & 15, (k >> 4) & 15, sy * 16 + (k >> 8)
                    hit = True; total += 1
                    by_id[name] += 1
                    b0 = (y // band) * band
                    by_band['%d..%d' % (b0, b0 + band - 1)] += 1
                    by_biome[biome_at(secs, x, y, z)] += 1
                    dep = ((hm[z * 16 + x] + MIN_Y - 1) - y) if hm else None
                    by_depth['?' if dep is None else ('<0' if dep < 0 else str(min(dep, 13)) if dep < 13 else '13+')] += 1
                    if dep is not None:
                        min_depth = dep if min_depth is None else min(min_depth, dep)
                        if fail_depth is not None and dep < fail_depth: shallow += 1
                    max_y = y if max_y is None else max(max_y, y)
                    if fail_above is not None and y > fail_above: above += 1
                    if nrx:
                        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
                            nx, ny, nz = x + dx, y + dy, z + dz
                            if not (0 <= nx < 16 and 0 <= nz < 16): continue
                            ns = ny >> 4
                            if ns not in decoded:
                                if ns not in secs: continue
                                decoded[ns] = section_blocks(secs[ns])
                            npal, nidx = decoded[ns]
                            if npal and nrx.search(npal[nidx[((ny & 15) * 16 + nz) * 16 + nx]]):
                                near_n += 1
                                break
            if hit: chunks_hit[sb] += 1
    res = {'total': total, 'by_id': dict(by_id), 'by_y_band': dict(sorted(by_band.items(), key=lambda kv: int(kv[0].split('..')[0]))),
           'by_biome': dict(by_biome.most_common()), 'by_depth': dict(by_depth), 'max_y': max_y, 'min_depth': min_depth,
           'chunks_with_match_by_surface_biome': {b: '%d/%d' % (chunks_hit[b], n) for b, n in chunks_all.most_common()},
           'chunks_read': sum(chunks_all.values())}
    if nrx: res['near_' + near] = near_n
    print('blocks /%s/: %d in %d chunks (max y %s, min depth %s)' % (rx.pattern, total, res['chunks_read'], max_y, min_depth))
    for k in ('by_id', 'by_y_band', 'by_biome', 'by_depth'):
        print('  %s: %s' % (k, ', '.join('%s=%s' % kv for kv in res[k].items()) or 'none'))
    print('  chunks with a match by surface biome: ' + ', '.join('%s=%s' % kv for kv in res['chunks_with_match_by_surface_biome'].items()))
    if nrx: print('  with a /%s/ neighbour: %d of %d' % (near, near_n, total))
    bad = []
    if fail_above is not None and above: bad.append('%d above y %d' % (above, fail_above))
    if fail_depth is not None and shallow: bad.append('%d with depth < %d' % (shallow, fail_depth))
    if bad: print('FAIL: ' + '; '.join(bad))
    return res, 1 if bad else 0


def cmd_entities(level, rx, box, field):
    biomes = {}
    for f in sorted(glob.glob(os.path.join(level, 'region', '*.mca'))):
        for d in chunks(f, box):
            if full(d) and in_box(d.get('xPos', 0), d.get('zPos', 0), box):
                biomes[(d.get('xPos', 0), d.get('zPos', 0))] = {s.get('Y'): s for s in d.get('sections') or []}
    by_id, by_biome = collections.Counter(), collections.Counter()
    for f in sorted(glob.glob(os.path.join(level, 'entities', '*.mca'))):
        for d in chunks(f, box):
            pos = d.get('Position') or [0, 0]
            if not in_box(pos[0], pos[1], box): continue
            for e in d.get('Entities') or []:
                eid = e.get('id', '?')
                if not rx.search(eid): continue
                key = eid + ('' if not field else '[%s=%s]' % (field, e.get(field)))
                by_id[key] += 1
                p = e.get('Pos') or [0, 0, 0]
                x, y, z = int(p[0] // 1), int(p[1] // 1), int(p[2] // 1)
                secs = biomes.get((x >> 4, z >> 4))
                by_biome[(biome_at(secs, x, y, z) if secs else '?', key)] += 1
    res = {'by_id': dict(by_id), 'by_biome': {'%s %s' % k: n for k, n in sorted(by_biome.items())}}
    print('entities /%s/: %d' % (rx.pattern, sum(by_id.values())))
    print('  by id: ' + (', '.join('%s=%d' % kv for kv in by_id.most_common()) or 'none'))
    for k, n in sorted(by_biome.items()):
        print('  %s %s: %d' % (k[0], k[1], n))
    return res, 0


def main():
    a = sys.argv[1:]
    if len(a) < 3 or a[0] not in ('blocks', 'entities'):
        sys.exit(__doc__)
    def opt(name, n=1, conv=str):
        if name not in a: return None
        i = a.index(name)
        v = [conv(x) for x in a[i + 1:i + 1 + n]]
        return v if n > 1 else v[0]
    box = opt('--box', 4, int)
    rx = re.compile(a[2])
    if a[0] == 'blocks':
        res, rc = cmd_blocks(a[1], rx, box, opt('--band', 1, int) or 8, opt('--near'), opt('--fail-above', 1, int),
                             opt('--fail-depth-below', 1, int))
    else:
        res, rc = cmd_entities(a[1], rx, box, opt('--field'))
    out = opt('--json')
    if out:
        json.dump(res, open(out, 'w'), indent=1)
    sys.exit(rc)


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Count blocks in a save's region files (feature_writeup/briefs/worldgen_biomes_netherblocks.md #4, test NW-21).

    python3 tools/block_census.py <world dir> <block id>... [--dim overworld|nether|end] [--max-files N] [--all-status]

Reads every chunk's section palettes (1.18+ `block_states`); a section whose palette lacks every listed block is
skipped without unpacking, so a big save is quick. Only fully generated chunks count unless --all-status. Prints
totals per block, then the chunks with hits (chunk x z, block, count, y range). Glowstone placed by players counts
too: read the hit list before blaming worldgen. No dependencies.
"""
import collections
import glob
import os
import struct
import sys
import zlib

DIMS = {'overworld': 'region', 'nether': 'DIM-1/region', 'end': 'DIM1/region'}


def nbt(b, i, t):
    if t == 1: return struct.unpack_from('>b', b, i)[0], i + 1
    if t == 2: return struct.unpack_from('>h', b, i)[0], i + 2
    if t == 3: return struct.unpack_from('>i', b, i)[0], i + 4
    if t == 4: return struct.unpack_from('>q', b, i)[0], i + 8
    if t == 5: return None, i + 4
    if t == 6: return None, i + 8
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
    if t == 11: n = struct.unpack_from('>i', b, i)[0]; return None, i + 4 + 4 * n
    if t == 12: n = struct.unpack_from('>i', b, i)[0]; return list(struct.unpack_from('>%dq' % n, b, i + 4)), i + 4 + 8 * n
    raise ValueError(f'nbt tag {t}')


def chunks(path):
    data = open(path, 'rb').read()
    for k in range(1024):
        off = int.from_bytes(data[k * 4:k * 4 + 3], 'big')
        if not off:
            continue
        p = off * 4096
        if p + 5 > len(data):
            continue
        ln = struct.unpack_from('>i', data, p)[0]
        if data[p + 4] != 2:
            continue
        try:
            raw = zlib.decompress(data[p + 5:p + 4 + ln])
            d, _ = nbt(raw, 3 + struct.unpack_from('>H', raw, 1)[0], 10)
        except Exception:
            continue
        yield d


def unpack(longs, bits, n):
    per = 64 // bits; mask = (1 << bits) - 1; out = []
    for v in longs:
        v &= (1 << 64) - 1
        for j in range(per):
            out.append((v >> (j * bits)) & mask)
            if len(out) == n:
                return out
    return out


def main():
    args = sys.argv[1:]
    opt = lambda k, d=None: args[args.index(k) + 1] if k in args else d
    dim, max_files, all_status = opt('--dim', 'overworld'), opt('--max-files'), '--all-status' in args
    pos = [a for i, a in enumerate(args) if not a.startswith('--') and (i == 0 or args[i - 1] not in ('--dim', '--max-files'))]
    if len(pos) < 2:
        sys.exit(__doc__)
    world, wanted = pos[0], set(pos[1:])
    files = sorted(glob.glob(os.path.join(world, DIMS[dim], '*.mca')))[:int(max_files) if max_files else None]
    totals, hits, n_chunks = collections.Counter(), [], 0
    for f in files:
        for d in chunks(f):
            if not all_status and d.get('Status') not in ('minecraft:full', 'full'):
                continue
            n_chunks += 1
            per = collections.defaultdict(lambda: [0, 9999, -9999])
            for s in d.get('sections') or []:
                bs = s.get('block_states') or {}
                pal = [p.get('Name') for p in bs.get('palette') or []]
                idx = {i for i, nm in enumerate(pal) if nm in wanted}
                if not idx:
                    continue
                y0 = s.get('Y', 0) * 16
                if len(pal) == 1 or not bs.get('data'):
                    ids = [0] * 4096
                else:
                    ids = unpack(bs['data'], max(4, (len(pal) - 1).bit_length()), 4096)
                for n, v in enumerate(ids):
                    if v in idx:
                        r = per[pal[v]]; y = y0 + (n >> 8)
                        r[0] += 1; r[1] = min(r[1], y); r[2] = max(r[2], y)
            for name, (c, lo, hi) in per.items():
                totals[name] += c
                hits.append((d.get('xPos'), d.get('zPos'), name, c, lo, hi))
    print(f'{world} {dim}: {len(files)} region files, {n_chunks} chunks read')
    for b in sorted(wanted):
        print(f'  {b}: {totals[b]} in {sum(1 for h in hits if h[2] == b)} chunks')
    for x, z, name, c, lo, hi in sorted(hits, key=lambda h: (h[2], -h[3])):
        print(f'  chunk {x} {z} (block {x * 16} {z * 16}): {name} {c}, y {lo}..{hi}')


if __name__ == '__main__':
    main()

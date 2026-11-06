#!/usr/bin/env python3
"""Offline AE2 meteorite height census of a scratch world (scratch queue SQ-WGE-13, plan_worldgen_edit.md C6).
    meteor_heights.py LEVEL_DIM [--box X0 Z0 X1 Z1] [--json FILE]
LEVEL_DIM is the dimension folder that holds region/ (srv/<level> for the Overworld).
Finds every ae2:sky_stone_block in full chunks, groups blocks within 12 blocks of each other into one meteorite,
and compares each meteorite with the ground: the median OCEAN_FLOOR height (top solid or liquid-free block; falls back to WORLD_SURFACE) of the columns in a ring 6 to 10
blocks outside the meteorite's box (the ring misses the sphere, so a floating sphere does not count as ground).
  underground: the meteorite's top is 3 or more blocks under the ground
  air:         its bottom is 8 or more blocks over the ground
  surface:     anything else (AE2's own placement)
Prints one line per meteorite and the shares. Exit 0 always (the row judges the shares).
"""
import collections, glob, json, os, re, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import region_blocks as rb  # noqa: E402

SKY = re.compile(r'^ae2:sky_stone_block$')


def main():
    a = sys.argv[1:]
    if not a:
        sys.exit(__doc__)
    level = a[0]
    box = [int(x) for x in a[a.index('--box') + 1:a.index('--box') + 5]] if '--box' in a else None
    out = a[a.index('--json') + 1] if '--json' in a else None
    pts = []
    heights = {}  # (x, z) -> world surface y, for every column of every full chunk read
    for f in sorted(glob.glob(os.path.join(level, 'region', '*.mca'))):
        for d in rb.chunks(f, box):
            cx, cz = d.get('xPos', 0), d.get('zPos', 0)
            if not rb.full(d) or not rb.in_box(cx, cz, box):
                continue
            hmap = (d.get('Heightmaps') or {}).get('OCEAN_FLOOR') or (d.get('Heightmaps') or {}).get('WORLD_SURFACE')
            hm = rb.unpack(hmap, 9, 256) if hmap else None  # OCEAN_FLOOR: the sea surface is not ground (r1103w)
            if hm:
                for k in range(256):
                    heights[(cx * 16 + (k & 15), cz * 16 + (k >> 4))] = hm[k] + rb.MIN_Y - 1
            for s in d.get('sections') or []:
                pal = [e.get('Name', '?') for e in (s.get('block_states') or {}).get('palette') or []]
                if not any(SKY.search(p) for p in pal):
                    continue
                pal, idx = rb.section_blocks(s)
                sy = s.get('Y')
                for k, v in enumerate(idx):
                    if SKY.search(pal[v]):
                        pts.append((cx * 16 + (k & 15), sy * 16 + (k >> 8), cz * 16 + ((k >> 4) & 15)))
    # group: grid hashing, union of points within 12 blocks (Chebyshev)
    cell = collections.defaultdict(list)
    for i, p in enumerate(pts):
        cell[(p[0] // 12, p[1] // 12, p[2] // 12)].append(i)
    parent = list(range(len(pts)))

    def find(i):
        while parent[i] != i:
            parent[i] = parent[parent[i]]
            i = parent[i]
        return i
    for (gx, gy, gz), ids in cell.items():
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    for j in cell.get((gx + dx, gy + dy, gz + dz), ()):
                        for i in ids[:1]:
                            ri, rj = find(i), find(j)
                            if ri != rj:
                                parent[ri] = rj
        for i in ids[1:]:
            ri, rj = find(i), find(ids[0])
            if ri != rj:
                parent[ri] = rj
    groups = collections.defaultdict(list)
    for i in range(len(pts)):
        groups[find(i)].append(pts[i])
    rows, kinds = [], collections.Counter()
    for g in groups.values():
        if len(g) < 8:  # loose blocks (fallout, a structure's decoration), not a meteorite
            continue
        xs, ys, zs = [p[0] for p in g], [p[1] for p in g], [p[2] for p in g]
        x0, x1, z0, z1 = min(xs), max(xs), min(zs), max(zs)
        ring = sorted(heights[(x, z)] for x in range(x0 - 10, x1 + 11) for z in range(z0 - 10, z1 + 11)
                      if (x < x0 - 5 or x > x1 + 5 or z < z0 - 5 or z > z1 + 5) and (x, z) in heights)
        if not ring:
            continue
        ground = ring[len(ring) // 2]
        kind = 'underground' if max(ys) <= ground - 3 else 'air' if min(ys) >= ground + 8 else 'surface'
        kinds[kind] += 1
        rows.append({'x': sum(xs) // len(xs), 'z': sum(zs) // len(zs), 'y_min': min(ys), 'y_max': max(ys),
                     'ground': ground, 'blocks': len(g), 'kind': kind, 'over_ground': min(ys) - ground})
    rows.sort(key=lambda r: (r['x'], r['z']))
    for r in rows:
        print('meteorite %(x)d %(z)d: y %(y_min)d..%(y_max)d, ground %(ground)d, %(blocks)d blocks -> %(kind)s'
              ' (bottom %(over_ground)+d)' % r)
    n = sum(kinds.values())
    print('meteorites %d: ' % n + ', '.join('%s %d (%.0f %%)' % (k, kinds[k], 100.0 * kinds[k] / max(n, 1))
                                             for k in ('surface', 'underground', 'air')))
    air = [r['over_ground'] for r in rows if r['kind'] == 'air']
    if air:
        print('air bottoms over ground: min %d, max %d' % (min(air), max(air)))
    if out:
        json.dump({'meteorites': rows, 'kinds': dict(kinds)}, open(out, 'w'), indent=1)


if __name__ == '__main__':
    main()

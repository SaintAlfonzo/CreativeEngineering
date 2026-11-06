#!/usr/bin/env python3
"""Prints qrun.py batch lines that probe a pregenerated square tile by tile with /zq under (SQ-EFB, run r1102b).

    qb_tiles.py VAR RADIUS TAG [FILTER] [--dim DIM]

VAR is a capture name whose groups 1 and 3 are the square's centre x and z ({VAR1} {VAR3}), RADIUS the square's
half-width in blocks (the Chunky radius), TAG a short unique word for the say markers, FILTER the /zq under id text
('-' = every start, the default). Tiles are 13 x 13 chunks (rChunks 6, the probe's 30-start cap per call); each tile
is force-loaded with a box 120 blocks out from its centre, clamped to the square so nothing new generates (at most
16 x 16 = 256 chunks, the forceload limit), probed, then released. A "say zqtile TAG n" after each probe marks its end (the server runs console commands in order, so the
marker prints only after the probe finished); waitlog waits on that marker, not on the probe's own line, because a
row's earlier tiles would already match it.
"""
import sys


def main():
    a = sys.argv[1:]
    dim = None
    if '--dim' in a:
        i = a.index('--dim'); dim = a[i + 1]; del a[i:i + 2]
    if len(a) < 3:
        sys.exit(__doc__)
    var, radius, tag = a[0], int(a[1]), a[2]
    filt = a[3] if len(a) > 3 else '-'
    step = 208
    m = -(-2 * radius // step)                # tiles per side, centred on the square
    offs = [-(m - 1) * step // 2 + k * step for k in range(m)]

    def v(g, d):
        return '{%s%d}' % (var, g) if d == 0 else '{%s%d%+d}' % (var, g, d)

    pre = 'execute in %s run ' % dim if dim else ''
    k = 0
    for dx in offs:
        for dz in offs:
            k += 1
            lo = lambda d: max(d - 120, -radius)
            hi = lambda d: min(d + 120, radius)
            box = '%s %s %s %s' % (v(1, lo(dx)), v(3, lo(dz)), v(1, hi(dx)), v(3, hi(dz)))
            print('> %sforceload add %s' % (pre, box))
            print('sleep 10')
            print('> zq under %s %s 6 %s%s' % (v(1, dx), v(3, dz), filt, (' ' + dim) if dim else ''))
            print('> say zqtile %s %d' % (tag, k))
            print('waitlog zqtile %s %d$ 600' % (tag, k))
            print('> %sforceload remove %s' % (pre, box))


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Biome shares of a /dumpclimate sample (SQ-WGE-01b, run r1102b).

    climate_share.py FILE.json.gz [--layer 0] [--min BIOME LO HI]...

FILE is a dump written by creativedevtools' /dumpclimate (kubejs/generation_scripts/worldgen/climate/<ns>__<dim>.json.gz):
one point per line, `live_biome` (the biome the live source picked) last. Prints every biome's share of the points
in the layer (0 = surface, the default). --min BIOME LO HI: exit 1 unless BIOME's share (in %) is within LO..HI
(repeatable). Pure reader.
"""
import gzip
import json
import sys


def main():
    a = sys.argv[1:]
    if not a:
        sys.exit(__doc__)
    path, layer, bounds = a[0], 0, []
    i = 1
    while i < len(a):
        if a[i] == '--layer':
            layer = int(a[i + 1]); i += 2
        elif a[i] == '--min':
            bounds.append((a[i + 1], float(a[i + 2]), float(a[i + 3]))); i += 4
        else:
            sys.exit('unknown option ' + a[i])
    d = json.loads(gzip.open(path, 'rt').read())
    biomes = d['biomes']
    counts = {}
    n = 0
    for p in d['points']:
        if p[0] != layer:
            continue
        n += 1
        b = biomes[p[-1]]
        counts[b] = counts.get(b, 0) + 1
    print('%s: %s, %d points in layer %d, seed %s' % (path, d.get('dimension'), n, layer, d.get('seed')))
    for b, c in sorted(counts.items(), key=lambda kv: -kv[1]):
        print('  %-40s %6d  %6.2f %%' % (b, c, 100.0 * c / max(n, 1)))
    rc = 0
    for b, lo, hi in bounds:
        s = 100.0 * counts.get(b, 0) / max(n, 1)
        ok = lo <= s <= hi
        print('%s %s %.2f %% (want %g..%g)' % ('ok  ' if ok else 'FAIL', b, s, lo, hi))
        rc |= 0 if ok else 1
    sys.exit(rc)


if __name__ == '__main__':
    main()

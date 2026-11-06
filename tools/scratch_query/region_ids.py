#!/usr/bin/env python3
"""Counts ids matching REGEX in every chunk of a world's region files (block palettes, structure starts, items).
    region_ids.py WORLD_REGION_DIR REGEX
Prints {region file: {id: count}}. Scratch-run helper (scratch_run_2026-11-01.md 7)."""
import glob, os, re, sys, zlib
d, rx = sys.argv[1], re.compile(sys.argv[2].encode())
out = {}
for f in sorted(glob.glob(os.path.join(d, "*.mca"))):
    b = open(f, "rb").read()
    for i in range(1024):
        off = int.from_bytes(b[i*4:i*4+3], "big")
        if not off:
            continue
        p = off * 4096
        ln = int.from_bytes(b[p:p+4], "big")
        try:
            raw = zlib.decompress(b[p+5:p+4+ln])
        except Exception:
            continue
        for m in rx.findall(raw):
            r = out.setdefault(os.path.basename(f), {})
            r[m.decode()] = r.get(m.decode(), 0) + 1
print(out or "none")

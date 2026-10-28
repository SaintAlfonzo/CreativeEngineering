#!/usr/bin/env python3
"""Counts saved entities in a world's entities/*.mca: by type, item entities by item id, falling blocks by block.

    python3 entity_census.py WORLD_DIR [DIM_SUBDIR]
"""
import collections
import glob
import os
import struct
import sys
import zlib


def nbt(b, i, t):
    fmt = {1: ">b", 2: ">h", 3: ">i", 4: ">q", 5: ">f", 6: ">d"}
    if t in fmt:
        return struct.unpack_from(fmt[t], b, i)[0], i + struct.calcsize(fmt[t])
    if t == 7:
        n = struct.unpack_from(">i", b, i)[0]
        return None, i + 4 + n
    if t == 8:
        n = struct.unpack_from(">H", b, i)[0]
        return b[i + 2:i + 2 + n].decode("utf8", "replace"), i + 2 + n
    if t == 9:
        et = b[i]
        n = struct.unpack_from(">i", b, i + 1)[0]
        i += 5
        out = []
        for _ in range(n):
            v, i = nbt(b, i, et)
            out.append(v)
        return out, i
    if t == 10:
        d = {}
        while True:
            tt = b[i]
            i += 1
            if tt == 0:
                return d, i
            n = struct.unpack_from(">H", b, i)[0]
            name = b[i + 2:i + 2 + n].decode("utf8", "replace")
            i += 2 + n
            d[name], i = nbt(b, i, tt)
    if t == 11:
        n = struct.unpack_from(">i", b, i)[0]
        return None, i + 4 + 4 * n
    if t == 12:
        n = struct.unpack_from(">i", b, i)[0]
        return None, i + 4 + 8 * n
    raise ValueError(t)


def chunks(path):
    data = open(path, "rb").read()
    for k in range(1024):
        off = int.from_bytes(data[k * 4:k * 4 + 3], "big")
        if not off:
            continue
        p = off * 4096
        ln = struct.unpack_from(">i", data, p)[0]
        if data[p + 4] != 2:
            continue
        raw = zlib.decompress(data[p + 5:p + 4 + ln])
        d, _ = nbt(raw, 3 + struct.unpack_from(">H", raw, 1)[0], 10)
        yield d


def main():
    world = sys.argv[1]
    sub = sys.argv[2] if len(sys.argv) > 2 else ""
    types, items, falling, ys = collections.Counter(), collections.Counter(), collections.Counter(), []
    nchunks = 0
    for f in glob.glob(os.path.join(world, sub, "entities", "*.mca")):
        for c in chunks(f):
            nchunks += 1
            for e in c.get("Entities") or []:
                types[e.get("id")] += 1
                if e.get("id") == "minecraft:item":
                    items[(e.get("Item") or {}).get("id")] += 1
                    ys.append(e["Pos"][1])
                elif e.get("id") == "minecraft:falling_block":
                    falling[(e.get("BlockState") or {}).get("Name")] += 1
    print(f"{nchunks} entity chunks")
    for title, c in (("types", types), ("items", items), ("falling blocks", falling)):
        print(f"{title}: {sum(c.values())}")
        for k, v in c.most_common(12):
            print(f"  {v:6} {k}")
    if ys:
        ys.sort()
        print(f"item y: min {ys[0]:.0f} median {ys[len(ys) // 2]:.0f} max {ys[-1]:.0f}; below y 50: {sum(y < 50 for y in ys)}")


if __name__ == "__main__":
    main()

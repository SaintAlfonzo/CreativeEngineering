#!/usr/bin/env python3
"""Offline census of a saved world for the Creative Delight namespace move (plan_cd_namespace.md 5.1, SQ-CDNS rows).

Reads the save itself, so stop the server (or /save-all flush) first. Forceload the fixture area so every chunk is read
and saved by the boot under test: a stack that decoded to air is gone from the next save, which is what a count drop means.

  cdns_probe.py scan WORLD_DIR OUT.json [--ids SWAP.json ...]
  cdns_probe.py compare BASELINE.json NOW.json

scan: per holder (block entity type, entity type, "player", "data/<file>") and per moved id, the item count found. Ids are
keyed by path alone, so a baseline under creativeengineering: compares with a save under creativedelight:. Also a block
census (decoded from chunk palettes), counts per namespace, capability keys found and plain strings naming a moved id.
compare: exit 0 when holders and blocks are equal; prints every difference.

Default id list: every "replaces" pair of data/creativedelight/swaps/{cd_namespace,wooden_buckets}.json.
"""
import gzip
import io
import json
import os
import struct
import sys
import zlib
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SWAPS = os.path.join(ROOT, "development/creativeDelight/src/main/resources/data/creativedelight/swaps")
OLD = "creativeengineering"
NEW = "creativedelight"
CAPS = {f"{ns}:{c}" for ns in (OLD, NEW) for c in ("corner_storage", "remainder_buffer")}


def read_tag(f, kind):
    if kind == 1:
        return struct.unpack(">b", f.read(1))[0]
    if kind == 2:
        return struct.unpack(">h", f.read(2))[0]
    if kind == 3:
        return struct.unpack(">i", f.read(4))[0]
    if kind == 4:
        return struct.unpack(">q", f.read(8))[0]
    if kind == 5:
        return struct.unpack(">f", f.read(4))[0]
    if kind == 6:
        return struct.unpack(">d", f.read(8))[0]
    if kind == 7:
        n = struct.unpack(">i", f.read(4))[0]
        f.read(n)
        return None
    if kind == 8:
        n = struct.unpack(">H", f.read(2))[0]
        return f.read(n).decode("utf-8", "replace")
    if kind == 9:
        sub = f.read(1)[0]
        n = struct.unpack(">i", f.read(4))[0]
        return [read_tag(f, sub) for _ in range(n)]
    if kind == 10:
        out = {}
        while True:
            sub = f.read(1)[0]
            if sub == 0:
                return out
            n = struct.unpack(">H", f.read(2))[0]
            name = f.read(n).decode("utf-8", "replace")
            out[name] = read_tag(f, sub)
    if kind == 11:
        n = struct.unpack(">i", f.read(4))[0]
        f.read(4 * n)
        return None
    if kind == 12:
        n = struct.unpack(">i", f.read(4))[0]
        return list(struct.unpack(f">{n}q", f.read(8 * n)))
    raise ValueError(f"bad tag {kind}")


def read_root(data):
    f = io.BytesIO(data)
    kind = f.read(1)[0]
    if kind != 10:
        raise ValueError("root is not a compound")
    n = struct.unpack(">H", f.read(2))[0]
    f.read(n)
    return read_tag(f, 10)


def read_dat(path):
    with open(path, "rb") as f:
        raw = f.read()
    return read_root(gzip.decompress(raw) if raw[:2] == b"\x1f\x8b" else raw)


def region_chunks(path):
    with open(path, "rb") as f:
        data = f.read()
    if len(data) < 8192:
        return
    for i in range(1024):
        loc = struct.unpack(">I", data[i * 4:i * 4 + 4])[0]
        offset, sectors = loc >> 8, loc & 255
        if not offset or not sectors:
            continue
        start = offset * 4096
        length = struct.unpack(">I", data[start:start + 4])[0]
        comp = data[start + 4]
        body = data[start + 5:start + 4 + length]
        if comp == 1:
            yield read_root(gzip.decompress(body))
        elif comp == 2:
            yield read_root(zlib.decompress(body))


class Census:
    def __init__(self, ids):
        self.ids = ids
        self.holders = defaultdict(lambda: defaultdict(int))
        self.blocks = defaultdict(int)
        self.ns = defaultdict(int)
        self.caps = defaultdict(int)
        self.strings = defaultdict(int)

    def path(self, value):
        if not isinstance(value, str) or ":" not in value:
            return None
        ns, p = value.split(":", 1)
        return p if ns in (OLD, NEW) and p in self.ids else None

    def walk(self, node, holder):
        if isinstance(node, dict):
            for key, value in node.items():
                if key == "ForgeCaps" and isinstance(value, dict):
                    for cap in value:
                        if cap in CAPS:
                            self.caps[cap] += 1
            item = self.path(node.get("id"))
            if item is not None:
                count = node.get("Count", 1)
                self.holders[holder][item] += count if isinstance(count, int) else 1
                self.ns[node["id"].split(":", 1)[0]] += 1
            for key, value in node.items():
                if key == "id" or (isinstance(value, str) and self.path(value) is None):
                    continue
                if isinstance(value, str):
                    self.strings[f"{holder} {key}={value}"] += 1
                else:
                    self.walk(value, holder)
        elif isinstance(node, list):
            for value in node:
                if isinstance(value, (dict, list)):
                    self.walk(value, holder)
                elif isinstance(value, str) and self.path(value) is not None:
                    self.strings[f"{holder} list={value}"] += 1

    def palette(self, section):
        states = section.get("block_states") or {}
        palette = states.get("palette") or []
        names = [p.get("Name", "") for p in palette]
        wanted = [i for i, n in enumerate(names) if self.path(n) is not None]
        if not wanted:
            return
        data = states.get("data")
        if data is None:
            self.record_block(names[0], 4096)
            return
        bits = max(4, len(bin(len(palette) - 1)) - 2)
        per_long = 64 // bits
        mask = (1 << bits) - 1
        counts = defaultdict(int)
        done = 0
        for word in data:
            word &= (1 << 64) - 1
            for _ in range(per_long):
                if done == 4096:
                    break
                counts[(word >> 0) & mask] += 1
                word >>= bits
                done += 1
        for i in wanted:
            if counts[i]:
                self.record_block(names[i], counts[i])

    def record_block(self, name, count):
        self.blocks[self.path(name)] += count
        self.ns[name.split(":", 1)[0]] += count

    def chunk(self, tag):
        root = tag.get("Level", tag)
        for section in root.get("sections") or []:
            self.palette(section)
        for be in root.get("block_entities") or []:
            kind = be.get("id", "?")
            p = self.path(kind)
            holder = f"be:{p}" if p else f"be:{kind}"
            copy = dict(be)
            copy.pop("id", None)
            if p:
                self.holders[holder]["<block entity>"] += 1
                self.ns[kind.split(":", 1)[0]] += 1
            self.walk(copy, holder)

    def entity_file(self, tag):
        for entity in tag.get("Entities") or []:
            kind = entity.get("id", "?")
            copy = dict(entity)
            copy.pop("id", None)
            self.walk(copy, f"entity:{kind}")

    def result(self):
        return {
            "holders": {h: dict(sorted(c.items())) for h, c in sorted(self.holders.items())},
            "blocks": dict(sorted(self.blocks.items())),
            "ns": dict(self.ns),
            "caps": dict(self.caps),
            "strings": dict(sorted(self.strings.items())),
        }


def load_ids(files):
    ids = set()
    for path in files:
        with open(path, encoding="utf-8") as f:
            for survivor, olds in json.load(f).get("replaces", {}).items():
                ids.add(survivor.split(":", 1)[1])
                ids.update(o.split(":", 1)[1] for o in olds)
    return ids


def scan(world, out, files):
    census = Census(load_ids(files))
    dims = [world] + [os.path.join(world, d) for d in ("DIM-1", "DIM1")]
    extra = os.path.join(world, "dimensions")
    for dp, dn, _ in os.walk(extra):
        if "region" in dn:
            dims.append(dp)
    for dim in dims:
        region = os.path.join(dim, "region")
        if os.path.isdir(region):
            for name in sorted(os.listdir(region)):
                if name.endswith(".mca"):
                    for tag in region_chunks(os.path.join(region, name)):
                        census.chunk(tag)
        entities = os.path.join(dim, "entities")
        if os.path.isdir(entities):
            for name in sorted(os.listdir(entities)):
                if name.endswith(".mca"):
                    for tag in region_chunks(os.path.join(entities, name)):
                        census.entity_file(tag)
    players = os.path.join(world, "playerdata")
    if os.path.isdir(players):
        for name in sorted(os.listdir(players)):
            if name.endswith(".dat"):
                census.walk(read_dat(os.path.join(players, name)), "player")
    data = os.path.join(world, "data")
    for dp, _, files_here in os.walk(data):
        for name in sorted(files_here):
            if name.endswith(".dat"):
                path = os.path.join(dp, name)
                try:
                    census.walk(read_dat(path), "data/" + os.path.relpath(path, data))
                except (ValueError, OSError, EOFError, zlib.error, struct.error, IndexError) as e:
                    print(f"skipped {path}: {e}")
    result = census.result()
    with open(out, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=1)
    print(f"holders {len(result['holders'])}, moved block ids {len(result['blocks'])}, ns {result['ns']}, caps {result['caps']}, strings {len(result['strings'])}")


def compare(base_path, now_path):
    with open(base_path, encoding="utf-8") as f:
        base = json.load(f)
    with open(now_path, encoding="utf-8") as f:
        now = json.load(f)
    diffs = []
    for holder in sorted(set(base["holders"]) | set(now["holders"])):
        a, b = base["holders"].get(holder, {}), now["holders"].get(holder, {})
        for item in sorted(set(a) | set(b)):
            if a.get(item, 0) != b.get(item, 0):
                diffs.append(f"{holder} {item}: {a.get(item, 0)} -> {b.get(item, 0)}")
    for block in sorted(set(base["blocks"]) | set(now["blocks"])):
        if base["blocks"].get(block, 0) != now["blocks"].get(block, 0):
            diffs.append(f"block {block}: {base['blocks'].get(block, 0)} -> {now['blocks'].get(block, 0)}")
    print("\n".join(diffs) if diffs else "EQUAL")
    print(f"namespaces now: {now['ns']}; caps now: {now['caps']}; strings now: {len(now['strings'])}")
    sys.exit(1 if diffs else 0)


def main():
    args = sys.argv[1:]
    if len(args) >= 3 and args[0] == "scan":
        files = args[args.index("--ids") + 1:] if "--ids" in args else [
            os.path.join(SWAPS, n) for n in ("cd_namespace.json", "wooden_buckets.json")]
        scan(args[1], args[2], files)
    elif len(args) == 3 and args[0] == "compare":
        compare(args[1], args[2])
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Counts saved chunk statuses in a box of chunks (run r1113tpf, SQ-TPF-19: MC-224729 proto chunks saved at shutdown).

    tpf_chunkstatus.py REGION_DIR CX0 CZ0 CX1 CZ1

Reads every chunk of the box (chunk coordinates, inclusive) from the region files and prints one line:
    [tpf-chunks] full=N proto=N missing=N statuses={status: count}
"proto" = saved with any status other than full. Only the Status string tag is read (raw NBT scan).
"""
import collections
import os
import re
import struct
import sys
import zlib
import gzip

STATUS = re.compile(rb"\x08\x00\x06Status(..)", re.S)


def chunk_bytes(region_dir, cx, cz):
	path = os.path.join(region_dir, "r.%d.%d.mca" % (cx >> 5, cz >> 5))
	if not os.path.exists(path):
		return None
	with open(path, "rb") as f:
		idx = ((cx & 31) + (cz & 31) * 32) * 4
		f.seek(idx)
		loc = struct.unpack(">I", f.read(4))[0]
		off, count = loc >> 8, loc & 0xFF
		if off == 0 or count == 0:
			return None
		f.seek(off * 4096)
		length, comp = struct.unpack(">IB", f.read(5))
		data = f.read(length - 1)
	if comp == 2:
		return zlib.decompress(data)
	if comp == 1:
		return gzip.decompress(data)
	return data


def main():
	d = sys.argv[1]
	x0, z0, x1, z1 = map(int, sys.argv[2:6])
	stats = collections.Counter()
	missing = 0
	for cx in range(min(x0, x1), max(x0, x1) + 1):
		for cz in range(min(z0, z1), max(z0, z1) + 1):
			raw = chunk_bytes(d, cx, cz)
			if raw is None:
				missing += 1
				continue
			m = STATUS.search(raw)
			if not m:
				stats["?"] += 1
				continue
			n = struct.unpack(">H", m.group(1))[0]
			start = m.end()
			stats[raw[start:start + n].decode("utf-8", "replace").replace("minecraft:", "")] += 1
	full = stats.get("full", 0)
	proto = sum(v for k, v in stats.items() if k != "full")
	print("[tpf-chunks] full=%d proto=%d missing=%d statuses=%s" % (full, proto, missing, dict(stats)))


if __name__ == "__main__":
	main()

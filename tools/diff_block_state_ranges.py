#!/usr/bin/env python3
"""Compare the block-state id range files written by the patcherMod debug switch.

Enable [debug.block_state_sync] enabled=true in config/creativedevtools-common.toml on the
client and on the dedicated server. Each side writes logs/cdev_block_state_ranges_<side>.txt (one
line per block: first state id, state count, registry name). Block ids are synced by Forge but
state COUNTS are not, so the first block whose count differs shifts every id after it.

    diff_block_state_ranges.py client.txt server.txt [--id N ...]

Prints every block whose count differs (the first one is the root cause; later ones only show the
shift), the blocks only one side has, and, for each --id (taken from the client's
"unresolvable block-state id" log lines), which block the SERVER says that id belongs to.
"""
import argparse
import bisect
import sys


def load(path):
    header, rows = "", []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if line.startswith("# side="):
                header = line
            if not line or line.startswith("#"):
                continue
            start, count, name = line.split("\t")
            rows.append((int(start), int(count), name))
    return header, rows


def owner(rows, starts, state_id):
    i = bisect.bisect_right(starts, state_id) - 1
    if i < 0:
        return None
    start, count, name = rows[i]
    return (name, start, count) if state_id < start + count else None


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("client")
    ap.add_argument("server")
    ap.add_argument("--id", type=int, action="append", default=[], help="a state id the client could not resolve")
    a = ap.parse_args()

    ch, crows = load(a.client)
    sh, srows = load(a.server)
    print("client:", ch)
    print("server:", sh)
    cmap = {n: (s, c) for s, c, n in crows}
    smap = {n: (s, c) for s, c, n in srows}

    only_c = [n for n in cmap if n not in smap]
    only_s = [n for n in smap if n not in cmap]
    if only_c:
        print(f"\nblocks only the CLIENT has ({len(only_c)}): " + ", ".join(only_c[:20]))
    if only_s:
        print(f"\nblocks only the SERVER has ({len(only_s)}): " + ", ".join(only_s[:20]))

    diffs = [(smap[n][0], n, cmap[n][1], smap[n][1]) for n in smap if n in cmap and cmap[n][1] != smap[n][1]]
    diffs.sort()
    print(f"\nblocks whose state count differs: {len(diffs)}")
    for i, (sid, n, cc, sc) in enumerate(diffs[:40]):
        print(f"  {'ROOT ' if i == 0 else '     '}{n}: client {cc} states, server {sc} (server first id {sid})")

    sstarts = [r[0] for r in srows]
    for sid in a.id:
        o = owner(srows, sstarts, sid)
        print(f"\nid {sid} on the server: " + (f"{o[0]} (states {o[1]}..{o[1] + o[2] - 1})" if o else "not in the server's table either"))
        c = owner(crows, [r[0] for r in crows], sid)
        print(f"id {sid} on the client: " + (f"{c[0]}" if c else "not in the client's table"))

    if not (only_c or only_s or diffs):
        print("\nNo differences: both sides assign the same ids. The bad id then comes from something other than a registry mismatch.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
"""Startup and world-join timings from client or server logs (latest.log and logs/*.log.gz).

    python3 tools/perf/log_timings.py [--months] [LOG_DIR ...]

Client rows: launch -> title screen (Sound engine started), then each single-player world join
(Starting integrated minecraft server -> "joined the game") or server connect (Connecting to -> joined).
Server rows: launch -> "Done (". Times are wall clock from the log's [HH:MM:SS] stamps.
"""
import glob
import gzip
import os
import re
import sys

TS = re.compile(r"^\[(\d\d):(\d\d):(\d\d)")


def sec(line):
    m = TS.match(line)
    return int(m[1]) * 3600 + int(m[2]) * 60 + int(m[3]) if m else None


def lines(path):
    op = gzip.open if path.endswith(".gz") else open
    with op(path, "rt", errors="replace") as f:
        yield from f


def scan(path):
    start = title = sp_start = None
    out = {"file": os.path.basename(path), "title": None, "joins": [], "done": None, "kind": "?"}
    last = None
    for l in lines(path):
        t = sec(l)
        if t is None:
            continue
        if last is not None and t < last:
            t += 86400
        last = t
        if start is None:
            start = t
        if "Sound engine started" in l and title is None:
            title = t
            out["kind"] = "client"
            out["title"] = t - start
        elif "Starting integrated minecraft server" in l or "Connecting to " in l:
            sp_start = t
        elif "joined the game" in l and sp_start is not None:
            out["joins"].append(t - sp_start)
            sp_start = None
        elif "Done (" in l and "For help" in l and out["done"] is None:
            out["kind"] = "server"
            out["done"] = t - start
    return out


def median(v):
    v = sorted(v)
    return v[len(v) // 2] if v else None


def months(rows):
    by = {}
    for r in rows:
        m = r["file"][:7] if r["file"][:2] == "20" else "latest"
        b = by.setdefault(m, ([], []))
        val = r["title"] if r["kind"] == "client" else r["done"]
        if val is not None and val < 3600:
            b[0].append(val)
        # joins over 5 min are a world screen left open, not a load
        b[1].extend(j for j in r["joins"] if j < 300)
    print("month    launches  median start  joins  median join")
    for m, (s, j) in sorted(by.items()):
        print(f"{m:8} {len(s):8}  {median(s) or '-':>11}s  {len(j):5}  {median(j) or '-':>10}s")


def main():
    args = sys.argv[1:]
    summary = "--months" in args
    dirs = [a for a in args if a != "--months"] or ["logs"]
    files = []
    for d in dirs:
        files += glob.glob(os.path.join(d, "20*.log.gz")) + glob.glob(os.path.join(d, "latest.log"))
    rows = [scan(f) for f in sorted(files, key=os.path.getmtime)]
    if summary:
        return months(rows)
    for r in rows:
        if r["title"] is None and r["done"] is None:
            continue
        val = r["title"] if r["kind"] == "client" else r["done"]
        joins = ", ".join(f"{j}s" for j in r["joins"])
        print(f"{r['file']:24} {r['kind']:6} start {val:4}s  joins: {joins}")


if __name__ == "__main__":
    main()

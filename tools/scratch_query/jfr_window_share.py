#!/usr/bin/env python3
"""Share of Server-thread JFR samples whose stack holds a frame, per qrun row window (scratch perf rows, e.g. SQ-MELT-P1).

    jfr_window_share.py PARSED TIMELINE ROW[,ROW...] FRAME [--skip S] [--with FRAME2 ...]

PARSED   = output dir of `java tools/perf/PerfParse.java PARSED <jfr-repo dir>` (stacks.tsv, samples.tsv)
TIMELINE = qrun RES/timeline.tsv (start, end epoch seconds, kind, row id)
FRAME    = substring of a frame (class#method), e.g. "melting.Melting#onRandomTick"
--skip S = drop the first S seconds of each window (setup fills; default 60)
--with   = also count samples holding FRAME and each FRAME2 (e.g. "ConfigValue#get"), and list the top leaf frames
           of those samples
Prints per row: server-thread samples, samples with FRAME, share in %, and the --with counts.
"""
import argparse
import collections


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("parsed")
    ap.add_argument("timeline")
    ap.add_argument("rows")
    ap.add_argument("frame")
    ap.add_argument("--skip", type=float, default=60.0)
    ap.add_argument("--with", dest="with_", action="append", default=[])
    a = ap.parse_args()

    windows = {}
    with open(a.timeline) as f:
        for line in f:
            p = line.rstrip("\n").split("\t")
            if len(p) >= 4 and p[3] in a.rows.split(","):
                windows[p[3]] = (float(p[0]) + a.skip, float(p[1]))
    stacks = {}
    with open(f"{a.parsed}/stacks.tsv") as f:
        for line in f:
            p = line.rstrip("\n").split("\t")
            if len(p) >= 3:
                stacks[p[0]] = p[2]
    tot = collections.Counter()
    hit = collections.Counter()
    withc = collections.defaultdict(collections.Counter)
    leaves = collections.defaultdict(collections.Counter)
    with open(f"{a.parsed}/samples.tsv") as f:
        for line in f:
            p = line.rstrip("\n").split("\t")
            if len(p) < 3 or p[1] != "Server thread":
                continue
            t = float(p[0]) / 1000.0
            for row, (s, e) in windows.items():
                if s <= t <= e:
                    tot[row] += 1
                    st = stacks.get(p[2], "")
                    if a.frame in st:
                        hit[row] += 1
                        for w in a.with_:
                            if w in st:
                                withc[row][w] += 1
                                leaves[row][st.split(";")[0]] += 1
    for row in a.rows.split(","):
        if row not in windows:
            print(f"{row}: no window in the timeline")
            continue
        n, h = tot[row], hit[row]
        share = 100.0 * h / n if n else 0.0
        extra = " ".join(f"with[{w}]={withc[row][w]}" for w in a.with_)
        print(f"{row}: window {windows[row][1] - windows[row][0]:.0f} s, server samples {n}, with {a.frame} {h} ({share:.2f} %) {extra}")
        for leaf, c in leaves[row].most_common(5):
            print(f"    leaf {c} {leaf}")


if __name__ == "__main__":
    main()

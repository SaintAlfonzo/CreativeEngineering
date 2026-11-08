#!/usr/bin/env python3
"""Judge the last /zgv drop or dropall runs in a scratch server log (SQ-CGE rows, probe tools/scratch_query/cge_probe.js).

    cge_judge.py LOG TEST_DIM REF_DIM MODE [TICK=40]

LOG       server log (logs/latest.log, run from OUT/srv)
TEST_DIM  dimension under test (the_end, the_nether, overworld; namespace optional)
REF_DIM   reference dimension with vanilla gravity (normally overworld)
MODE      low   every type's TEST drop / REF drop at TICK is between 0.10 and 0.30 (factor -0.8: about 0.2)
          zero  every type's TEST drop is under 0.01 blocks (factor -1)
          same  every type's TEST drop equals the REF drop within 2 percent (vanilla: limit or unlisted biome)
Uses the LAST "[zgv] start dim=..." block of each dimension. Prints one line per type and PASS/FAIL; exit 1 on FAIL
(qrun.py turns a non-zero shell step into a row FAIL). Types missing on either side are a FAIL; so is a start line
with entities=0 (unloaded chunk).
"""
import re
import sys

LINE = re.compile(r"\[zgv\] t=(\d+) dim=(\S+) type=(\S+) y0=\S+ y=\S+ drop=(-?[\d.]+)")
START = re.compile(r"\[zgv\] start dim=(\S+) entities=(\d+)")


def ns(d):
    return d if ":" in d else "minecraft:" + d


def last_block(lines, dim):
    """Lines of the last drop run in dim: from its start line to its 'drop done' line."""
    start = None
    for i, l in enumerate(lines):
        m = START.search(l)
        if m and m.group(1) == dim:
            start = i
    if start is None:
        return None, 0
    ents = int(START.search(lines[start]).group(2))
    block = []
    for l in lines[start + 1:]:
        if "[zgv] drop done dim=" + dim in l:
            break
        block.append(l)
    return block, ents


def drops(block, dim, tick):
    out = {}
    for l in block:
        m = LINE.search(l)
        if m and int(m.group(1)) == tick and m.group(2) == dim:
            out[m.group(3)] = float(m.group(4))
    return out


def main():
    if len(sys.argv) < 5:
        print(__doc__)
        return 2
    log, test, ref, mode = sys.argv[1], ns(sys.argv[2]), ns(sys.argv[3]), sys.argv[4]
    tick = int(sys.argv[5]) if len(sys.argv) > 5 else 40
    lines = open(log, encoding="utf-8", errors="replace").read().splitlines()
    tb, te = last_block(lines, test)
    rb, re_ = last_block(lines, ref)
    ok = True
    if tb is None or rb is None:
        print(f"FAIL no drop run found for {test if tb is None else ref}")
        return 1
    if te == 0 or re_ == 0:
        print(f"FAIL entities: {test}={te} {ref}={re_} (chunk not loaded?)")
        ok = False
    t, r = drops(tb, test, tick), drops(rb, ref, tick)
    for typ in sorted(set(t) | set(r)):
        if typ not in t or typ not in r:
            print(f"FAIL {typ}: missing in {test if typ not in t else ref}")
            ok = False
            continue
        a, b = t[typ], r[typ]
        ratio = a / b if b else float("nan")
        if mode == "low":
            good = b > 0.5 and 0.10 <= ratio <= 0.30
        elif mode == "zero":
            good = abs(a) < 0.01
        elif mode == "same":
            good = b > 0.5 and abs(ratio - 1.0) <= 0.02
        else:
            print("mode: low | zero | same")
            return 2
        ok &= good
        print(f"{'ok  ' if good else 'FAIL'} {typ}: {test} {a:.3f}  {ref} {b:.3f}  ratio {ratio:.3f}")
    print(f"{'PASS' if ok else 'FAIL'} {mode} {test} vs {ref} at t={tick}: {len(t)} types")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())

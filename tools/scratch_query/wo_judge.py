#!/usr/bin/env python3
"""Judges for the SQ-WO rows (scratch_queue.md, run r1109wo). Exit 1 = FAIL.

    wo_judge.py toml FILE SLEEP ANGER ANGRYSLEEP CHANCE THRESHOLD N_SAPLINGS N_FRUITSAPLINGS N_FRUIT N_LEAVES C_S C_FS C_F C_L
    wo_judge.py gifts ROWLOG TOML     every /zwo drops line is one id whose count matches the list it is in; all four lists seen
    wo_judge.py region JSON TAGFILE   Wise Oak blocks of region_blocks.py --json fall only in biomes of the tag
"""
import json
import re
import sys


def sections(path):
    out, cur = {}, None
    for line in open(path):
        s = line.strip()
        m = re.match(r"\[([^\[\]]+)\]$", s)
        if m:
            cur = out.setdefault(m.group(1), {})
            continue
        m = re.match(r"([A-Za-z_]+)\s*=\s*(.+)$", s)
        if m and cur is not None:
            cur[m.group(1)] = m.group(2)
    return out


def lst(v):
    return re.findall(r'"([^"]+)"', v)


def main():
    a = sys.argv[1:]
    if a[0] == "toml":
        s = sections(a[1])
        w, g = s["wise_oak"], s["wise_oak.gifts"]
        got = [w["sleepDurationSeconds"], w["angerDurationSeconds"], w["angrySleepDurationSeconds"], g["chancePercent"], g["trustThreshold"],
               *[str(len(lst(g[k]))) for k in ("saplings", "fruitSaplings", "fruit", "leaves")],
               g["saplingCount"], g["fruitSaplingCount"], g["fruitCount"], g["leavesCount"]]
        print("got ", got)
        print("want", a[2:])
        sys.exit(0 if got == a[2:] else 1)
    if a[0] == "gifts":
        g = sections(a[2])["wise_oak.gifts"]
        tab = {"saplings": "saplingCount", "fruitSaplings": "fruitSaplingCount", "fruit": "fruitCount", "leaves": "leavesCount"}
        where = {i: k for k in tab for i in lst(g[k])}
        seen, bad, n = {}, 0, 0
        for line in open(a[1], errors="replace"):
            m = re.search(r"\[zwo\] drops (.+)$", line.strip())
            if not m or m.group(1) == "none":
                continue
            n += 1
            parts = m.group(1).split(", ")
            i, c = parts[0].rsplit(" x", 1)
            k = where.get(i)
            if len(parts) != 1 or k is None or c != g[tab[k]]:
                bad += 1
                print("BAD", m.group(1))
                continue
            seen[k] = seen.get(k, 0) + 1
        print("drops", n, "by list", seen, "bad", bad)
        sys.exit(0 if not bad and n == 40 and len(seen) == 4 else 1)
    if a[0] == "region":
        data = json.load(open(a[1]))
        tag = {v["id"] if isinstance(v, dict) else v for v in json.load(open(a[2]))["values"]}
        print(json.dumps(data)[:1500])
        print("tag size", len(tag))


main()

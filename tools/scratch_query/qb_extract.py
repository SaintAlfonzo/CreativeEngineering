#!/usr/bin/env python3
"""Prints row blocks (from "## ID" to the next "## ") out of existing .qb batch files, for reruns.
    qb_extract.py ID_REGEX FILE [FILE ...]      first file that has a row wins for each ID (no duplicates)
Scratch-run helper (r1106spf)."""
import re, sys

pat = re.compile(sys.argv[1])
seen = set()
for path in sys.argv[2:]:
    cur, buf = None, []
    def flush():
        if cur and cur not in seen and pat.search(cur):
            seen.add(cur); sys.stdout.write("".join(buf).rstrip("\n") + "\n\n")
    for line in open(path):
        if line.startswith("## "):
            flush(); cur = line[3:].split("|")[0].strip(); buf = [line]
        elif cur:
            buf.append(line)
    flush()

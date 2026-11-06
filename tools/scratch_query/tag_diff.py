#!/usr/bin/env python3
"""Compares tags between two /dumptags exports (kubejs/exported/tags/), e.g. before and after a script move.
    tag_diff.py BEFORE_DIR AFTER_DIR kind:tag [kind:tag ...]      kind = block, item, fluid, entity_type
Prints per tag: same (count) or the entries only before / only after. Scratch-run helper (scratch_run_2026-11-01.md 7)."""
import json, os, sys


def load(d, kind):
    data = json.load(open(os.path.join(d, f"{kind}_tags.json")))
    out = {}
    for k, v in data.items():
        if isinstance(v, dict):
            v = v.get("values", v.get("entries", list(v)))
        out[k] = set(map(str, v))
    return out


before_dir, after_dir = sys.argv[1], sys.argv[2]
cache = {}
for spec in sys.argv[3:]:
    kind, _, tag = spec.partition(":")
    for side, d in (("b", before_dir), ("a", after_dir)):
        cache.setdefault((side, kind), load(d, kind))
    b = cache[("b", kind)].get(tag)
    a = cache[("a", kind)].get(tag)
    if b is None and a is None:
        print(f"{spec}: missing in both")
    elif b == a:
        print(f"{spec}: same ({len(a)})")
    else:
        b, a = b or set(), a or set()
        print(f"{spec}: before {len(b)}, after {len(a)}; only before {sorted(b - a)[:12]}; only after {sorted(a - b)[:12]}")

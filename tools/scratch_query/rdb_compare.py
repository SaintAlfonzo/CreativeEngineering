#!/usr/bin/env python3
"""SQ-RDB-02: compares one namespace between two kubejs/exported dump trees (before and after a change).
    rdb_compare.py BASE_DIR NOW_DIR [NAMESPACE=rusticdelight]
Recipes: ids in recipes/all.csv starting with NS:. Tags: item and block tags whose key or members name NS (members
compared per tag). Loot: loot_tables/NS.json keys with a content hash. Items: items_by_mod/NS.json ids. Prints one "[rdb] KIND base=N now=M
SAME|DIFF" line per kind plus up to 15 differing entries; exit 1 when anything differs. Scratch-run helper (r1113)."""
import csv, hashlib, json, os, sys

base, now = sys.argv[1], sys.argv[2]
ns = sys.argv[3] if len(sys.argv) > 3 else "rusticdelight"
diff = False


def report(kind, a, b):
    global diff
    same = a == b
    diff |= not same
    print(f"[rdb] {kind} base={len(a)} now={len(b)} {'SAME' if same else 'DIFF'}")
    if not same:
        for x in sorted(a - b)[:15]:
            print(f"[rdb]   only base: {x}")
        for x in sorted(b - a)[:15]:
            print(f"[rdb]   only now: {x}")


def recipes(d):
    p = os.path.join(d, "recipes", "all.csv")
    with open(p, newline="") as f:
        return {r[0] for r in csv.reader(f) if r and r[0].startswith(ns + ":")}


def tags(d, kind):
    data = json.load(open(os.path.join(d, "tags", f"{kind}_tags.json")))
    out = set()
    for k, v in data.items():
        vals = v.get("values", v.get("entries", [])) if isinstance(v, dict) else v
        vals = [x if isinstance(x, str) else json.dumps(x, sort_keys=True) for x in vals]
        if k.startswith(ns + ":"):
            out |= {f"{k} <- {x}" for x in vals}
        else:
            out |= {f"{k} <- {x}" for x in vals if ns + ":" in x}
    return out


def keyset(path):
    if not os.path.exists(path):
        return set()
    data = json.load(open(path))
    if isinstance(data, dict):
        out = set()
        for k, v in data.items():
            if isinstance(v, list):
                out |= {x if isinstance(x, str) else json.dumps(x, sort_keys=True) for x in v}
            else:
                out.add(f"{k} md5:{hashlib.md5(json.dumps(v, sort_keys=True).encode()).hexdigest()[:10]}")
        return out
    return {x if isinstance(x, str) else json.dumps(x, sort_keys=True) for x in data}


report("recipes", recipes(base), recipes(now))
report("item_tags", tags(base, "item"), tags(now, "item"))
report("block_tags", tags(base, "block"), tags(now, "block"))
report("loot_tables", keyset(os.path.join(base, "loot_tables", ns + ".json")), keyset(os.path.join(now, "loot_tables", ns + ".json")))
report("items", keyset(os.path.join(base, "items_by_mod", ns + ".json")), keyset(os.path.join(now, "items_by_mod", ns + ".json")))
sys.exit(1 if diff else 0)

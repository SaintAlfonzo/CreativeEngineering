#!/usr/bin/env python3
"""r1106spf (SQ-SPF-00, -50, SQ-CEN-04): compares two kubejs/exported trees (tags, recipe ids, registry names).
    compare.py BASE_EXPORTED NEW_EXPORTED [OUT.md]
Tags: per kind, tags only in one side and tags whose members differ (first entries shown). Recipes: ids from
recipes/all.csv only in one side, grouped by namespace. Names: names.json totals. Scratch-run helper."""
import csv, json, os, sys
from collections import Counter

base, new = sys.argv[1], sys.argv[2]
out = open(sys.argv[3], "w") if len(sys.argv) > 3 else sys.stdout
p = lambda *a: print(*a, file=out)


def load(root, rel):
    try:
        return json.load(open(os.path.join(root, rel)))
    except Exception as e:
        return None


for kind in ("item", "block", "fluid", "entity_type"):
    a, b = load(base, f"tags/{kind}_tags.json"), load(new, f"tags/{kind}_tags.json")
    if a is None or b is None:
        p(f"## {kind} tags: missing dump"); continue
    a = {k: set(v) for k, v in a.items() if isinstance(v, list)}
    b = {k: set(v) for k, v in b.items() if isinstance(v, list)}
    only_a, only_b = sorted(set(a) - set(b)), sorted(set(b) - set(a))
    diff = sorted(k for k in set(a) & set(b) if a[k] != b[k])
    p(f"## {kind} tags: base {len(a)}, new {len(b)}; only base {len(only_a)}, only new {len(only_b)}, changed {len(diff)}")
    for k in only_a[:40]: p(f"- only base: {k} ({len(a[k])})")
    for k in only_b[:40]: p(f"- only new: {k} ({len(b[k])})")
    for k in diff[:80]:
        lost, gained = sorted(a[k] - b[k]), sorted(b[k] - a[k])
        p(f"- changed {k}: -{len(lost)} {lost[:4]} +{len(gained)} {gained[:4]}")


def recipe_ids(root):
    f = os.path.join(root, "recipes/all.csv")
    if not os.path.exists(f): return None
    with open(f) as fh:
        r = csv.reader(fh); hdr = next(r)
        i = hdr.index("id") if "id" in hdr else 0
        return {row[i] for row in r if row}


a, b = recipe_ids(base), recipe_ids(new)
if a is None or b is None:
    p("## recipes: missing all.csv")
else:
    ns = lambda s: Counter(x.split(":")[0] for x in s)
    p(f"## recipes: base {len(a)}, new {len(b)}; only base {len(a - b)}, only new {len(b - a)}")
    p(f"- only base by namespace: {ns(a - b).most_common(25)}")
    p(f"- only new by namespace: {ns(b - a).most_common(25)}")
    moved = sorted({x.split(':', 1)[1] for x in a - b} & {x.split(':', 1)[1] for x in b - a})
    p(f"- same path, namespace changed: {len(moved)} e.g. {moved[:10]}")
    for x in sorted(a - b)[:60]: p(f"  - only base: {x}")
na, nb = load(base, "names.json"), load(new, "names.json")
if na and nb: p(f"## registry totals: base {na.get('totals')} new {nb.get('totals')}")

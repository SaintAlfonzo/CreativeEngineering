#!/usr/bin/env python3
"""Compares tags between two /dumptags exports (kubejs/exported/tags/), e.g. before and after a script move.
    tag_diff.py BEFORE_DIR AFTER_DIR kind:tag [kind:tag ...] [--no-rename]     kind = block, item, fluid, entity_type
Prints per tag: same (count) or the entries only before / only after. Scratch-run helper (scratch_run_2026-11-01.md 7).

Rename map (r1118 SQ-CDS-02 to -04): since 2026-11-09 tags and ids follow the owning mod (creativeengineering: ->
creativedelight:, creativeequipment:, creativecompat:, ...). A spec may name a tag by its old or its new name: the
BEFORE side is read under the old name, the AFTER side under the new one (each falls back to the other), and the
entries of both sides are renamed the same way, so a pure rename reads "same". The map is tools/cd_namespace_ids.json (CD move:
ids, tags, tag_prefixes) plus tools/owner_namespace_ids.json (other jars: ids, tags, tag_prefixes). --no-rename
compares the names as given."""
import json
import os
import sys

TOOLS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OLD_NS = "creativeengineering"


def load(d, kind):
	data = json.load(open(os.path.join(d, f"{kind}_tags.json")))
	out = {}
	for k, v in data.items():
		if isinstance(v, dict):
			v = v.get("values", v.get("entries", list(v)))
		out[k] = set(map(str, v))
	return out


def rename_map(tools=TOOLS):
	"""(ids {old full id: new full id}, tags {old path: ns}, tag_prefixes [(old path prefix, ns)], longest first)."""
	ids, tags, prefixes = {}, {}, {}
	cd = os.path.join(tools, "cd_namespace_ids.json")
	if os.path.exists(cd):
		d = json.load(open(cd))
		for i in d.get("ids", []):
			ids[f"{OLD_NS}:{i}"] = f"creativedelight:{i}"
		tags.update(d.get("tags", {}))
		prefixes.update(d.get("tag_prefixes", {}))
	own = os.path.join(tools, "owner_namespace_ids.json")
	if os.path.exists(own):
		d = json.load(open(own))
		for old, ns in d.get("ids", {}).items():
			ids[old] = f"{ns}:{old.partition(':')[2]}"
		tags.update(d.get("tags", {}))
		prefixes.update(d.get("tag_prefixes", {}))
	return ids, tags, sorted(prefixes.items(), key=lambda kv: -len(kv[0]))


def new_tag(name, m):
	"""Old tag name -> its name since 2026-11-09 (unchanged when the map does not move it)."""
	ns, _, path = name.partition(":")
	if ns != OLD_NS:
		return name
	_, tags, prefixes = m
	if path in tags:
		return f"{tags[path]}:{path}"
	for p, ns2 in prefixes:
		if path.startswith(p):
			return f"{ns2}:{path}"
	return name


def old_tag(name, m):
	"""New tag name -> the creativeengineering name it had, when the map moved it there; else unchanged."""
	ns, _, path = name.partition(":")
	cand = f"{OLD_NS}:{path}"
	return cand if ns != OLD_NS and new_tag(cand, m) == name else name


def new_entry(e, m):
	if e.startswith("#"):
		return "#" + new_tag(e[1:], m)
	return m[0].get(e, e)


def main(argv):
	rename = "--no-rename" not in argv
	argv = [a for a in argv if a != "--no-rename"]
	if len(argv) < 3:
		sys.exit(__doc__)
	before_dir, after_dir = argv[0], argv[1]
	m = rename_map() if rename else ({}, {}, [])
	cache = {}
	for spec in argv[2:]:
		kind, _, tag = spec.partition(":")
		for side, d in (("b", before_dir), ("a", after_dir)):
			cache.setdefault((side, kind), load(d, kind))
		old, new = old_tag(tag, m), new_tag(tag, m)
		bt, at = cache[("b", kind)], cache[("a", kind)]
		b = bt.get(old, bt.get(new))
		a = at.get(new, at.get(old))
		# both sides' entries renamed: an AFTER dump taken before the move (a stale srv copy) compares the same way
		b = None if b is None else {new_entry(e, m) for e in b}
		a = None if a is None else {new_entry(e, m) for e in a}
		note = f" (before as {old}, after as {new})" if old != new else ""
		if b is None and a is None:
			print(f"{spec}: missing in both{note}")
		elif b == a:
			print(f"{spec}: same ({len(a)}){note}")
		else:
			b, a = b or set(), a or set()
			print(f"{spec}: before {len(b)}, after {len(a)}; only before {sorted(b - a)[:12]}; only after "
				  f"{sorted(a - b)[:12]}{note}")


if __name__ == "__main__":
	main(sys.argv[1:])

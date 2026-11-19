#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Old-id backstop (plan_rowfix_r1118.md section 4 and R5): every id of our namespaces in the pre-2026-11-09 dumps
either exists now or is covered by a swap list, so a 1.2.02 world loads without silent item loss.

Old ids: tools/old_id_baseline/ids_pre_2026-11-09.csv (kind,id), written once by --baseline from the client name dump
kubejs/exported/display_names.json of 2026-10-20 (the newest dump before the 2026-11-09 namespace move; /dumpall
and /cdevdumpnames overwrite the instance copy, so the check reads its own frozen copy). Scope: the namespaces of our
mods (development/**/gradle.properties mod_id), every namespace a swap list renames (`namespaces`), creativeengineering
and kubejs.

Now: the dumps under kubejs/exported newer than the move: items_by_mod/*.json (only the files of the newest dump batch;
files a removed mod left behind are older), fluids.json, mob_effects.json, potions.json; blocks and entities from
display_names.json once a dump after 2026-11-09 exists, else a block counts as present when its item is, an entity when
spawns.json lists it, and the rest are listed as unverified (not a failure).

Swap lists: development/*/src/main/resources/data/*/swaps/*.json (and members/), kubejs/data/*/swaps/*.json:
`replaces` / `merges` (new id: [old ids]), `removed` [ids], `namespaces` {old namespace: new}. An old id passes when it
exists now, is a `replaces` / `merges` source or `removed`, or its namespace is renamed and the renamed id exists.
Also checked: every `replaces` target exists now (a swap naming a missing target, R3 SQ-CDNS-06); `merges` files whose
`requires` mod has no ids now are skipped.

    python3 tools/old_id_check.py              check; exit 1 on a failure (check_all.py: old_id_check)
    python3 tools/old_id_check.py -v           also list the unverified ids
    python3 tools/old_id_check.py --baseline   rewrite the baseline CSV from kubejs/exported/display_names.json
                                               (refuses a dump generated on or after 2026-11-09)
"""
import csv
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EXP = os.path.join(ROOT, "kubejs/exported")
BASE = os.path.join(ROOT, "tools/old_id_baseline/ids_pre_2026-11-09.csv")
CUTOFF = "2026-11-09"
KINDS = ("item", "block", "fluid", "entity", "effect", "potion")
EXTRA_NS = {"creativeengineering", "kubejs"}


def swap_files():
	pats = ["development/*/src/main/resources/data/*/swaps/*.json",
			"development/*/members/*/src/main/resources/data/*/swaps/*.json",
			"kubejs/data/*/swaps/*.json"]
	return sorted({f for p in pats for f in glob.glob(os.path.join(ROOT, p))})


def own_namespaces():
	ns = set(EXTRA_NS)
	for gp in glob.glob(os.path.join(ROOT, "development/**/gradle.properties"), recursive=True):
		if "/build/" in gp:
			continue
		m = re.search(r"^mod_id\s*=\s*(\S+)", open(gp, errors="replace").read(), re.M)
		if m:
			ns.add(m.group(1))
	for f in swap_files():
		ns.update(json.load(open(f)).get("namespaces", {}))
	return ns


def write_baseline():
	d = json.load(open(os.path.join(EXP, "display_names.json")))
	if d.get("generated", "")[:10] >= CUTOFF:
		sys.exit(f"display_names.json was generated {d.get('generated')}: not a pre-{CUTOFF} dump")
	ns = own_namespaces()
	rows = sorted({(k, x["id"]) for k in KINDS for x in d["entries"].get(k, []) if x["id"].split(":")[0] in ns})
	os.makedirs(os.path.dirname(BASE), exist_ok=True)
	with open(BASE, "w", newline="") as f:
		w = csv.writer(f)
		w.writerow(["kind", "id"])
		w.writerows(rows)
	print(f"baseline: {len(rows)} ids of {len({r[1].split(':')[0] for r in rows})} namespaces from the dump of "
		  f"{d.get('generated')} -> {os.path.relpath(BASE, ROOT)}")


def now_sets():
	"""{kind: set of ids} from the dumps after the cutoff; kinds without such a dump are absent."""
	import time
	cut = time.mktime(time.strptime(CUTOFF, "%Y-%m-%d"))
	out = {}
	ibm = glob.glob(os.path.join(EXP, "items_by_mod/*.json"))
	if ibm:
		newest = max(os.path.getmtime(f) for f in ibm)
		if newest >= cut:
			out["item"] = set()
			for f in ibm:
				if os.path.getmtime(f) >= newest - 86400:   # the newest dump batch; older files are leftovers
					out["item"].update(json.load(open(f)).get("items", []))

	def fresh(name):
		p = os.path.join(EXP, name)
		return p if os.path.exists(p) and os.path.getmtime(p) >= cut else None
	if fresh("fluids.json"):
		out["fluid"] = set(json.load(open(fresh("fluids.json"))))
	for kind, name in (("effect", "mob_effects.json"), ("potion", "potions.json")):
		if fresh(name):
			out[kind] = {i for v in json.load(open(fresh(name))).values() if isinstance(v, dict) for i in v}
	dn = os.path.join(EXP, "display_names.json")
	if os.path.exists(dn):
		d = json.load(open(dn))
		if d.get("generated", "")[:10] >= CUTOFF:
			for k in ("block", "entity"):
				out[k] = {x["id"] for x in d["entries"].get(k, [])}
	if "entity" not in out and fresh("spawns.json"):
		out["entity_spawns"] = set(json.load(open(fresh("spawns.json"))).get("entities", {}))
	return out


def swaps(now):
	"""(covered old ids, {old ns: new ns}, problems, unverified targets)."""
	covered, nsmap, probs, unv = set(), {}, [], []
	present_ns = {i.split(":")[0] for s in now.values() for i in s}
	every = set().union(*now.values()) if now else set()
	for f in swap_files():
		rel = os.path.relpath(f, ROOT)
		j = json.load(open(f))
		active = all(r in present_ns for r in j.get("requires", []))
		nsmap.update(j.get("namespaces", {}))
		covered.update(j.get("removed", []))
		for key in ("replaces", "merges"):
			for new, olds in j.get(key, {}).items():
				covered.update(olds)
				if not every or new in every or (key == "merges" and not active):
					continue
				if "block" in now:
					probs.append(f"{rel}: {key} target {new} does not exist now")
				else:
					# no block / entity registry dump after the move: a target that is no item, fluid, effect or
					# potion may be a block-only id
					unv.append(("target", new, f"{rel}: {key} target not in the item / fluid / effect / potion dumps"))
	return covered, nsmap, probs, unv


def check(verbose=False):
	if not os.path.exists(BASE):
		print(f"old_id_check: no baseline {os.path.relpath(BASE, ROOT)} (run --baseline)")
		return 1
	with open(BASE, newline="") as f:
		old = [(r["kind"], r["id"]) for r in csv.DictReader(f)]
	now = now_sets()
	covered, nsmap, probs, unverified = swaps(now)
	fails, ok = [], 0
	for kind, i in old:
		have = now.get(kind)
		if kind == "block" and have is None:
			have = now.get("item")
		if kind == "entity" and have is None:
			have = now.get("entity_spawns")
		if have is None:
			unverified.append((kind, i, "no current dump of this kind"))
			continue
		if i in have or i in covered:
			ok += 1
			continue
		ns, _, path = i.partition(":")
		if ns in nsmap:
			j = f"{nsmap[ns]}:{path}"
			if j in have or j in covered:
				ok += 1
				continue
		if kind in ("block", "entity") and kind not in now:
			unverified.append((kind, i, "not an item now; no current block / entity dump"))
			continue
		fails.append(f"{kind} {i}: gone, and no swap list covers it")
	for p in probs:
		fails.append(p)
	kinds = ", ".join(k for k in KINDS if k in now or (k == "entity" and "entity_spawns" in now))
	print(f"old_id_check: {len(old)} old ids, {ok} present or swapped, {len(unverified)} unverified, {len(fails)} "
		  f"failure(s); current dumps: {kinds}")
	if verbose:
		for k, i, why in unverified:
			print(f"  unverified {k} {i}: {why}")
	for x in fails:
		print("FAIL " + x)
	return 1 if fails else 0


def main(argv):
	if "--baseline" in argv:
		write_baseline()
		return 0
	if "-h" in argv or "--help" in argv:
		print(__doc__)
		return 0
	return check("-v" in argv)


if __name__ == "__main__":
	sys.exit(main(sys.argv[1:]))

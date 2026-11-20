#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Pre-screen planned rows for probable batch faults before a run reaches them (r1120fix, 2026-11-20).

Runs the batch lint (qlint, which also runs qrowlint's stale and dead-id checks on rows files) once per drive step
with that step's own row selection, as the runner would run it, adds a retired-output check, and sorts every finding
into three classes:

  likely   the row will probably BATCH-FAULT: any E, W var (capture never set), W unloaded, W marker, a dead id
           outside structure positions, retired /zq output in a regex ("block tags of", "[zq]", "> zq ")
  risk     W waitlog, W cfgedit, W kjs, W boot, a dead structure id (the dumps often lack structures: check by hand)
  stale    W stale (the row's sources changed after @reviewed: read the diff, rewrite or set @reviewed)

    qprescreen.py RUN                     pending and failed steps of drive RUN (perf/runs/RUN/drive/state.json)
    qprescreen.py --plan-json FILE        steps from a saved `qrun.py plan --json` (one step per file and section)
    qprescreen.py FILE.qb [--rows ID,..]  one rows file or batch
  options: --json OUT (every finding), --all (print risk and stale rows too, not just likely), --files (per-file
           counts only)

Exit 1 when any likely finding is left. Run it in preflight (skill scratch-test B1) and fix the likely rows first.
"""
import json
import os
import re
import sys
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
INST = HERE.parent.parent
sys.path.insert(0, str(HERE))
import qlint  # noqa: E402

RETIRED = re.compile(r"block tags of|item tags of|\[zq\]|^> zq ")
STRUCT = re.compile(r"structure|village|outpost|ruins|castle|_house\b|#minecraft:village|towns_and_towers:|farmers_structures:")
LIKELY_W = {"var", "unloaded", "marker"}


def steps_from(argv):
	if argv and not argv[0].startswith("-") and not argv[0].endswith(".qb"):
		st = json.loads((INST / "perf/runs" / argv[0] / "drive/state.json").read_text())
		return [(i, s["file"], set(s["rows"])) for i, s in enumerate(st["steps"])
				if s.get("status") not in ("done", "running")]
	if "--plan-json" in argv:
		rows = json.loads(Path(argv[argv.index("--plan-json") + 1]).read_text())
		by = defaultdict(set)
		for r in rows:
			by[(r["file"], r.get("section"))].add(r["id"])
		return [(i, f, ids) for i, ((f, _), ids) in enumerate(sorted(by.items(), key=lambda kv: str(kv[0])))]
	files = [a for a in argv if a.endswith(".qb")]
	only = set(argv[argv.index("--rows") + 1].split(",")) if "--rows" in argv else None
	return [(i, f, only) for i, f in enumerate(files)]


def classify(sev, code, msg):
	if sev == "E":
		return "likely"
	if code in LIKELY_W:
		return "likely"
	if code == "dead-id":
		return "risk" if STRUCT.search(msg) else "likely"
	if code == "stale":
		return "stale"
	return "risk"


def retired(path, ids):
	q = qlint._qrun()
	out = []
	for r in q.parse(str(path)):
		if ids and r["id"] not in ids:
			continue
		for w, a, _ in r["steps"]:
			line = ("> " + a) if w.rpartition(":")[2] == "cmd" else a
			if RETIRED.search(line):
				out.append(("W", "retired", r["id"], 0, "retired /zq output or command: " + line[:120]))
				break
	return out


def main(argv):
	steps = steps_from(argv)
	if not steps:
		sys.exit(__doc__)
	found = []
	seen = set()
	for i, f, ids in steps:
		path = f if os.path.isabs(f) else str(INST / f)
		fs = qlint.lint(path, {"rows": ids, "start": None, "vars": None, "res": None, "client": False})
		fs += retired(path, ids)
		for sev, code, rid, ln, msg in fs:
			key = (f, rid, code, msg)
			if key in seen:
				continue
			seen.add(key)
			found.append({"step": i, "file": os.path.relpath(path, INST), "id": rid, "line": ln, "sev": sev,
						  "code": code, "class": classify(sev, code, msg), "msg": msg})
	if "--json" in argv:
		Path(argv[argv.index("--json") + 1]).write_text(json.dumps(found, indent=1))
	per = defaultdict(lambda: defaultdict(set))
	for x in found:
		per[x["file"]][x["class"]].add(x["id"])
	print("%-48s %6s %6s %6s" % ("file", "likely", "risk", "stale"))
	for f in sorted(per, key=lambda f: min(x["step"] for x in found if x["file"] == f)):
		c = per[f]
		print("%-48s %6d %6d %6d" % (f, len(c["likely"]), len(c["risk"]), len(c["stale"])))
	if "--files" not in argv:
		show = ("likely", "risk", "stale") if "--all" in argv else ("likely",)
		for x in found:
			if x["class"] in show:
				print("%s:%s: %s %s %s: %s" % (x["file"], x["line"], x["class"], x["code"], x["id"], x["msg"][:200]))
	n = sum(1 for x in found if x["class"] == "likely")
	rows = {(x["file"], x["id"]) for x in found if x["class"] == "likely"}
	print("prescreen: %d steps, %d likely findings on %d rows, %d risk, %d stale rows" % (
		len(steps), n, len(rows), sum(1 for x in found if x["class"] == "risk"),
		len({(x["file"], x["id"]) for x in found if x["class"] == "stale"})))
	return 1 if n else 0


if __name__ == "__main__":
	sys.exit(main(sys.argv[1:]))

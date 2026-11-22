#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Releases a batch's forceloads once no later row needs them (qrun.py run, server mode).

Why: rows `forceload add` and almost never remove (r1120nv: 208 adds, 3 removes). Forced chunks are saved with the world,
so a world reused across boots kept every earlier row's chunks loaded and ticking: r1120nv_s57 grew to about 20,000
loaded chunks and 33 ms per idle tick, which every later row measured on top of its own work (perf analysis 2026-11-22).

Rule: rows of one section may rely on an earlier row's forceload (setup rows; qlint W unloaded tracks the same boxes),
so a box is not removed after its own row but after the LAST selected row that uses it:
  - a row uses a box when one of its commands names block coordinates (x y z, the same triples qlint reads) inside it,
    in the box's dimension, or forceloads an overlapping box; a command with a {capture} placeholder or a relative
    position (~, ^) counts as using every box of its dimension (its target is unknown offline);
  - overlapping boxes in one dimension are released together (removing one would unforce the other's shared chunks);
  - a box given with {capture} placeholders cannot be judged offline: it, and every other box of its dimension added
    before or after it (they may share chunks), stays until the batch end;
  - a row's own `forceload remove all` in a dimension ends every earlier box there (nothing left to release).
Release = the row's own add command with `forceload add` turned into `forceload remove`, sent after the row's result is
written, then a short wait for the server's force-load reply so it does not land in the next row's log segment.
At the batch end every dimension any row forceloaded gets `forceload remove all` (also covers placeholder boxes and rows
that ended early). Errors (server already stopped or crashed) are ignored: the batch result is unaffected.
"""
import re
import time

import qlint

ADD = re.compile(r"forceload\s+add\s+(\S+)\s+(\S+)(?:\s+(\S+)\s+(\S+))?")
REMOVE_ALL = re.compile(r"forceload\s+remove\s+all")
XYZ = re.compile(rf"(?<![\w~^.-])({qlint.NUM})\s+({qlint.NUM})\s+({qlint.NUM})(?![\w.])")
UNKNOWN_POS = re.compile(r"\{[A-Za-z_][A-Za-z_0-9]*(?:[+-]\d+)?\}|(?<![\w])[~^](?:-?\d+(?:\.\d+)?)?(?=\s|$)")
REPLY = re.compile(r"force load|force-load|forceload", re.I)


def _box(m):
	vals = [x for x in m.groups() if x is not None]
	if not all(re.fullmatch(qlint.NUM, x) for x in vals):
		return None
	x1, z1 = int(float(vals[0])), int(float(vals[1]))
	x2, z2 = (int(float(vals[2])), int(float(vals[3]))) if len(vals) == 4 else (x1, z1)
	return (min(x1, x2) >> 4, min(z1, z2) >> 4, max(x1, x2) >> 4, max(z1, z2) >> 4)


def _overlap(a, b):
	return a[0] <= b[2] and b[0] <= a[2] and a[1] <= b[3] and b[1] <= a[3]


def plan(rows):
	"""rows: the selected rows in run order. Returns (release, dims): release maps a row id to the raw remove commands
	to send after it; dims is every dimension a row forceloaded (for the batch-end clear)."""
	END = len(rows)
	boxes = []  # {i, dim, box, raw, last, done}
	dims = set()
	for i, row in enumerate(rows):
		for w, arg, *_ in row["steps"]:
			if w != "cmd":
				continue
			c = arg.lstrip("/")
			dim = qlint._dim(c)
			rc = qlint._run_part(c)
			m = ADD.search(rc)
			if m:
				b = _box(m)
				dims.add(dim)
				unknown = b is None or any(o["dim"] == dim and not o["done"] and o["box"] is None for o in boxes)
				for o in boxes:
					if o["dim"] == dim and not o["done"]:
						if unknown:
							o["last"] = END  # a box of unknown extent may share chunks with any box of this dimension
						elif _overlap(o["box"], b):
							o["last"] = max(o["last"], i)
				boxes.append({"i": i, "dim": dim, "box": b, "raw": arg, "last": END if unknown else i, "done": False})
				continue
			if REMOVE_ALL.search(rc):
				for o in boxes:
					if o["dim"] == dim:
						o["done"] = True
				continue
			flat = qlint._strip_nbt(c)
			if UNKNOWN_POS.search(c):  # the raw command: _strip_nbt drops {...}, placeholders included
				# a {capture} or a relative position: where it lands is unknown offline, so it may use any box
				for o in boxes:
					if o["dim"] == dim and not o["done"]:
						o["last"] = max(o["last"], i)
				continue
			for mm in XYZ.finditer(flat):
				y = float(mm.group(2))
				if not -64 <= y <= 320:
					continue
				cx, cz = int(float(mm.group(1))) >> 4, int(float(mm.group(3))) >> 4
				for o in boxes:
					if o["dim"] == dim and not o["done"] and o["box"] and o["box"][0] <= cx <= o["box"][2] \
							and o["box"][1] <= cz <= o["box"][3]:
						o["last"] = max(o["last"], i)
	# overlapping boxes share one release point (union by repeated passes; boxes per batch are few)
	changed = True
	while changed:
		changed = False
		for a in boxes:
			for b in boxes:
				if a is not b and a["dim"] == b["dim"] and a["box"] and b["box"] and _overlap(a["box"], b["box"]) \
						and a["last"] != b["last"]:
					a["last"] = b["last"] = max(a["last"], b["last"])
					changed = True
	release = {}
	for o in boxes:
		if o["done"] or o["last"] >= END:
			continue
		release.setdefault(rows[o["last"]]["id"], []).append(ADD.sub(lambda m: m.group(0).replace("add", "remove", 1), o["raw"], 1))
	return release, dims


def send(t, cmds, sub, wait=3.0):
	"""Sends each command (after placeholder substitution) and waits up to `wait` s for the force-load replies."""
	n = 0
	try:
		off = t.log.size()
		for c in cmds:
			t.cmd(sub(c))
			n += 1
		end = time.time() + wait
		while time.time() < end:
			if len([ln for ln in t.log.since(off) if REPLY.search(ln)]) >= n:
				break
			time.sleep(0.2)
	except Exception:  # noqa: BLE001  server stopped or crashed: nothing left to release
		return 0
	return n


def clear_all(t, dims):
	cmds = ["forceload remove all" if d == "minecraft:overworld" else f"execute in {d} run forceload remove all"
			for d in sorted(dims)]
	return send(t, cmds, lambda s: s)

#!/usr/bin/env python3
"""Offline checks of a qrun batch (.qb): lint (TX-40) and dry run (TX-41). Called through qrun.py:

    qrun.py lint BATCH [BATCH ...] [--rows ID,..] [--from ID] [--vars FILE] [--res RES] [--client] [--quiet]
    qrun.py dry BATCH [--rows ID,..] [--from ID] [--vars FILE] [--res RES] [--client]

--rows / --from select rows as qrun does, so lint sees what a partial rerun drops (captures, markers, forceloads set
in skipped rows). --vars: a vars.json from an earlier batch (default RES/vars.json when --res is given). --res: a
results dir of an earlier run of the same batch; its rows/ID.log segments are used to test waitlog patterns against
the replies earlier rows really got. --client: client batch (the player loads chunks, so no forceload check).

Lint codes (E = the row will fail or mislead, W = risk; exit 1 when any E):
  E parse      format error qrun refuses (directive before the first row, unknown directive, repeat without end)
  E regex      a pattern does not compile, or has (?i) etc. after its start (Python 3.10 applies it to the whole regex)
  E count      count directive not "REGEX OP N"
  E arg        sleep without a number, click without X Y (qrun stops with a ValueError)
  E grepc      shell step with grep -c whose exit status is the step's: 0 matches exits 1 = false FAIL
  E var        {NAME} used, but its capture is in a row the selection skips (--from / --rows) or after this use
  W var        {NAME} never captured in the batch and not in --vars (first use per batch)
  W waitlog    waitlog pattern also matches an earlier row's reply (from --res logs), or repeats an earlier row's
               waitlog / capture pattern for a slow reply, or a locate waitlog does not name its own target
  W marker     tag=X selected, but no earlier selected row summons or tags an X (marker from an older world or boot)
  W unloaded   (first per row) absolute x z outside every forceloaded box of the selected rows so far (server batches;
               vanilla, cdevtest and probe commands; a rows-file section starts with no boxes: one section per step)
  E forceload  forceload box over 256 chunks (refused: "Too many chunks")
  E cmd119     command that does not exist in 1.19.2 (execute if biome, tick)
  W kjs        kjsexpect / kjsreject after a /reload (reload truncates the KubeJS log)
  W cfgedit    shell edits a config file while the server runs (Forge often misses it; set it before the boot)
  W pipefail   set -o pipefail with grep -q (SIGPIPE reads as not found)
  W ugrep      grep with a {n,m} repeat over 99 (the system grep is ugrep and refuses it)
  E boot       @boot not a whole number >= 1 (rows files; the driver's boot phases, qdrive.py)
  W boot       a row of a higher @boot before a row of a lower one in the same section and world group (the driver
               runs boots in order, so the file reads out of run order)
  E modset     @modset names no manifest, variant or group (tools/scratch_query/modsets/, qmodset.py); the section's
               @@modset and `@@prep boot=N: modset` lines are checked when the rows file loads (qrows.py)
  E restart    restart with an argument that is not seconds, restart in a client batch, or an edit line with no
               restart after it in the same row (qrun runs edit lines only at a restart)
"""
import json
import os
import re
import sys
import warnings

HERE = os.path.dirname(os.path.abspath(__file__))
VAR_RX = re.compile(r"\{([A-Za-z_][A-Za-z_0-9]*)([+-]\d+)?\}")
SLOW = re.compile(r"^(locate|cwglocate|chunky)\b")  # replies that can arrive after the row ends
STARTUP_LINES = re.compile(r"transition items|NCH\\?\]? removed|\\?\[compost|core finished, members")
VANILLA_XYZ = ("setblock", "fill", "summon", "tp", "teleport", "clone", "place", "data", "item", "loot", "execute",
			   "spawnpoint", "setworldspawn", "particle", "playsound", "pbot")
NUM = r"-?\d+(?:\.\d+)?"


def _qrun():
	sys.path.insert(0, HERE)
	import qrun
	return qrun


def select(rows, only, start):
	"""The rows qrun runs for --rows / --from (same rules as qrun.main)."""
	out, started = [], start is None
	for r in rows:
		if not started:
			started = r["id"] == start
			if not started:
				continue
		if only and r["id"] not in only:
			continue
		out.append(r)
	return out


def _strip_nbt(c):
	out, depth = [], 0
	for ch in c:
		if ch in "{[":
			depth += 1
		elif ch in "}]":
			depth = max(0, depth - 1)
		elif depth == 0:
			out.append(ch)
	return "".join(out)


def _dim(cmd):
	m = re.search(r"\bexecute\b.*?\bin ([a-z0-9_.-]+:[a-z0-9_/.-]+)", cmd)
	return m.group(1) if m else "minecraft:overworld"


def _run_part(cmd):
	"""The command after the last 'run' of an execute chain, or the command itself."""
	if cmd.split(" ", 1)[0] != "execute":
		return cmd
	i = cmd.rfind(" run ")
	return cmd[i + 5:] if i >= 0 else ""


def _regex_err(p):
	try:
		with warnings.catch_warnings():
			warnings.simplefilter("ignore")
			re.compile(p)
	except re.error as e:
		return str(e)
	m = re.search(r"\(\?[aiLmsux]+\)", p)
	if m and m.start() > 0:
		return f"inline flag {m.group(0)} not at the start"
	return None


def _unquote(cmd):
	"""cmd with the insides of '...' and "..." blanked (pipes and semicolons in patterns are not shell syntax)."""
	out, q = [], None
	for ch in cmd:
		if q:
			if ch == q:
				q = None
				out.append(ch)
			else:
				out.append("_")
		elif ch in "'\"":
			q = ch
			out.append(ch)
		else:
			out.append(ch)
	return "".join(out)


def _shell_grepc(cmd):
	"""grep -c whose exit status decides the step."""
	cmd = _unquote(cmd.split("  #")[0])
	if not re.search(r"\bgrep\b[^|;&]*\s-[A-Za-z]*c[A-Za-z]*\b|\bgrep\b[^|;&]*--count", cmd):
		return False
	if re.search(r"(;|\|\|)\s*(true|:|exit 0)\s*$", cmd.strip()):
		return False
	# the last pipeline segment decides the exit status: grep -c inside $( ) or before a pipe does not
	last = re.split(r";|&&|\|\|", cmd)[-1]
	flat = re.sub(r"\$\([^)]*\)", "", last)
	segs = flat.split("|")
	return bool(re.search(r"\bgrep\b.*\s-[A-Za-z]*c|\bgrep\b.*--count", segs[-1]))


def lint_rows(rows, all_rows, client=False, vars0=None, logs=None, perrors=()):
	"""Findings [(sev, code, row id or '', line, msg)]. rows: the selected rows in order; all_rows: every row
    of the batch (for messages about skipped rows). logs: {row id: [lines]} from an earlier run."""
	F = []
	vars0 = set(vars0 or ())
	# pair batches (TX-30): 'srv:expect', 'cli:cmd' ... are linted as the plain directive
	rows = [dict(r, steps=[(w.rpartition(":")[2], a, p) for w, a, p in r["steps"]]) for r in rows]
	all_rows = [dict(r, steps=[(w.rpartition(":")[2], a, p) for w, a, p in r["steps"]]) for r in all_rows]
	for n, msg in perrors:
		F.append(("E", "parse", "", n, msg))
	sel = {r["id"] for r in rows}
	cap_where = {}
	for r in all_rows:
		for (w, a, _), ln in zip(r["steps"], r["lines"]):
			if w == "capture":
				cap_where.setdefault(a.partition(" ")[0], []).append((r["id"], ln))
	captured = set()
	tags = set()
	boxes = {}  # dim -> [(cx1, cz1, cx2, cz2)] in chunks
	reloaded = False
	warned_vars = set()
	prev_waits = []  # (row id, pattern, slow)
	seen_logs = []  # (row id, lines) of earlier selected rows

	xyz_roots = set(VANILLA_XYZ) | {"cdevtest"}
	if not client:
		try:
			xyz_roots |= set(_qrun().probe_map())  # probe commands take block coordinates too (zpwg3 place X Y Z ...)
		except Exception:
			pass
	section = None
	top_boot = {}   # (section, world group) -> highest @boot seen so far (W boot)
	for r in rows:
		rid = r["id"]
		mv = r.get("meta", {}).get("modset")
		if mv is not None:
			import qrows
			err = qrows.modset_error(mv)
			if err:
				F.append(("E", "modset", rid, r["line"], f"@modset {mv}: {err}"))
		bv = r.get("meta", {}).get("boot")
		if bv is not None:
			if not re.fullmatch(r"[1-9]\d*", bv.strip()):
				F.append(("E", "boot", rid, r["line"], f"@boot wants a whole number >= 1, got {bv!r}"))
		b = int(bv) if bv and re.fullmatch(r"[1-9]\d*", bv.strip()) else 1
		try:
			from qdrive import world_letter
			gk = (r.get("section"), world_letter(r.get("meta", {}).get("group")))
		except Exception:  # noqa: BLE001
			gk = (r.get("section"), "A")
		if b < top_boot.get(gk, 1):
			F.append(("W", "boot", rid, r["line"], f"@boot {b} after a row of boot {top_boot[gk]} in this section and "
												   f"world group: the driver runs it first (boots in order)"))
		top_boot[gk] = max(top_boot.get(gk, 1), b)
		if r.get("section") != section:
			# a rows file (feature_writeup/testing/rows/*.qb) runs one section per driver step on a fresh world: another
			# section's forceloads do not carry over (r1118 judge-9 / judge-10: the PWG setup-row forceloads were lost)
			if section is not None:
				boxes = {}
			section = r.get("section")
		row_unloaded = False
		row_slow = False
		for (w, a, _), ln in zip(r["steps"], r["lines"]):
			def f(sev, code, msg):
				F.append((sev, code, rid, ln, msg))

			# variables
			if w in ("cmd", "shell", "waitlog"):
				for m in VAR_RX.finditer(a):
					v = m.group(1)
					base = re.sub(r"\d+$", "", v)
					if v in captured or v in vars0 or base in captured:
						continue
					where = cap_where.get(v) or cap_where.get(base)
					if where is None:
						if v in warned_vars:
							continue
						warned_vars.add(v)
						f("W", "var", f"{{{v}}} never captured in this batch and not in --vars (an earlier batch's "
									  f"vars.json?)")
					else:
						if (rid, base) in warned_vars:
							continue
						warned_vars.add((rid, base))
						skipped = [x for x, _ in where if x not in sel]
						if skipped:
							f("E", "var", f"{{{v}}} is captured in {', '.join(skipped)}, which the selection skips "
										  f"(use --rows with that row first)")
						else:
							f("E", "var", f"{{{v}}} used before its capture (line {where[0][1]})")
			if w == "sleep" and not re.fullmatch(NUM, a):
				f("E", "arg", f"sleep wants seconds, got {a!r}: qrun stops with a ValueError here")
			if w == "click" and not re.fullmatch(r"\d+ \d+", a):
				f("E", "arg", f"click wants X Y, got {a!r}")
			# restart verb (r1118 R4 A2)
			if w == "restart":
				if a and not re.fullmatch(NUM, a):
					f("E", "restart", f"restart wants nothing or the seconds to wait for Done, got {a!r}")
				if client:
					f("E", "restart", "restart restarts a scratch server: not in a client batch")
			if w == "edit" and not any(w2 == "restart" for (w2, _, _), ln2 in zip(r["steps"], r["lines"]) if ln2 > ln):
				f("E", "restart", "edit runs at the row's next restart, and no restart follows it in this row")
			# regexes
			if w in ("waitlog", "expect", "reject", "bootexpect", "bootreject", "kjsexpect", "kjsreject", "capture",
					 "count"):
				pat = a
				if w == "capture":
					pat = a.partition(" ")[2]
				elif w == "waitlog":
					parts = a.rsplit(" ", 1)
					pat = parts[0] if len(parts) == 2 and parts[1].isdigit() else a
				elif w == "count":
					m = re.match(r"(.+)\s+(==|>=|<=|>|<)\s+(\d+)$", a)
					if not m:
						f("E", "count", f"count wants 'REGEX OP N': {a[:80]}")
						continue
					pat = m.group(1)
				err = _regex_err(VAR_RX.sub("0", pat))
				if err:
					f("E", "regex", f"/{pat[:80]}/: {err}")
			if w in ("kjsexpect", "kjsreject") and STARTUP_LINES.search(a):
				f("W", "kjs", "this line is a startup-script print (logs/kubejs/startup.log); kjsexpect reads server.log: "
							  "use a shell grep on startup.log")
			if w in ("kjsexpect", "kjsreject") and reloaded:
				f("W", "kjs", "after a /reload in this batch: the KubeJS log was truncated by it")
			if w == "waitlog":
				parts = a.rsplit(" ", 1)
				pat = parts[0] if len(parts) == 2 and parts[1].isdigit() else a
				# no waitlog2 warning since r1118: qrun waits for a new matching line when a row repeats a waitlog
				# pattern (judge-14), so a repeat no longer returns at once on the first's line
				try:
					rx = re.compile(VAR_RX.sub("0", pat))
				except re.error:
					rx = None
				hit = None
				if rx and logs:
					for pid, lines in reversed(seen_logs):
						h = next((l for l in lines if rx.search(l)), None)
						if h:
							hit = (pid, h)
							break
				if hit:
					f("W", "waitlog", f"/{pat[:60]}/ also matches {hit[0]}'s reply in the earlier run: "
									  f"{hit[1].strip()[:100]}")
				else:
					same = [p for p in prev_waits if p[1] == pat and p[2]]
					if same:
						f("W", "waitlog", f"/{pat[:60]}/ is also {same[-1][0]}'s wait for a slow reply: a late "
										  f"reply to that row satisfies this one")
				lastcmd = next((x for (ww, x, _), l2 in reversed(list(zip(r["steps"], r["lines"])))
								if ww == "cmd" and l2 < ln), "")
				lc = _run_part(lastcmd)
				lm = re.match(r"(?:locate\s+(?:structure|biome|poi)?\s*|cwglocate\s+\S+\s+)(#?[a-z0-9_.-]+:[a-z0-9_/.-]+)", lc)
				if lm and lm.group(1).split(":")[-1].split("/")[-1] not in pat:
					f("W", "waitlog", f"locate waitlog /{pat[:50]}/ does not name its target {lm.group(1)}: an older "
									  f"locate's late reply can satisfy it")
			if w == "capture":
				captured.add(a.partition(" ")[0])
				prev_waits.append((rid, a.partition(" ")[2], row_slow))
			if w == "shell":
				if _shell_grepc(a):
					f("E", "grepc", "grep -c decides the exit status: 0 matches exits 1 = FAIL. End with '; true' or "
									"test the number")
				if re.search(r"pipefail", a) and re.search(r"grep\s+-[A-Za-z]*q", a):
					f("W", "pipefail", "pipefail with grep -q: the writer's SIGPIPE reads as not found")
				if re.search(r"\bgrep\b.*\{\d*,\d{3,}\}", a):
					f("W", "ugrep", "{n,m} repeat over 99: ugrep refuses it; parse in python")
				if re.search(r'(sed\s+-i|qrun\.py"?\s+toml|>\s*\S*config/)', a) and re.search(r"config/|\.toml|\.json5?\b", a):
					f("W", "cfgedit", "config edited while the game runs: Forge may not reload it (r1110 SQ-WO-34, "
									  "r1114 SQ-TP-22b); set it with qrun.py toml before the boot")
			if w != "cmd":
				continue
			c = a.strip().lstrip("/")
			rc = _run_part(c)
			if re.search(r"\bexecute\b.*\b(if|unless) biome\b", c):
				f("E", "cmd119", "execute if biome is 1.19.3+: a command error on 1.19.2")
			if re.match(r"tick\b", rc):
				f("E", "cmd119", "/tick does not exist in 1.19.2")
			if re.match(r"reload\b", rc):
				reloaded = True
			if SLOW.match(rc):
				row_slow = True
			# markers
			for m in re.finditer(r"Tags:\[([^\]]*)\]", c):
				tags.update(t.strip().strip('"') for t in m.group(1).split(","))
			m = re.match(r"tag\s+\S+\s+add\s+(\S+)", rc)
			if m:
				tags.add(m.group(1))
			m = re.match(r"pbot\s+spawn\s+(\S+)", rc)
			if m:
				tags.add(m.group(1))
			if not re.match(r"(kill|tag\s+\S+\s+remove)\b", rc) and not re.search(r"\b(if|unless) entity\b", c):
				for m in re.finditer(r"tag=(?!!)([A-Za-z0-9_.+-]+)", c):
					if m.group(1) not in tags and "{" not in m.group(1):
						f("W", "marker", f"tag={m.group(1)} selected, but no earlier selected row summons or tags it "
										 f"(marker from another boot or a skipped row?)")
						tags.add(m.group(1))
			# forceload and coordinates
			dim = _dim(c)
			m = re.match(rf"forceload\s+add\s+({NUM}|\{{[^}}]+\}})\s+({NUM}|\{{[^}}]+\}})(?:\s+({NUM}|\{{[^}}]+\}})\s+({NUM}|\{{[^}}]+\}}))?", rc)
			if m:
				vals = [x for x in m.groups() if x is not None]
				if all(re.fullmatch(NUM, x) for x in vals):
					x1, z1 = int(float(vals[0])), int(float(vals[1]))
					x2, z2 = (int(float(vals[2])), int(float(vals[3]))) if len(vals) == 4 else (x1, z1)
					b = (min(x1, x2) >> 4, min(z1, z2) >> 4, max(x1, x2) >> 4, max(z1, z2) >> 4)
					n = (b[2] - b[0] + 1) * (b[3] - b[1] + 1)
					if n > 256:
						f("E", "forceload", f"box of {n} chunks: refused (max 256)")
					boxes.setdefault(dim, []).append(b)
				else:
					boxes.setdefault(dim, []).append(None)  # unknown box: stop judging this dimension
				continue
			if re.match(r"forceload\s+remove\s+all", rc):
				boxes[dim] = []
				continue
			if client or rc.startswith("forceload") or c.split(" ", 1)[0] not in xyz_roots:
				continue
			if None in boxes.get(dim, []) or row_unloaded:
				continue
			flat = _strip_nbt(c)
			for m in re.finditer(rf"(?<![\w~^.-])({NUM})\s+({NUM}|~)\s+({NUM})(?![\w.])", flat):
				x, z = float(m.group(1)), float(m.group(3))
				if m.group(2) != "~" and not -64 <= float(m.group(2)) <= 320:
					continue
				if dim == "minecraft:overworld" and abs(x) <= 160 and abs(z) <= 160:
					continue  # spawn chunks
				cx, cz = int(x) >> 4, int(z) >> 4
				if not any(b[0] <= cx <= b[2] and b[1] <= cz <= b[3] for b in boxes.get(dim, [])):
					f("W", "unloaded", f"{m.group(0)} ({dim.split(':')[-1]}) is outside every forceload of the selected "
									   f"rows so far (another batch's forceload, a bot nearby, or a stale coordinate)")
					row_unloaded = True
					break
		for (w, a, _), ln in zip(r["steps"], r["lines"]):
			if w == "waitlog":
				parts = a.rsplit(" ", 1)
				prev_waits.append((rid, parts[0] if len(parts) == 2 and parts[1].isdigit() else a, row_slow))
		if logs and rid in logs:
			seen_logs.append((rid, logs[rid]))
	return F


INST_ROWS = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../feature_writeup/testing/rows"))


def load(batch, opts):
	q = _qrun()
	perr = []
	rows = q.parse(batch, errors=perr)
	sel = select(rows, opts.get("rows"), opts.get("start"))
	vars0 = {}
	vf = opts.get("vars") or (os.path.join(opts["res"], "vars.json") if opts.get("res") else None)
	if vf and os.path.exists(vf):
		vars0 = json.load(open(vf))
	logs = None
	if opts.get("res") and os.path.isdir(os.path.join(opts["res"], "rows")):
		logs = {}
		for r in rows:
			p = os.path.join(opts["res"], "rows", r["id"] + ".log")
			if os.path.exists(p):
				logs[r["id"]] = open(p, errors="replace").read().splitlines()
	return rows, sel, vars0, logs, perr


def lint(batch, opts):
	rows, sel, vars0, logs, perr = load(batch, opts)
	out = lint_rows(sel, rows, opts.get("client"), vars0, logs, perr)
	if os.path.abspath(batch).startswith(os.path.join(INST_ROWS, "")):
		# queue row file: stale rows and dead ids (qrowlint.py); baseline dead ids stay warnings
		import qrowlint
		base = qrowlint.baseline()
		ids = {r["id"] for r in sel}
		for sev, ln, rid, msg in qrowlint.lint_file(batch):
			if rid not in ids:
				continue
			if sev == "E" and qrowlint._key(batch, rid, msg) in base:
				sev, msg = "W", msg + " (baseline)"
			code = "stale" if msg.startswith("stale") else "trail-space" if "ends in a space" in msg else "dead-id"
			out.append((sev, code, rid, ln, msg))
	return out


def opts_from(argv):
	o = {"rows": None, "start": None, "vars": None, "res": None, "client": False, "quiet": False}
	files, i = [], 0
	while i < len(argv):
		x = argv[i]
		if x == "--rows":
			o["rows"] = set(argv[i + 1].split(","))
			i += 1
		elif x == "--from":
			o["start"] = argv[i + 1]
			i += 1
		elif x == "--vars":
			o["vars"] = argv[i + 1]
			i += 1
		elif x == "--res":
			o["res"] = argv[i + 1]
			i += 1
		elif x == "--client":
			o["client"] = True
		elif x == "--quiet":
			o["quiet"] = True
		else:
			files.append(x)
		i += 1
	return files, o


def fmt(batch, x):
	sev, code, rid, ln, msg = x
	return f"{batch}:{ln}: {sev} {code} {rid + ': ' if rid else ''}{msg}"


def main_lint(argv):
	files, o = opts_from(argv)
	if not files:
		sys.exit(__doc__)
	nerr = nwarn = 0
	for b in files:
		fs = lint(b, o)
		for x in fs:
			if not (o["quiet"] and x[0] == "W"):
				print(fmt(b, x))
		nerr += sum(1 for x in fs if x[0] == "E")
		nwarn += sum(1 for x in fs if x[0] == "W")
	print(f"lint: {len(files)} batch(es), {nerr} error(s), {nwarn} warning(s)")
	return 1 if nerr else 0


def _est(r):
	lo = hi = 2.0
	for w, a, pace in r["steps"]:
		w = w.rpartition(":")[2]
		if w == "cmd":
			lo += pace
			hi += pace
		elif w == "sleep" and re.fullmatch(NUM, a):
			lo += float(a)
			hi += float(a)
		elif w in ("key", "click"):
			lo += 1
			hi += 1
		elif w == "waitlog":
			parts = a.rsplit(" ", 1)
			hi += float(parts[1]) if len(parts) == 2 and parts[1].isdigit() else 120.0
		elif w == "restart":
			lo += 90.0   # stop and a boot
			hi += 30.0 + (float(a) if re.fullmatch(NUM, a or "") else 600.0)
	return lo, hi


def main_dry(argv):
	files, o = opts_from(argv)
	if len(files) != 1:
		sys.exit(__doc__)
	batch = files[0]
	rows, sel, vars0, logs, perr = load(batch, o)
	q = _qrun()
	q.VARS.clear()
	q.VARS.update({k: str(v) for k, v in vars0.items()})
	fs = lint_rows(sel, rows, o["client"], vars0, logs, perr)
	by_line = {}
	for x in fs:
		by_line.setdefault((x[2], x[3]), []).append(x)
	for x in fs:
		if not x[2]:
			print(fmt(batch, x))
	tlo = thi = 0.0
	ncmd = nchk = 0
	capby = {}
	for r in sel:
		lo, hi = _est(r)
		tlo, thi = tlo + lo, thi + hi
		print(f"\n## {r['id']} | {r['title']}   (line {r['line']}, {lo:.0f}..{hi:.0f} s)")
		shown = set()
		for (w, a, pace), ln in zip(r["steps"], r["lines"]):
			side, _, w = w.rpartition(":")
			if w in ("cmd", "shell", "waitlog"):
				a2 = q.sub(a)
				a2 = VAR_RX.sub(lambda m: m.group(0) + "<-" + capby.get(m.group(1), capby.get(re.sub(r"\d+$", "", m.group(1)), "?")), a2)
			else:
				a2 = a
			tag = (side + ("" if w == "cmd" else ":") if side else "") + {"cmd": ">", "shell": "$"}.get(w, w)
			if w == "cmd":
				ncmd += 1
			elif w in ("expect", "reject", "count", "bootexpect", "bootreject", "kjsexpect", "kjsreject", "look"):
				nchk += 1
			print(f"  {ln:>4} {tag} {a2}" + (f"   [pace {pace:g}]" if w == "cmd" else ""))
			if (r["id"], ln) not in shown:
				for x in by_line.get((r["id"], ln), []):
					print(f"       ^ {x[0]} {x[1]}: {x[4]}")
				shown.add((r["id"], ln))
			if w == "capture":
				capby[a.partition(" ")[0]] = r["id"]
	skipped = [r["id"] for r in rows if r not in sel]
	print(f"\ndry: {len(sel)} of {len(rows)} rows ({len(skipped)} skipped), {ncmd} commands, {nchk} checks and looks, "
		  f"{tlo / 60:.1f}..{thi / 60:.1f} min; lint {sum(1 for x in fs if x[0] == 'E')} E, "
		  f"{sum(1 for x in fs if x[0] == 'W')} W")
	return 0


if __name__ == "__main__":
	what = sys.argv[1] if len(sys.argv) > 1 else ""
	if what == "lint":
		sys.exit(main_lint(sys.argv[2:]))
	if what == "dry":
		sys.exit(main_dry(sys.argv[2:]))
	sys.exit(__doc__)

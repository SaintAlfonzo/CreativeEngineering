#!/usr/bin/env python3
"""Runs a scratch-queue batch file against a scratch server or a throwaway client and records a result per row.

    qrun.py run OUT BATCH RES [--rows ID,ID,...] [--from ID] [--section K,..] [--tier T1,..] [watch options]
                                                                server: OUT = scratch_server.sh dir; BATCH may be a
                                                                queue row file (feature_writeup/testing/rows/*.qb,
                                                                qrows.py): --section / --tier select from it, rows
                                                                without steps (@draft) are listed and not run
    qrun.py client GAME BATCH RES [--rows ...] [--from ID] [watch options] [--xdotool]   client: GAME = eval_client.py
                                                                or run_captures.py serve game dir
    qrun.py list BATCH                                          row ids, titles and kinds
    qrun.py lint BATCH [BATCH ...] [--rows|--from|--vars F|--res RES|--client|--quiet]   offline batch checks (qlint.py)
    qrun.py dry BATCH [same options]                            every row's commands and checks, vars substituted,
                                                                lint findings inline, time estimate; no server
    qrun.py judge RES [...]                                     open LOOKs; fill one (qverdict.py docstring)
    qrun.py table RES [RES ...] [--ids ..] [--open]             run-doc verdict table from RES/verdicts/
    qrun.py toml FILE SECTION KEY VALUE                         set KEY under [SECTION] (added if absent); SECTION
                                                                `-` (or '') = a top-level key, before the first [section]
    qrun.py plan [--jars a,b] [--never] [--failed] [--rewritten] [--all] [--release] ...   rows a run needs from jar stamps
                                                                and sources (qplan.py docstring)
    qrun.py playtest [--out FILE] / qrun.py states              owner playtest checklist / row states per code
    qrun.py drive start|resume|status|stop RUN [plan options]   resumable run driver (qdrive.py docstring)
    qrun.py prep FILE SECTION [A|B] [--boot N] [--out OUT --level LEVEL]   a rows section's @@prep (qrows.py docstring): print it;
                                                                with --out run prep_srv.sh with it and apply its toml /
                                                                copy / rm actions, as the driver does before a boot

run / client: lint runs first (errors printed, the batch runs anyway; --no-lint skips it). Crash watch (qwatch.py,
--no-watch skips it): the JVM with cwd OUT/srv or GAME (or --pid P), new hs_err_pid*.log and crash-reports/ files are
checked before every step and every second of a waitlog or sleep; the row ends CRASH, evidence goes to RES/crash/ID/. Then
--relaunch CMD (bash, cwd = instance, e.g. an eval_client.py launch plus a join step) restarts the game and the batch
goes on with the next row once --ready REGEX (default server 'Done \(', client 'joined the game') shows in latest.log
within --ready-secs (600); at most --max-relaunch (2) times. Without --relaunch a crash ends the batch. A row that
sends "stop" ends the batch without a CRASH; a row that needs the server back uses `restart` instead (r1118 R4 A2).
run (server): before the first row, the scratch KubeJS probes (tools/**/*_probe.js and any other tools/**/*.js that
registers a server command, qrun.probe_files) whose commands the selected rows use
are copied into srv/kubejs/server_scripts as zz_<file> and the server is /reloaded once (--no-probes skips it).

Batch format (one directive per line, '#' comments, blank lines ignored):
    ## ID | title                 starts a row
    > command                     console command (server) or chat command typed into the window (client)
    sleep N                       wait N seconds
    waitlog REGEX [SECS]          wait until the row's log segment matches (default 120 s); FAIL on timeout. A repeat
                                  of an earlier waitlog pattern in the same row waits for a new matching line
    expect REGEX                  the row's log segment must match
    reject REGEX                  the row's log segment must not match
    count REGEX OP N              lines of the segment matching REGEX, OP one of == >= <= > <
    bootexpect REGEX              the whole log (from boot) must match
    bootreject REGEX              the whole log must not match
    kjsexpect REGEX / kjsreject REGEX   same against the KubeJS log (logs/kubejs/server.log or client.log); /reload
                                  truncates that file, so put these rows before any /reload
    look TEXT                     a human or agent judges this; the row ends LOOK unless a check FAILed
    shot NAME                     client: window screenshot RES/shots/ID_NAME.png (server: ignored)
    key KEYS                      client: xdotool key KEYS (e.g. "e", "Escape", "F3+t")
    click X Y                     client: click at window coordinates
    do JSON                       client on the capture driver only: one driver step object or a list of them
                                  (feature_writeup/client_capture.md 2.2: readback, walk, click_widget, type_into ...);
                                  a failed step (its expect / reject included) FAILs the row; READ lines land in latest.log

Client transport: when GAME/cdev_channel/state.json is a fresh SERVE heartbeat (client started by
tools/client_capture/run_captures.py serve), client mode drives the in-process capture driver over that file channel
(client_capture.md 2.11): '>' commands run on the integrated server as the player, key / click / shot become driver
steps (no xdotool, no focus or typing loss), shots go to test_captures/judgement/qrun_<RES name>/ for the owner (the
row ends LOOK). --xdotool forces the old window transport.
    pace N                        seconds after each command for the rest of the batch (default 2)
    repeat N ... end              the steps between run N times (no nesting)
    capture NAME REGEX            last segment line matching REGEX sets {NAME} (group 1), {NAME1}..{NAMEn} (groups);
                                  {NAME}, {NAME+N}, {NAME-N} in later commands, shell lines and waitlog patterns are replaced;
                                  kept in RES/vars.json for later batches and boots; FAIL if nothing matches. Locate: capture P is at \[(-?\d+), (~|-?\d+), (-?\d+)\]
    shell CMD                     bash CMD run in OUT/srv (server) or GAME (client), e.g. to edit a scratch file;
                                  $INST = the instance root
    edit CMD                      server: bash CMD queued for the row's next restart; it runs in OUT/srv while the
                                  server is down ($INST, $OUT set; output in RES/rows/ID.shell.txt), e.g. a qrun.py toml
                                  edit or a jar swap. A failed edit FAILs the row (HARNESS) and the restart still boots
    restart [SECS]                server: a deliberate stop (not a crash), wait for the JVM to exit, the queued edit
                                  lines, boot, wait up to SECS (default 600) for "Done (", then the row and the batch go
                                  on on the new boot. The row's log segment keeps its lines from before the restart;
                                  waitlog / capture after it read the new boot's log; bootexpect reads the new boot.
                                  The old log is kept as srv/logs/qrun_bootN.log. Probes copied in stay (they load at
                                  boot). No restart within SECS: the row FAILs and the batch ends (ABORT)

Every row is also a perf segment: when a perf session is open (tools/perf/session/perf_session.sh) the row ID is
marked there; RES/timeline.tsv always gets the wall-clock start and end of each row, so a JFR recording (server or
client) can be cut to a row later. Results: RES/results.json (merged across runs), RES/results.md, RES/rows/ID.log
(the row's log segment), RES/rows/ID.shell.txt (shell step output), RES/verdicts/ID.json (every check with its
outcome, the LOOKs to judge, shots, crash; see qverdict.py). Regexes are Python, matched per line.
"""
import json
import os
import re
import subprocess
import sys
import time


def fsid(rid):
	"""A row id as a file name (qverdict.fsid): '/' in an id becomes '_' (r1120nv step 23)."""
	return rid.replace("/", "_")

INST = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
SCRATCH = os.environ.get("QRUN_SCRATCH", os.path.join(INST, "tools/creative_labels_test/scratch_server.sh"))
PERF = os.path.join(INST, "tools/perf/session/perf_session.sh")
OPS = ("==", ">=", "<=", ">", "<")


def parse(path, errors=None):
	"""Rows of the batch. Each row: id, title, line (of its ## header), steps [(word, arg, pace)] and lines (the
    batch line of each step, parallel to steps; repeat copies keep the line of their source). errors: a list to
    collect format errors into instead of exiting (lint and dry)."""
	rows, pace, cur, rep, section, sec_modset = [], 2.0, None, None, None, ""

	def bad(n, msg):
		if errors is None:
			sys.exit(f"{path}:{n}: {msg}")
		errors.append((n, msg))

	with open(path) as f:
		for n, raw in enumerate(f, 1):
			line = raw.rstrip("\n")
			s = line.strip()
			if not s or (s.startswith("#") and not s.startswith("## ")):
				continue
			if s.startswith("@@"):
				# queue row file (feature_writeup/testing/rows/*.qb, qrows.py): section start and section text
				word, _, rest = s[2:].partition(" ")
				if word == "section":
					section, cur, sec_modset = rest.strip(), None, ""
				elif word == "modset":
					sec_modset = rest.strip()   # the section's jar set (qlint W boot keys on the effective set)
				continue
			if s.startswith("@"):
				if cur is not None:
					word, _, rest = s[1:].partition(" ")
					cur["meta"][word] = rest.strip()
				continue
			if s.startswith("## "):
				rid, _, title = s[3:].partition("|")
				cur = {"id": rid.strip(), "title": title.strip(), "line": n, "steps": [], "lines": [], "meta": {},
					   "section": section, "section_modset": sec_modset}
				rows.append(cur)
				continue
			if s.startswith("pace "):
				pace = float(s.split()[1])
				continue
			if cur is None:
				bad(n, "directive before the first row")
				continue
			if s.startswith("repeat "):
				rep = (int(s.split()[1]), len(cur["steps"]), n)
				continue
			if s == "end":
				if rep is None:
					bad(n, "end without repeat")
					continue
				times, at, _ = rep
				cur["steps"].extend(cur["steps"][at:] * (times - 1))
				cur["lines"].extend(cur["lines"][at:] * (times - 1))
				rep = None
				continue
			if s.startswith(("srv> ", "cli> ")):
				cur["steps"].append((s[:3] + ":cmd", s[5:], pace))
			elif s.startswith("> "):
				cur["steps"].append(("cmd", s[2:], pace))
			else:
				word, _, rest = s.partition(" ")
				side, _, base = word.rpartition(":")
				if side not in ("", "srv", "cli") or base not in (
						"capture", "shell", "sleep", "waitlog", "expect", "reject", "count", "bootexpect", "bootreject",
						"kjsexpect", "kjsreject", "look", "shot", "key", "click", "do", "edit", "restart"):
					bad(n, f"unknown directive {word!r}")
					continue
				cur["steps"].append((word, rest.strip(), pace))
			cur["lines"].append(n)
	if rep is not None:
		bad(rep[2], "repeat without end")
	return rows


class Log:
	def __init__(self, path):
		self.path = path

	def size(self):
		try:
			return os.path.getsize(self.path)
		except OSError:
			return 0

	def since(self, off):
		try:
			with open(self.path, "rb") as f:
				f.seek(off)
				return f.read().decode("utf-8", "replace").splitlines()
		except OSError:
			return []


class Server:
	kind = "server"

	def __init__(self, out):
		self.out = out
		self.dir = os.path.join(out, "srv")
		self.log = Log(os.path.join(out, "srv/logs/latest.log"))
		self.kjs = Log(os.path.join(out, "srv/logs/kubejs/server.log"))

	def cmd(self, c):
		r = subprocess.run(["bash", SCRATCH, "cmd", self.out, c], capture_output=True, text=True)
		if r.returncode:
			raise RuntimeError("server not reading the fifo")

	def shot(self, path):
		return False

	def key(self, k):
		pass

	def click(self, x, y):
		pass

	def restart(self, edits, secs, shell_log):
		"""The restart directive (r1118 R4 A2): stop (the watch is told it is deliberate), wait for the JVM to exit,
		run the queued edit lines in srv, boot, wait for 'Done ('. Returns (ok, detail, [(cmd, rc, output)])."""
		import qwatch
		w = getattr(self, "watch", None)
		if w is not None:
			w.stopping = True
		subprocess.run(["bash", SCRATCH, "stop", self.out], capture_output=True, text=True, stdin=subprocess.DEVNULL)
		end = time.time() + 120
		while qwatch.jvm_pids(self.dir) and time.time() < end:
			time.sleep(1)
		if qwatch.jvm_pids(self.dir):
			return False, "JVM still up 120 s after stop (not killed)", []
		self.boots = getattr(self, "boots", 1)
		logs = os.path.join(self.dir, "logs")
		if os.path.exists(self.log.path):
			import shutil
			shutil.copy2(self.log.path, os.path.join(logs, f"qrun_boot{self.boots}.log"))
		ran = []
		for c in edits:
			r = subprocess.run(["bash", "-c", c], cwd=self.dir, capture_output=True, text=True,
							   env=dict(os.environ, INST=INST, OUT=os.path.abspath(self.out)), stdin=subprocess.DEVNULL)
			ran.append((c, r.returncode, r.stdout + r.stderr))
			with open(shell_log, "a") as so:
				so.write(f"$ (edit, server down) {c}\n{r.stdout}{r.stderr}\n")
		# output to a file, never a pipe: the server's fifo feeder inherits the fds and holds a pipe open (r1118)
		os.makedirs(logs, exist_ok=True)
		with open(os.path.join(logs, "qrun_restart.out"), "a") as bf:
			subprocess.run(["bash", SCRATCH, "boot", self.out], stdout=bf, stderr=subprocess.STDOUT,
						   stdin=subprocess.DEVNULL)
		self.boots += 1
		end = time.time() + secs
		ok = False
		while time.time() < end:
			if any("Done (" in x for x in self.log.since(0)):
				ok = True
				break
			if w is not None and w.new_files():
				break
			time.sleep(2)
		if w is not None:
			w.files0 = qwatch.crash_files(self.dir) if ok else w.files0
			w.pids = qwatch.jvm_pids(self.dir)
			w.stopping = False
		if not ok:
			return False, f"no 'Done (' within {int(secs)} s after the restart (srv/logs/latest.log)", ran
		return True, f"boot {self.boots} ready", ran


class Client:
	kind = "client"

	def __init__(self, game):
		self.dir = game
		self.log = Log(os.path.join(game, "logs/latest.log"))
		self.kjs = Log(os.path.join(game, "logs/kubejs/client.log"))
		w = subprocess.run(["xdotool", "search", "--onlyvisible", "--name", "^Minecraft"],
						   capture_output=True, text=True).stdout.split()
		if not w:
			sys.exit("no Minecraft window")
		self.w = w[0]

	def _x(self, *a):
		subprocess.run(["xdotool", *a], check=False)

	def cmd(self, c):
		self._x("windowactivate", "--sync", self.w)
		self._x("key", "--window", self.w, "t")
		time.sleep(0.8)
		# on a hitch the opening "t" also lands in the chat box ("t/gamemode ..." goes out as a chat message): clear it
		self._x("key", "--window", self.w, "ctrl+a", "BackSpace")
		time.sleep(0.3)
		# batch lines are console commands (no slash); in chat they must start with one or they are sent as messages
		if not c.startswith("/"):
			c = "/" + c
		# 40 ms and a pause after Return keep chat in step
		self._x("type", "--window", self.w, "--delay", "40", "--", c)
		time.sleep(0.6)  # a shorter pause lets Return overtake the last characters on a hitch ("minecraft:air" sent as "minecra")
		self._x("key", "--window", self.w, "Return")
		time.sleep(0.7)

	def shot(self, path):
		subprocess.run(["import", "-window", self.w, path], check=False)
		return True

	def key(self, k):
		self._x("windowactivate", "--sync", self.w)
		self._x("key", "--window", self.w, *k.split())

	def click(self, x, y):
		self._x("mousemove", "--window", self.w, x, y, "click", "1")


class DriverClient:
	"""Client transport over the capture driver's file channel (TX-02). Each directive is one request; the call
    returns when the driver has run it, so no pace sleeps are needed."""
	kind = "client"
	pace_scale = 0
	XKEYS = {"Escape": "escape", "Return": "enter", "BackSpace": "backspace", "space": "space", "Tab": "tab"}

	def __init__(self, game):
		sys.path.insert(0, os.path.join(INST, "tools/client_capture"))
		from chan import Channel
		self.dir = game
		self.log = Log(os.path.join(game, "logs/latest.log"))
		self.kjs = Log(os.path.join(game, "logs/kubejs/client.log"))
		self.chan = Channel(os.path.join(game, "cdev_channel"))
		if not self.chan.serving():
			sys.exit("capture driver not serving in " + self.chan.dir + " (tools/client_capture/run_captures.py serve)")
		self.row = "qrun"
		self.judge = None

	def run(self, steps, timeout=300.0):
		from chan import ChannelError
		try:
			return self.chan.request(self.row, steps, timeout=timeout)
		except ChannelError as e:
			raise RuntimeError(f"capture driver: {e}")

	def cmd(self, c):
		self.run([{"cmd": c, "allow_fail": True}])

	def do(self, arg):
		steps = json.loads(arg)
		steps = steps if isinstance(steps, list) else [steps]
		walk = sum(int(s.get("walk", 0)) + int(s.get("after", 0)) for s in steps if isinstance(s, dict))
		r = self.run(steps, timeout=300.0 + walk / 20.0)
		return r.get("error", "") if r.get("status") != "ok" else ""

	def shot(self, path):
		"""Pixels go only to the judgement folder (plan scratch_testing_expansion 0); returns the copied file."""
		name = os.path.basename(path)[:-4]
		r = self.run([{"capture": {"name": ""}}])
		raw = [f for f in r.get("files", []) if not f.endswith("__failed.png")]
		if not raw or not self.judge:
			return False
		os.makedirs(self.judge, exist_ok=True)
		dst = os.path.join(self.judge, name + ".png")
		import shutil
		shutil.copy2(os.path.join(self.chan.raw, raw[0]), dst)
		side = os.path.join(self.chan.raw, raw[0][:-4] + ".json")
		if os.path.exists(side):
			shutil.copy2(side, dst[:-4] + ".json")
		mp = os.path.join(self.judge, "manifest.json")
		man = json.load(open(mp)) if os.path.exists(mp) else {"judgement_rule": "owner judges every image", "shots": []}
		man["shots"].append({"row": self.row, "file": name + ".png", "verdict": None, "judge": "owner",
							 "at": time.strftime("%Y-%m-%d %H:%M:%S")})
		json.dump(man, open(mp, "w"), indent=1)
		return os.path.relpath(dst, INST)

	def key(self, k):
		steps = []
		for combo in k.split():
			parts = combo.split("+")
			if [p.lower() for p in parts] == ["f3", "t"]:
				steps.append({"reload": True})
				continue
			shift = any(p.lower() == "shift" for p in parts[:-1])
			steps.append({"press": self.XKEYS.get(parts[-1], parts[-1].lower()), "shift": shift})
		self.run(steps)

	def click(self, x, y):
		self.run([{"click": {"px": float(x), "py": float(y)}}])


XDOTOOL = False  # --xdotool: client mode keeps the window transport even when a driver serves


def driver_serving(game):
	try:
		st = json.load(open(os.path.join(game, "cdev_channel", "state.json")))
	except (OSError, ValueError):
		return False
	return st.get("phase") == "SERVE" and time.time() - st.get("time", 0) / 1000.0 < 60


def perf_mark(label):
	if os.path.exists(os.path.join(INST, "perf/sessions/CURRENT")):
		subprocess.run(["bash", PERF, "mark", label], capture_output=True)


def compare(n, op, m):
	return {"==": n == m, ">=": n >= m, "<=": n <= m, ">": n > m, "<": n < m}[op]


VARS = {}
CAPTURES = set()   # capture names defined anywhere in the batch (run_batch fills it)


def sub(s):
	def one(m):
		v = VARS.get(m.group(1))
		if v is None:
			return m.group(0)
		return str(int(v) + int(m.group(2))) if m.group(2) else v
	return re.sub(r"\{([A-Za-z_][A-Za-z_0-9]*)([+-]\d+)?\}", one, s)


def watch_check(t):
	"""Crash watch (qwatch.py, TX-06): raises qwatch.Crashed when the game JVM died or wrote a crash file."""
	w = getattr(t, "watch", None)
	if w is not None:
		w.check()


def run_row(t, row, res, checks=None):
	"""Runs one row. checks: a list that gets one {step, arg, ok, detail} per check (the verdict file, TX-45); on a
    crash (qwatch.Crashed) the caller still has the checks made so far."""
	off = t.log.size()
	fails, looks, shots = [], [], []
	checks = [] if checks is None else checks

	def chk(step, arg, ok, detail="", harness=False):
		checks.append({"step": step, "arg": arg, "ok": ok, "detail": detail} | ({"harness": True} if harness else {}))
		if not ok:
			fails.append(("HARNESS: " if harness else "") + detail)

	def rx_ok(step, pat):
		"""HARNESS (P2.4): a regex that does not compile is a batch fault, not a game FAIL."""
		try:
			re.compile(pat)
			return True
		except re.error as e:
			chk(step, pat, False, f"regex does not compile: {e}", harness=True)
			return False

	def unset(step, arg):
		"""HARNESS (P2.4): a {NAME} still unresolved after substitution names a capture whose row was skipped or failed.
		An upper-case {NAME} (the capture naming the rows use) that no capture in this batch defines, and that RES/vars.json
		did not supply, is a capture from another batch or boot that never ran: also HARNESS, not sent raw (judge-8)."""
		names = re.findall(r"\{([A-Za-z_][A-Za-z_0-9]*)(?:[+-]\d+)?\}", arg)
		left = [n for n in names if n in CAPTURES]
		foreign = [n for n in names if n not in CAPTURES and re.fullmatch(r"[A-Z][A-Z0-9_]*", n)]
		if left:
			chk(step, arg, False, f"capture {', '.join(left)} unset (its row was skipped or failed)", harness=True)
		if foreign:
			chk(step, arg, False, f"placeholder {', '.join(foreign)} unset (no capture in this batch, not in RES/vars.json)",
				harness=True)
		return bool(left or foreign)

	def since(o, side):
		"""Log lines since o; side 'srv' / 'cli' (pair mode, TX-30) reads one game's log, '' every log of the target."""
		return t.log.since(o, side) if side else t.log.since(o)
	t0 = time.time()
	drv = t if isinstance(t, DriverClient) else getattr(t, "cli", None)
	if isinstance(drv, DriverClient):
		drv.row = row["id"]
		drv.judge = os.path.join(INST, "test_captures/judgement", "qrun_" + os.path.basename(os.path.normpath(res)))
	perf_mark(row["id"])
	# a waitlog whose pattern an earlier waitlog of this row already matched waits for one more matching line, not the
	# old one (judge-14, r1118 SQ-EF-01: the second "waitlog Task finished" hit the first Chunky task's line at once)
	wl_seen = {}
	pre, edits = [], []   # restart (A2): the segment before each restart; edit lines queued for the next restart
	for word, arg, pace in row["steps"]:
		watch_check(t)
		side, _, word = word.rpartition(":")
		if word in ("cmd", "shell", "waitlog", "edit"):
			arg = sub(arg)
			if unset(word, arg):
				continue
		if word in ("capture", "waitlog") and not rx_ok(word, arg.partition(" ")[2] if word == "capture"
														  else arg.rsplit(" ", 1)[0] if re.search(r" \d+$", arg)
														  else arg):
			continue
		if word == "capture":
			name, _, pat = arg.partition(" ")
			hit = [m for m in (re.search(pat, l) for l in since(off, side)) if m]
			if not hit:
				chk("capture", arg, False, f"capture {name}: no line matches /{pat}/")
			else:
				g = hit[-1].groups() or (hit[-1].group(0),)
				VARS[name] = g[0]
				for i, v in enumerate(g, 1):
					VARS[f"{name}{i}"] = v
				chk("capture", arg, True, f"{name} = {' '.join(map(str, g))}")
		elif word == "cmd":
			if re.match(r"/?stop\s*$", arg) and getattr(t, "watch", None) is not None:
				t.watch.stopping = True
			if side:
				t.cmd(arg, side)
			else:
				t.cmd(arg)
			time.sleep(pace * getattr(t, "pace_scale", 1))
		elif word == "edit":
			edits.append(arg)
		elif word == "restart":
			if side or not hasattr(t, "restart"):
				chk("restart", arg, False, "restart needs a server target (qrun.py run)", harness=True)
				continue
			secs = float(arg) if re.fullmatch(r"\d+(\.\d+)?", arg or "") else 600.0
			os.makedirs(os.path.join(res, "rows"), exist_ok=True)
			pre += t.log.since(off)
			ok, detail, ran = t.restart(edits, secs, os.path.join(res, "rows", fsid(row["id"]) + ".shell.txt"))
			for c, rc, outp in ran:
				chk("edit", c, not rc, f"edit exit {rc}: {outp.strip()[:200]}" if rc else "exit 0", harness=bool(rc))
			edits = []
			chk("restart", arg, ok, detail)
			off, wl_seen = 0, {}
			if not ok:
				t.broken = detail
				break
		elif word == "shell":
			r = subprocess.run(["bash", "-c", arg], cwd=t.side_dir(side) if side else t.dir, capture_output=True,
							   text=True,
							   env=dict(os.environ, INST=INST))
			os.makedirs(os.path.join(res, "rows"), exist_ok=True)
			with open(os.path.join(res, "rows", fsid(row["id"]) + ".shell.txt"), "a") as so:
				so.write(f"$ {arg}\n{r.stdout}{r.stderr}\n")
			chk("shell", arg, not r.returncode,
				f"shell exit {r.returncode}: {(r.stderr or r.stdout).strip()[:200]}" if r.returncode else "exit 0")
		elif word == "sleep":
			# crash watch every second, as in waitlog (r1116 SQ-TXQ-02: a kill during "sleep 60" ended the row only
			# after the full sleep)
			end = time.time() + float(arg)
			while True:
				left = end - time.time()
				if left <= 0:
					break
				time.sleep(min(1.0, left))
				if left > 1.0:
					watch_check(t)
		elif word == "waitlog":
			parts = arg.rsplit(" ", 1)
			pat, secs = (parts[0], float(parts[1])) if len(parts) == 2 and parts[1].isdigit() else (arg, 120.0)
			rx, end, ok = re.compile(pat), time.time() + secs, True
			need = wl_seen.get((side, pat), 0)
			while (got := sum(1 for l in since(off, side) if rx.search(l))) <= need:
				if time.time() > end:
					ok = False
					break
				watch_check(t)
				time.sleep(1)
			if ok:
				wl_seen[(side, pat)] = got
			chk("waitlog", arg, ok, (f"timeout {int(secs)} s waiting for /{pat}/" + (f" (a new line: {need} matched before)"
										if need else "")) if not ok else "matched")
		elif word == "look":
			looks.append(arg)
		elif word == "shot":
			p = os.path.join(res, "shots", f"{row['id']}_{arg}.png")
			os.makedirs(os.path.dirname(p), exist_ok=True)
			got = t.shot(p)
			if got:
				shots.append(got if isinstance(got, str) else os.path.relpath(p, res))
		elif word == "key":
			t.key(arg)
			time.sleep(1 * getattr(t, "pace_scale", 1))
		elif word == "click":
			x, y = arg.split()
			t.click(x, y)
			time.sleep(1 * getattr(t, "pace_scale", 1))
		elif word == "do":
			if not hasattr(t, "do"):
				chk("do", arg, False, "do needs the capture driver (qrun client on run_captures.py serve)")
			else:
				t.row = row["id"]
				err = t.do(sub(arg))
				chk("do", arg, not err, err or "ok")
	time.sleep(float(os.environ.get("QRUN_SETTLE", "2")))   # log flush before the checks; the offline tests use less
	watch_check(t)
	segs, wholes = {"": pre + t.log.since(off)}, {}
	for word, arg, _ in row["steps"]:
		side, _, word = word.rpartition(":")
		if side not in segs:
			segs[side] = since(off, side)
		seg = segs[side]
		if word in ("expect", "reject", "bootexpect", "bootreject", "kjsexpect", "kjsreject") and not rx_ok(word, arg):
			continue
		if word == "count" and re.match(r"(.+)\s+(==|>=|<=|>|<)\s+(\d+)$", arg) and not rx_ok(
				word, re.match(r"(.+)\s+(==|>=|<=|>|<)\s+(\d+)$", arg).group(1)):
			continue
		if word in ("expect", "reject"):
			hit = [l for l in seg if re.search(arg, l)]
			if word == "expect":
				chk(word, arg, bool(hit), hit[0].strip()[:200] if hit else f"no line matches /{arg}/")
			else:
				chk(word, arg, not hit, f"/{arg}/ matched: {hit[0].strip()[:200]}" if hit else "no match")
		elif word == "count":
			m = re.match(r"(.+)\s+(==|>=|<=|>|<)\s+(\d+)$", arg)
			if not m:
				chk(word, arg, False, f"bad count directive: {arg}")
				continue
			n = sum(1 for l in seg if re.search(m.group(1), l))
			ok = compare(n, m.group(2), int(m.group(3)))
			chk(word, arg, ok, f"count /{m.group(1)}/ = {n}" + ("" if ok else f", wanted {m.group(2)} {m.group(3)}"))
		elif word in ("bootexpect", "bootreject", "kjsexpect", "kjsreject"):
			kjs = word.startswith("kjs")
			if kjs:
				src = t.kjs.since(0, side) if side else t.kjs.since(0)
			else:
				if side not in wholes:
					wholes[side] = since(0, side)
				src = wholes[side]
			name = "KubeJS log" if kjs else "boot log"
			hit = [l for l in src if re.search(arg, l)]
			if word.endswith("expect"):
				chk(word, arg, bool(hit), hit[0].strip()[:200] if hit else f"{name}: no line matches /{arg}/")
			else:
				chk(word, arg, not hit, f"{name}: /{arg}/ matched: {hit[0].strip()[:200]}" if hit else "no match")
	os.makedirs(os.path.join(res, "rows"), exist_ok=True)
	with open(os.path.join(res, "rows", fsid(row["id"]) + ".log"), "w") as f:
		f.write("\n".join(segs[""]) + "\n")
	with open(os.path.join(res, "timeline.tsv"), "a") as f:
		f.write(f"{t0:.1f}\t{time.time():.1f}\t{t.kind}\t{row['id']}\n")
	# HARNESS (P2.4): only harness faults failed, or the only failure is a shell exit code while every log check passed
	# (e.g. grep -c printing 0 exits 1); the row is then a batch fault for the runner to fix, not a game FAIL
	bad = [c for c in checks if not c["ok"]]
	if any(c.get("harness") for c in bad):
		# a harness fault (bad regex, unset capture) leaves the row unjudged: its other failed checks follow from it
		result = "HARNESS"
	elif bad and all(c["step"] == "shell" for c in bad) and any(
			c["ok"] and c["step"] in ("expect", "count", "waitlog", "bootexpect", "kjsexpect") for c in checks):
		for c in bad:
			c["harness"] = True
		result = "HARNESS"
	else:
		result = "FAIL" if fails else ("LOOK" if looks or shots else "PASS")
	return {"result": result, "fails": fails, "looks": looks, "shots": shots, "title": row["title"],
			"at": time.strftime("%Y-%m-%d %H:%M:%S"), "lines": len(segs[""]), "started": t0, "ended": time.time()}


def load_vars(res):
	vp = os.path.join(res, "vars.json")
	return json.load(open(vp)) if os.path.exists(vp) else {}


def make_target(mode, target):
	"""The transport for a batch: Server (fifo) or Client. Client-mode transport hook: a new client driver (the
    in-process capture driver channel, TX-02) replaces Client here; it needs .dir, .log, .kjs, .kind and cmd / shot /
    key / click. Called again after a crash relaunch, so it must attach to the new JVM."""
	if mode == "server":
		return Server(target)
	if not XDOTOOL and (driver_serving(target) or os.path.isdir(os.path.join(target, "cdev_channel"))):
		return DriverClient(target)  # a stale channel exits with a message instead of typing into some window
	return Client(target)


def write_results(res, results):
	with open(os.path.join(res, "results.json"), "w") as f:
		json.dump(results, f, indent=1)
	with open(os.path.join(res, "results.md"), "w") as f:
		f.write("| ID | Result | Detail |\n|---|---|---|\n")
		for rid, r in results.items():
			d = "; ".join(r["fails"] + ["look: " + x for x in r["looks"]] + ["shot: " + x for x in r["shots"]])
			f.write(f"| {rid} | {r['result']} | {d.replace('|', '/')} |\n")


def main():
	global XDOTOOL
	a = sys.argv[1:]
	if not a:
		sys.exit(__doc__)
	XDOTOOL = "--xdotool" in a
	if a[0] in ("lint", "dry"):
		import qlint
		sys.exit((qlint.main_lint if a[0] == "lint" else qlint.main_dry)(a[1:]))
	if a[0] in ("judge", "table"):
		import qverdict
		sys.exit((qverdict.main_judge if a[0] == "judge" else qverdict.main_table)(a[1:]))
	if a[0] == "toml":
		path, section, key, value = a[1:5]
		lines = open(path).read().splitlines()
		if section in ("", "-"):
			# a top-level key (before the first [section]; r1118 follow-up, generators-and-tests #25)
			first = next((i for i, l in enumerate(lines) if re.match(r"\s*\[[^\]]+\]\s*$", l)), len(lines))
			hit = next((i for i in range(first) if re.match(rf"\s*{re.escape(key)}\s*=", lines[i])), None)
			if hit is not None:
				lines[hit] = re.sub(r"=.*", "= " + value, lines[hit], count=1)
			else:
				lines[first:first] = [f"{key} = {value}"] + ([""] if first < len(lines) else [])
			open(path, "w").write("\n".join(lines) + "\n")
			print(f"{path}: (top level) {key} = {value}")
			return
		sec, done, out, at_end = None, False, [], None
		for i, l in enumerate(lines):
			m = re.match(r"\s*\[([^\]]+)\]\s*$", l)
			if m:
				if sec == section and not done:
					out.append(f"\t{key} = {value}")
					done = True
				sec = m.group(1).strip()
			elif sec == section and re.match(rf"\s*{re.escape(key)}\s*=", l) and not done:
				out.append(re.sub(r"=.*", "= " + value, l, count=1))
				done = True
				continue
			out.append(l)
		if not done:
			if sec != section:
				out.append(f"\n[{section}]")
			out.append(f"\t{key} = {value}")
		open(path, "w").write("\n".join(out) + "\n")
		print(f"{path}: [{section}] {key} = {value}")
		return
	if a[0] == "list":
		for r in parse(a[1]):
			kinds = sorted({w for w, _, _ in r["steps"]})
			print(f"{r['id']:<16} {r['title'][:70]:<70} {','.join(kinds)}")
		return
	if a[0] == "drive":
		import qdrive
		sys.exit(qdrive.main(a[1:]))
	if a[0] == "prep":
		import qdrive
		sys.exit(qdrive.main_prep(a[1:]))
	if a[0] in ("plan", "playtest", "states", "archive", "never"):
		import qplan
		sys.exit(qplan.main(a))
	if a[0] in ("pair", "matrix"):
		import qpair
		sys.exit((qpair.main_pair if a[0] == "pair" else qpair.main_matrix)(a[1:]))
	if a[0] not in ("run", "client") or len(a) < 4:
		sys.exit(__doc__)
	run_batch(a, "server" if a[0] == "run" else "client")


def probe_files():
	"""Scratch KubeJS probe files: tools/**/*_probe.js, then every other tools/**/*.js that registers a server command
    (ServerEvents / onEvent plus `register(X.literal('root')`), e.g. tools/fungi_probe/ztrades.js, zcount.js, zgoose.js
    (r1118 judge-11 / judge-9, 2026-11-18). *_probe.js files come first, so their roots win a clash."""
	import glob
	probes = sorted(glob.glob(os.path.join(INST, "tools/**/*_probe.js"), recursive=True))
	extra = []
	for js in sorted(glob.glob(os.path.join(INST, "tools/**/*.js"), recursive=True)):
		if js in probes or "node_modules" in js:
			continue
		try:
			s = open(js, encoding="utf-8", errors="replace").read()
		except OSError:
			continue
		if re.search(r"ServerEvents\.|onEvent\(", s) and REG_ROOT.search(s):
			extra.append(js)
	return probes + extra


# a command root: the first literal of a register(...) call (sub-literals like 'check' or 'setup' are not roots)
REG_ROOT = re.compile(r"""register\(\s*\w+\.literal\(\s*['"](\w+)['"]""")


def probe_map():
	"""command root -> scratch KubeJS probe file (probe_files(); in *_probe.js every '.literal('root')', in the other
    files only the first literal of each register(...) call). Roots that more than one probe registers map to the first
    in probe_files() order."""
	m = {}
	for js in probe_files():
		try:
			s = open(js, encoding="utf-8", errors="replace").read()
		except OSError:
			continue
		pat = r"""\.literal\(\s*['"](\w+)['"]""" if js.endswith("_probe.js") else REG_ROOT
		for root in re.findall(pat, s):
			m.setdefault(root, js)
	return m


def probe_tags(files):
	"""probe file -> the '[tag]' log prefixes it prints (console.log / logger strings starting with '[word]')."""
	m = {}
	for js in files:
		try:
			src = open(js, encoding="utf-8", errors="replace").read()
		except OSError:
			continue
		m[js] = set(re.findall(r"""['"`]\[([A-Za-z0-9_-]+)\]""", src))
	return m


def probes_needed(rows):
	"""(probe files, roots) the rows need. A probe is needed when (1) a '> root ...' command uses a root only it
    registers (roots a jar registers via literal("root") in development/ Java are dropped), (2) a step or the row's
    @commands names its file (tools/**/NAME_probe.js), or (3) a log check (expect, count,
    waitlog, kjsexpect, ...) looks for a '[tag]' prefix that only that probe prints. (2) and (3) cover probes that only
    print at load or reload and register no command (cc_dict_probe.js, nch_guard_probe.js; r1118 judge-4, judge-5)."""
	import glob
	pm = probe_map()
	allp = probe_files()
	byname = {}
	for js in allp:
		byname.setdefault(os.path.basename(js), js)
	roots, named = set(), set()
	files = {}  # probe file -> the '[tag]'s that matched a log check
	tagmap = None
	for r in rows:
		# section @@text is left out: it carries run-log history that names retired probes (SALT names 27)
		texts = [arg for _, arg, _ in r["steps"]] + [r.get("meta", {}).get("commands", "")]
		for w, arg, _ in r["steps"]:
			if w in (">", "cmd"):
				m = re.match(r"(?:execute .*? run )?(\w+)", arg)
				if m and m.group(1) in pm:
					roots.add(m.group(1))
		for txt in texts:
			for m in re.finditer(r"(?:zz_)?(\w+_probe\.js|\b\w+\.js)", txt):
				# "zq_probe.js is retired", "(zq_probe.js End climate dump retired)": a mention, not a use
				near = txt[m.end():m.end() + 60].split(";")[0].split(")")[0]
				if m.group(1) in byname and not re.search(r"retired|not used|no longer", near):
					named.add(byname[m.group(1)])
		checks = [arg.replace("\\", "") for w, arg, _ in r["steps"]
				  if w in ("expect", "reject", "count", "waitlog", "bootexpect", "bootreject", "kjsexpect", "kjsreject")]
		if checks:
			if tagmap is None:
				tagmap = probe_tags(allp)
			for js, tags in tagmap.items():
				hit = {tg for tg in tags for c in checks if "[" + tg + "]" in c}
				if hit:
					files.setdefault(js, set()).update(hit)
	if roots:
		java = subprocess.run(["grep", "-rhoE", r'literal\("(' + "|".join(sorted(roots)) + r')"', os.path.join(INST, "development"),
							   "--include=*.java"], capture_output=True, text=True).stdout
		roots -= set(re.findall(r'"(\w+)"', java))
	# a '[tag]' that another probe or our own code (jars, pack scripts) also prints is no proof this probe is needed:
	# a tag-only match stands only when every matched tag is this probe's alone (named files and roots always stand)
	keep = set()
	for js, hit in sorted(files.items()):
		if js in named:
			continue
		if any(tg in tagmap[o] for o in tagmap if o != js for tg in hit):
			continue
		own = False
		for tg in hit:
			out = subprocess.run(["grep", "-rlF", "--include=*.java", "--include=*.js", "--exclude-dir=exported",
								  "--exclude-dir=build", "[" + tg + "]", os.path.join(INST, "development"),
								  os.path.join(INST, "kubejs")], capture_output=True, text=True).stdout
			if out.strip():
				own = True
				break
		if not own:
			keep.add(js)
	# a root that a named file (or one kept for its own log tag) registers comes from that file, not from probe_map's first registrant (RD names
	# zcount.js for `/zcount BOX`; leaf_decay_probe.js also registers an argless zcount and a tick handler)
	def own_roots(js):
		try:
			src = open(js, encoding="utf-8", errors="replace").read()
		except OSError:
			return set()
		return set(re.findall(r"""\.literal\(\s*['"](\w+)['"]""" if js.endswith("_probe.js") else REG_ROOT, src))
	by_named = set().union(*[own_roots(js) for js in named | keep]) if (named or keep) else set()
	return keep | named | {pm[x] for x in roots if x not in by_named}, roots


def install_probes(t, rows):
	"""Server mode: copy the KubeJS probes the rows need (probes_needed: command roots, named probe files, probe-only
    log tags) into srv/kubejs/server_scripts as zz_<file> and /reload once. Section texts used to ask for this by hand
    before boot, which the driver never did (r1118 judge-1). A probe already in place (an @@prep copy) is left.
    --no-probes turns it off."""
	need, roots = probes_needed(rows)
	if not need:
		return
	sdir = os.path.join(t.dir, "kubejs/server_scripts")
	new = []
	for f in sorted(need):
		dst = os.path.join(sdir, "zz_" + os.path.basename(f))
		if os.path.isdir(sdir) and not os.path.exists(dst):
			import shutil
			shutil.copyfile(f, dst)
			new.append(os.path.basename(f))
	if not new:
		return
	why = f" for /{', /'.join(sorted(roots))}" if roots else ""
	print(f"probes: copied {', '.join(new)}{why}; reload", flush=True)
	# The "Loaded N/M KubeJS server scripts" line goes to latest.log (KubeJS Server logger), not to
	# logs/kubejs/server.log, which KubeJS also rewrites on reload (an old offset can sit past its end):
	# watch latest.log from its pre-reload offset (judge-2, r1118).
	off = t.log.size()
	t.cmd("reload")
	# The reload goes on about 10 s past "Loaded N/M" (recipes, tags, datapack listeners); its last line is KubeJS's
	# "Reloaded with no KubeJS errors!" or "KubeJS errors found [N]!" (MinecraftServer logger). Wait for that, 180 s
	# cap; once "Loaded" is seen, give the end line at most 60 s (hideServerScriptErrors can suppress it) (judge-7).
	end = time.time() + 180
	pat = re.compile(r"Loaded \d+/\d+ KubeJS server scripts")
	fin = re.compile(r"Reloaded with no KubeJS errors!|KubeJS errors found \[")
	loaded = None
	while time.time() < end:
		lines = t.log.since(off)
		if any(fin.search(x) for x in lines):
			break
		if loaded is None and any(pat.search(x) for x in lines):
			loaded = time.time()
			end = min(end, loaded + 60)
		time.sleep(1)
	else:
		if loaded is None:
			print("probes: no 'Loaded ... KubeJS server scripts' in 180 s after the reload", flush=True)
		else:
			print("probes: 'Loaded ... KubeJS server scripts' seen, no reload end line within 60 s", flush=True)
	time.sleep(2)


def run_batch(a, mode, t=None):
	"""run / client (a = [verb, TARGET, BATCH, RES, options...]); pair mode (qpair.py) passes its own target t with
    its watch set. Returns the results dict."""
	target, batch, res = a[1], a[2], a[3]
	opt = lambda k, d=None: a[a.index(k) + 1] if k in a else d  # noqa: E731
	only = set(opt("--rows").split(",")) if "--rows" in a else None
	start = opt("--from")
	sections = set(opt("--section").split(",")) if "--section" in a else None
	tiers = set(opt("--tier").split(",")) if "--tier" in a else None
	tier_of = {}
	if tiers:
		import qrows
		tier_of = qrows.tiers_of(batch)
	os.makedirs(res, exist_ok=True)
	if "--no-lint" not in a:
		import qlint
		perr = []
		allrows = parse(batch, errors=perr)
		fs = qlint.lint_rows(qlint.select(allrows, only, start), allrows, mode != "server", load_vars(res), None, perr)
		for x in fs:
			if x[0] == "E":
				print("lint:", qlint.fmt(batch, x))
		if fs:
			print(f"lint: {sum(1 for x in fs if x[0] == 'E')} E, {sum(1 for x in fs if x[0] == 'W')} W "
				  f"(qrun.py lint {batch} for the warnings); running anyway")
	stamps = None
	if mode == "server" and os.path.isdir(os.path.join(target, "srv")):
		# pinned jars (qpin.py, P2.4): refuse a server whose own jars are links, changed since prep, or unlisted
		import qpin
		probs = qpin.verify(target)
		if probs and "--no-pin" not in a:
			for p in probs:
				print("REFUSE", p)
			sys.exit("qrun: not starting (pinned-jar check); --no-pin overrides and is recorded in the verdicts")
		stamps = qpin.stamps(target)
		if probs:
			stamps = dict(stamps or {}, _no_pin="; ".join(probs)[:500])
	if t is None:
		t = make_target(mode, target)
		t.watch = None
	if "--no-watch" not in a and t.watch is None:
		import qwatch
		pids = [int(x) for x in opt("--pid").split(",")] if "--pid" in a else None
		t.watch = qwatch.Watch(mode, t.dir, pids)
	relaunch = opt("--relaunch")
	ready = opt("--ready", r"Done \(" if mode == "server" else r"joined the game")
	ready_secs = float(opt("--ready-secs", "600"))
	max_relaunch, relaunched = int(opt("--max-relaunch", "2")), 0
	rp = os.path.join(res, "results.json")
	results = json.load(open(rp)) if os.path.exists(rp) else {}
	vp = os.path.join(res, "vars.json")
	VARS.update(load_vars(res))
	started = start is None
	crashdir = os.path.join(target, "srv", "crash-reports") if a[0] == "run" and t.watch is None else None
	crashes0 = set(os.listdir(crashdir)) if crashdir and os.path.isdir(crashdir) else set()
	allrows = parse(batch)
	CAPTURES.update(arg.split(" ", 1)[0] for r in allrows for w, arg, _ in r["steps"] if w.endswith("capture"))
	if mode == "server" and a[0] == "run" and "--no-probes" not in a:
		sel = [r for r in allrows if (not only or r["id"] in only) and (not sections or r["section"] in sections)]
		try:
			install_probes(t, sel)
		except (OSError, RuntimeError) as e:
			print(f"probes: not installed ({e})", flush=True)
	for row in allrows:
		if crashdir and os.path.isdir(crashdir) and set(os.listdir(crashdir)) - crashes0:
			# the server died: later rows would only time out (r1102b 19:03, a probe crashed the tick loop)
			print(f"ABORT: server crash report {sorted(set(os.listdir(crashdir)) - crashes0)} before {row['id']}")
			break
		if not started:
			started = row["id"] == start
			if not started:
				continue
		if only and row["id"] not in only:
			continue
		if sections and row["section"] not in sections:
			continue
		if tiers and tier_of.get(row["id"], "T2") not in tiers:
			continue
		if row["meta"].get("superseded"):
			# marked after a plan was made (drive steps keep their row lists): listed, never run
			print(f"{row['id']} SUPERSEDED ({row['meta']['superseded'][:80]}); not run")
			continue
		if not row["steps"]:
			# a queue row with no steps yet (@draft): the runner writes them into the .qb first
			print(f"{row['id']} DRAFT (no steps: {row['meta'].get('draft', 'empty row')}); not run")
			continue
		print(f"{row['id']} ...", end=" ", flush=True)
		checks, t0, crash = [], time.time(), None
		try:
			r = run_row(t, row, res, checks)
		except RuntimeError as e:
			print(f"ABORT: {e}")
			break
		except Exception as e:  # qwatch.Crashed (imported lazily)
			if type(e).__name__ != "Crashed":
				raise
			if e.stopped:
				print(f"ABORT: {e.reason}")
				break
			edir, got = t.watch.collect(res, fsid(row["id"]))
			crash = {"reason": e.reason, "evidence_dir": edir, "files": got}
			r = {"result": "CRASH", "fails": [e.reason], "looks": [], "shots": [], "title": row["title"],
				 "at": time.strftime("%Y-%m-%d %H:%M:%S"), "lines": 0, "started": t0, "ended": time.time()}
		results[row["id"]] = r
		write_results(res, results)
		import qverdict
		qverdict.write(res, row, r, checks, batch, mode, crash, stamps)
		with open(vp, "w") as f:
			json.dump(VARS, f, indent=1)
		print(r["result"], "; ".join(r["fails"])[:200])
		if getattr(t, "broken", None):
			print(f"ABORT: restart failed ({t.broken})")
			break
		if crash:
			print(f"  evidence: {os.path.join(res, crash['evidence_dir'])}")
			if not relaunch or relaunched >= max_relaunch:
				print("ABORT: crash" + (f" (relaunch limit {max_relaunch})" if relaunch else "; no --relaunch given"))
				break
			if not t.watch.rearm():
				print("ABORT: crashed JVM still up after 60 s; not killed (only its owner may kill it)")
				break
			relaunched += 1
			ok, msg = t.watch.relaunch(relaunch, ready, ready_secs, t.log.path,
									   os.path.join(res, "crash", f"relaunch_{relaunched}.out"))
			print(f"  relaunch {relaunched}: {msg}")
			if not ok:
				break
			w = t.watch
			t = make_target(mode, target)
			t.watch = w
	write_results(res, results)
	n = {}
	for r in results.values():
		n[r["result"]] = n.get(r["result"], 0) + 1
	print("totals:", ", ".join(f"{k} {v}" for k, v in sorted(n.items())), "->", os.path.join(res, "results.md"))
	return results


if __name__ == "__main__":
	sys.modules.setdefault("qrun", sys.modules[__name__])  # helper modules import qrun: one copy, one VARS
	main()

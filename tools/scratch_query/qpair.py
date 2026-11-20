#!/usr/bin/env python3
"""Paired client + own scratch server runs (TX-30) and the registry / config mismatch matrix (TX-31). Called through
qrun.py:

    qrun.py pair OUT GAME BATCH RES [--jar PATH]... [--drop GLOB]... [--config FILE:SECTION:KEY=VALUE]... [--default srv|cli] [--join-secs N] [run options]
    qrun.py matrix SPEC RES [--only ID,ID] [--plan]

Owner decision D2 (plan_scratch_testing_expansion.md 0): a paired launch is valid when ONE driver starts and stops
both JVMs for a single scratch test; that driver is then the only game-JVM owner. pair and matrix are that driver:
they refuse to start while any game JVM is up, boot the scratch server in OUT (already built by prep_srv.sh), start
the client with the capture driver joined to it (run_captures.py serve --join 127.0.0.1:PORT --ownsrv SRVPID), run
the batch, then stop the client (driver "stop", then its own pid only) and the server (stop_wait.sh), and check that
neither pid is left. They never kill a JVM they did not start.

pair batch lines (qrun format plus sides): 'srv> CMD' runs on the server console, 'cli> CMD' as the client player
through the driver, '> CMD' on the --default side (srv). Log directives take a side prefix: 'srv:expect', 'cli:waitlog',
'cli:capture', 'srv:bootreject', 'cli:shell' (cwd GAME); without one they read both logs (server lines, then client
lines). key / click / shot / do go to the client. Crash watch covers both JVMs; a crash ends the batch (no relaunch).

matrix SPEC (JSON):
    {"prep": "NOSWAP=1 bash $INST/tools/scratch_query/prep_srv.sh {OUT} txp A",      default shown
     "out": "<scratch dir for OUT>", "game": "<client game dir>",                   defaults under the scratchpad / ~/.cache
     "cases": [{"id": "SQ-TXP-M01", "title": "...",
                "srv_env": {"PGJAR": "jar_archive/..."},     env for prep (prep_srv.sh options)
                "srv_drop": ["backportedPowerGrid-*.jar"],     globs removed from OUT/srv/mods (unlink only)
                "srv_toml": [["config/x-common.toml", "section", "key", "value"]],
                "cli_jars": ["jar_archive/pwg_gate/x.jar"],   added to the client (run_captures serve --jar)
                "cli_drop": ["creativelabels-*.jar"],          globs removed from GAME/mods (serve --drop)
                "cli_config": [["config/x-client.toml", "section", "key", "value"]],   (serve --config)
                "hold_secs": 30,                               after JOINED, watch both JVMs this long (spec or case)
                "expect": "refuse" | "join",
                "refusal": "regex the refusal text must match (optional)",
                "batch": "path.qb run in pair mode when the client joined (optional)"}]}
Outcome per case: JOINED (driver reached SERVE and both JVMs lived through hold_secs, default 30: a client that
joins and then crashes, like the Power Grid client without the server jar, TODO #90, is CRASH-CLIENT), REFUSED (refusal text in either log, client alive), CRASH-CLIENT,
CRASH-SERVER, BOOT-FAIL (server never Done), HANG (none within --join-secs, default 600). Verdict PASS when the outcome
is the expected one (a refusal also matching "refusal"), else FAIL; a crash is always FAIL. Results: RES/<case>/ (logs,
crash evidence, pair batch results), RES/verdicts/<case>.json, RES/matrix.md. --plan prints the cases and commands
and launches nothing.

Refusal text is Forge 1.19.2-43.3.9 (HandshakeHandler, NetworkRegistry, assets/forge/lang/en_us.json; checked in the
Forge jar 2026-11-16) plus the registry refusal seen in run r1111 (SQ-CDNS-12). Screen texts only reach the log when
the driver or a mod logs the disconnect screen; the log lines are the reliable part.

Commands are overridable for offline tests: env QPAIR_SRV_BOOT, QPAIR_SRV_STOP, QPAIR_CLI_LAUNCH (placeholders {OUT},
{GAME}, {PORT}, {SRVPID}, {JARARGS}).
"""
import glob
import json
import os
import re
import shlex
import signal
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
INST = os.path.abspath(os.path.join(HERE, "../.."))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(INST, "tools/client_capture"))
import qrun  # noqa: E402
import qwatch  # noqa: E402

SRV_BOOT = 'bash "$INST/tools/creative_labels_test/scratch_server.sh" boot {OUT}'
SRV_STOP = 'bash "$INST/tools/scratch_query/stop_wait.sh" {OUT}'
CLI_LAUNCH = ('python3 "$INST/tools/client_capture/run_captures.py" serve --game {GAME} --join 127.0.0.1:{PORT} '
			  '--ownsrv {SRVPID} {JARARGS}')
PREP = 'NOSWAP=1 bash "$INST/tools/scratch_query/prep_srv.sh" {OUT} txp A'
GAME_DEFAULT = os.path.expanduser("~/.cache/ce_capture/game")
# refusal text: Forge 1.19.2-43.3.9 strings (see the docstring); client log first, then the disconnect screen texts
CLI_REFUSAL = (r"(Terminating connection with server, mismatched mod list|Connection closed - mismatched mod channel list|"
			   r"Failed to synchronize registry data from server|Missing required datapack registr|"
			   r"Incompatible FML modded server|Incompatible Vanilla server|Server mod list is not compatible|"
			   r"Server network message list is not compatible|Your client is missing the following mods|"
			   r"The server is missing the following mods|The following mod versions do not match)")
SRV_REFUSAL = (r"(Terminating connection with client, mismatched mod list|Connection closed - mismatched mod channel list|"
			   r"Missing required datapack registr)")
SRV_FAIL = r"Failed to start the minecraft server|Failed to load datapacks|Missing or unsupported mandatory dependencies"


def sh(cmd, env=None, timeout=None, **_):
	"""bash cmd in the instance; output through a temp file, never a pipe (a boot script's background children,
    the fifo feeder, would hold a pipe open forever)."""
	import tempfile
	with tempfile.TemporaryFile("w+") as f:
		p = subprocess.run(["bash", "-c", cmd], cwd=INST, env=env or dict(os.environ, INST=INST), stdout=f,
						   stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL, timeout=timeout)
		f.seek(0)
		out = f.read()
	return subprocess.CompletedProcess(p.args, p.returncode, out, "")


def fill(cmd, **v):
	for k, x in v.items():
		cmd = cmd.replace("{" + k + "}", str(x))
	return cmd


def game_jvms():
	return subprocess.run(["bash", os.path.join(HERE, "game_jvms.sh")], capture_output=True, text=True).stdout.strip()


class MultiLog:
	"""Both games' logs as one: offsets are a dict per side; since(off) is server lines then client lines."""

	def __init__(self, **logs):
		self.logs = logs
		self.path = logs["srv"].path

	def size(self):
		return {k: l.size() for k, l in self.logs.items()}

	def since(self, off, side=None):
		o = (lambda k: off[k] if isinstance(off, dict) else off)
		if side:
			return self.logs[side].since(o(side))
		out = []
		for k, l in self.logs.items():
			out += l.since(o(k))
		return out


class MultiWatch:
	def __init__(self, **w):
		self.w = w

	@property
	def stopping(self):
		return any(x.stopping for x in self.w.values())

	@stopping.setter
	def stopping(self, v):
		self.w["srv"].stopping = v  # only the server is stopped by a batch line; the client then sees a disconnect

	def check(self):
		for x in self.w.values():
			x.check()

	def collect(self, res, rid):
		got = []
		for k, x in self.w.items():
			_, g = x.collect(res, os.path.join(rid, k))
			got += [f"{k}/{f}" for f in g]
		return os.path.join("crash", rid.replace("/", "_")), got


class PairTarget:
	"""qrun target for pair mode: server fifo + capture driver client."""
	kind = "pair"
	pace_scale = 1

	def __init__(self, out, game, default="srv"):
		self.srv = qrun.Server(out)
		self.cli = qrun.DriverClient(game)
		self.default = default
		self.dir = self.srv.dir
		self.log = MultiLog(srv=self.srv.log, cli=self.cli.log)
		self.kjs = MultiLog(srv=self.srv.kjs, cli=self.cli.kjs)
		self.watch = None
		self.row = "qrun"

	def side_dir(self, side):
		return self.srv.dir if side == "srv" else self.cli.dir

	def cmd(self, c, side=None):
		(self.srv if (side or self.default) == "srv" else self.cli).cmd(c)

	def do(self, arg):
		return self.cli.do(arg)

	def shot(self, path):
		return self.cli.shot(path)

	def key(self, k):
		self.cli.key(k)

	def click(self, x, y):
		self.cli.click(x, y)


class Pair:
	"""Lifecycle of one paired scratch test: boot server, launch client, wait for the join, stop both."""

	def __init__(self, out, game, res, jars=(), log=print, drop=(), config=()):
		self.out, self.game, self.res = os.path.abspath(out), os.path.abspath(game), res
		self.srv_dir = os.path.join(self.out, "srv")
		self.jars, self.log = list(jars), log
		self.drop, self.config = list(drop), [list(c) for c in config]
		self.launched = None
		self.srv_pid = self.cli_pid = None
		self.serve = None
		self.wsrv = self.wcli = None
		os.makedirs(res, exist_ok=True)

	def port(self):
		try:
			for l in open(os.path.join(self.srv_dir, "server.properties")):
				if l.startswith("server-port="):
					return int(l.split("=", 1)[1])
		except OSError:
			pass
		return 25565

	def boot_server(self, secs=900):
		"""Returns (ok, outcome detail)."""
		live = game_jvms()
		if live:
			return False, "a game JVM is up (one-JVM rule): " + " ".join(l.split()[0] for l in live.splitlines())
		files0 = qwatch.crash_files(self.srv_dir)
		r = sh(fill(os.environ.get("QPAIR_SRV_BOOT", SRV_BOOT), OUT=shlex.quote(self.out)), capture_output=True,
			   text=True, stdin=subprocess.DEVNULL)
		if r.returncode:
			return False, f"server boot exit {r.returncode}: {(r.stderr or r.stdout).strip()[:200]}"
		lg, end = os.path.join(self.srv_dir, "logs/latest.log"), time.time() + secs
		while time.time() < end:
			if not self.srv_pid:
				p = qwatch.jvm_pids(self.srv_dir)
				self.srv_pid = p[0] if p else None
			txt = open(lg, errors="replace").read() if os.path.exists(lg) else ""
			if re.search(r"Done \(", txt):
				self.wsrv = qwatch.Watch("server", self.srv_dir, [self.srv_pid] if self.srv_pid else None, self.log)
				self.wsrv.files0 = files0
				return True, "Done"
			m = re.search(SRV_FAIL, txt)
			if m:
				return False, m.group(0)
			if qwatch.crash_files(self.srv_dir) - files0:
				return False, "server crash report during boot"
			if self.srv_pid and not qwatch.alive(self.srv_pid):
				return False, "server JVM gone during boot"
			time.sleep(2)
		return False, f"no Done in {secs} s"

	def launch_client(self):
		jarargs = " ".join("--jar " + shlex.quote(os.path.join(INST, j) if not os.path.isabs(j) else j) for j in self.jars)
		jarargs += "".join(" --drop " + shlex.quote(g) for g in self.drop)
		jarargs += "".join(" --config " + shlex.quote(f"{f}:{sec}:{k}={v}") for f, sec, k, v in self.config)
		# an earlier launch's pid / heartbeat must not be read as this client's (serve also wipes the dir, but only
		# once its process has started; wait_join polls before that)
		chan_dir = os.path.join(self.game, "cdev_channel")
		for f in ("pid", "state.json"):
			try:
				os.remove(os.path.join(chan_dir, f))
			except OSError:
				pass
		self.cli_pid = None
		cmd = fill(os.environ.get("QPAIR_CLI_LAUNCH", CLI_LAUNCH), GAME=shlex.quote(self.game), PORT=self.port(),
				   SRVPID=self.srv_pid or 0, JARARGS=jarargs, OUT=shlex.quote(self.out))
		self.cli_files0 = qwatch.crash_files(self.game)
		self.cli_off = qrun.Log(os.path.join(self.game, "logs/latest.log")).size()
		self.srv_off = qrun.Log(os.path.join(self.srv_dir, "logs/latest.log")).size()
		self.serve_out = os.path.join(self.res, "client_launch.out")
		self.launched = time.time()
		with open(self.serve_out, "w") as f:
			self.serve = subprocess.Popen(["bash", "-c", cmd], cwd=INST, stdout=f, stderr=subprocess.STDOUT,
										  stdin=subprocess.DEVNULL, env=dict(os.environ, INST=INST))

	def _client_pid(self):
		if self.cli_pid:
			return self.cli_pid
		pf = os.path.join(self.game, "cdev_channel", "pid")
		try:
			if self.launched and os.path.getmtime(pf) < self.launched - 1:
				raise OSError("pid file older than this launch")
			self.cli_pid = int(open(pf).read().strip())
		except (OSError, ValueError):
			m = re.search(r"client pid (\d+)", open(self.serve_out, errors="replace").read())
			self.cli_pid = int(m.group(1)) if m else None
		return self.cli_pid

	def wait_join(self, secs=600, cli_refusal=CLI_REFUSAL, srv_refusal=SRV_REFUSAL):
		"""Returns (outcome, evidence lines)."""
		from chan import Channel
		chan = Channel(os.path.join(self.game, "cdev_channel"))
		cl, sl = qrun.Log(os.path.join(self.game, "logs/latest.log")), qrun.Log(os.path.join(self.srv_dir, "logs/latest.log"))
		end, refused_at = time.time() + secs, None
		ev = []
		while time.time() < end:
			pid = self._client_pid()
			if self.wsrv:
				try:
					self.wsrv.check()
				except qwatch.Crashed as e:
					return "CRASH-SERVER", [e.reason]
			new = sorted(qwatch.crash_files(self.game) - self.cli_files0)
			if new:
				return "CRASH-CLIENT", ["client crash file(s): " + ", ".join(new)]
			if pid and not qwatch.alive(pid):
				return "CRASH-CLIENT", [f"client pid {pid} gone before joining"]
			if self.serve.poll() not in (None, 0) and not pid:
				return "CRASH-CLIENT", ["launch failed: " + open(self.serve_out, errors="replace").read().strip()[-300:]]
			st = chan.state()
			if st and st.get("phase") == "SERVE" and st["age_s"] < 60 and st.get("time", 0) / 1000.0 >= self.launched - 1:
				return "JOINED", ["driver SERVE"]
			hits = [("cli", l) for l in cl.since(self.cli_off) if re.search(cli_refusal, l)]
			hits += [("srv", l) for l in sl.since(self.srv_off) if re.search(srv_refusal, l)]
			if hits:
				ev = [f"{k}: {l.strip()[:300]}" for k, l in hits[:12]]
				if refused_at is None:
					refused_at = time.time()
				elif time.time() - refused_at > 10:  # 10 s more: a refusal that turns into a crash is a crash
					return "REFUSED", ev
			time.sleep(2)
		return "HANG", ev or ["no join, refusal or crash"]

	def hold(self, secs):
		"""After JOINED: both JVMs must live SECS more. Returns (outcome, evidence) or (None, [])."""
		end = time.time() + secs
		while time.time() < end:
			if self.wsrv:
				try:
					self.wsrv.check()
				except qwatch.Crashed as e:
					return "CRASH-SERVER", [e.reason]
			new = sorted(qwatch.crash_files(self.game) - self.cli_files0)
			if new:
				return "CRASH-CLIENT", ["client crash file(s) after joining: " + ", ".join(new)]
			pid = self._client_pid()
			if pid and not qwatch.alive(pid):
				return "CRASH-CLIENT", [f"client pid {pid} gone {int(secs - (end - time.time()))} s after joining"]
			time.sleep(2)
		return None, []

	def target(self, default="srv"):
		t = PairTarget(self.out, self.game, default)
		pid = self._client_pid()
		self.wcli = qwatch.Watch("client", self.game, [pid] if pid else None, self.log)
		self.wcli.files0 = self.cli_files0
		t.watch = MultiWatch(srv=self.wsrv, cli=self.wcli)
		return t

	def stop(self):
		"""Stop own client then own server. Returns a list of problems (empty = both gone)."""
		from chan import Channel
		probs = []
		pid = self._client_pid()
		if pid and qwatch.alive(pid):
			chan = Channel(os.path.join(self.game, "cdev_channel"))
			if chan.serving():
				try:
					chan.op("stop", timeout=30)
				except Exception as e:  # ChannelError: fall through to the pid
					probs.append(f"driver stop: {e}")
			end = time.time() + 60
			while qwatch.alive(pid) and time.time() < end:
				time.sleep(1)
			for sig, wait in ((signal.SIGTERM, 30), (signal.SIGKILL, 10)):
				if not qwatch.alive(pid):
					break
				try:
					os.kill(pid, sig)  # own pid only: this driver launched it (D2)
				except ProcessLookupError:
					break
				end = time.time() + wait
				while qwatch.alive(pid) and time.time() < end:
					time.sleep(1)
		if self.serve and self.serve.poll() is None:
			self.serve.terminate()  # the serve helper (python), not a game JVM
			try:
				self.serve.wait(10)
			except subprocess.TimeoutExpired:
				self.serve.kill()
		if self.srv_pid or qwatch.jvm_pids(self.srv_dir):
			sh(fill(os.environ.get("QPAIR_SRV_STOP", SRV_STOP), OUT=shlex.quote(self.out)), capture_output=True,
			   stdin=subprocess.DEVNULL, timeout=400)
		for k, p in (("client", pid), ("server", self.srv_pid)):
			if p and qwatch.alive(p):
				probs.append(f"{k} pid {p} still up")
		return probs


def opt(a, k, d=None):
	return a[a.index(k) + 1] if k in a else d


def opts(a, k):
	return [a[i + 1] for i, x in enumerate(a) if x == k and i + 1 < len(a)]


def main_pair(a):
	if len(a) < 4:
		sys.exit(__doc__)
	out, game, batch, res = a[:4]
	p = Pair(out, game, res, opts(a, "--jar"), drop=opts(a, "--drop"),
			 config=[re.match(r"([^:]+):([^:]+):([^=]+)=(.*)", x).groups() for x in opts(a, "--config")])
	ok, why = p.boot_server()
	print("server:", why)
	if not ok:
		print("ABORT: server did not boot;", "; ".join(p.stop()) or "nothing left up")
		return 1
	p.launch_client()
	outcome, ev = p.wait_join(float(opt(a, "--join-secs", "600")))
	print("client:", outcome, "|", "; ".join(ev)[:300])
	rc = 0
	try:
		if outcome == "JOINED":
			t = p.target(opt(a, "--default", "srv"))
			qrun.run_batch(["pair", out, batch, res, *a[4:]], "pair", t)
		else:
			rc = 1
	finally:
		probs = p.stop()
		print("stopped both" if not probs else "STOP PROBLEMS: " + "; ".join(probs))
	return rc or (1 if probs else 0)


def case_plan(spec, c, res):
	out = spec.get("out") or os.path.join(res, "_out")
	lines = [f"prep: {' '.join(f'{k}={v}' for k, v in c.get('srv_env', {}).items())} "
			 f"{fill(spec.get('prep', PREP), OUT=out)}"]
	lines += [f"srv drop: {g}" for g in c.get("srv_drop", [])]
	lines += [f"srv toml: {' '.join(map(str, x))}" for x in c.get("srv_toml", [])]
	lines += [f"cli jar: {j}" for j in c.get("cli_jars", [])]
	lines += [f"cli drop: {g}" for g in c.get("cli_drop", [])]
	lines += [f"cli config: {' '.join(map(str, x))}" for x in c.get("cli_config", [])]
	lines.append(f"hold: {c.get('hold_secs', spec.get('hold_secs', 30))} s after a join")
	lines.append(f"expect: {c.get('expect')}" + (f" /{c['refusal']}/" if c.get("refusal") else "")
				 + (f", then batch {c['batch']}" if c.get("batch") else ""))
	return lines


def run_case(spec, c, res):
	import qverdict
	rid, cres = c["id"], os.path.join(res, c["id"])
	out = os.path.abspath(spec.get("out") or os.path.join(res, "_out"))
	game = os.path.abspath(spec.get("game") or GAME_DEFAULT)
	os.makedirs(cres, exist_ok=True)
	checks, t0 = [], time.time()

	def chk(step, arg, ok, detail):
		checks.append({"step": step, "arg": arg, "ok": ok, "detail": detail})

	r = sh(fill(spec.get("prep", PREP), OUT=shlex.quote(out)), capture_output=True, text=True,
		   stdin=subprocess.DEVNULL, env=dict(os.environ, INST=INST, **c.get("srv_env", {})))
	open(os.path.join(cres, "prep.out"), "w").write(r.stdout + r.stderr)
	if r.returncode:
		chk("prep", "", False, f"prep exit {r.returncode}: {(r.stderr or r.stdout).strip()[-200:]}")
		outcome, ev = "BOOT-FAIL", []
	else:
		for g in c.get("srv_drop", []):
			for f in glob.glob(os.path.join(out, "srv/mods", g)):
				os.remove(f)  # unlink: hard links to mods/ stay intact
		for f, sec, k, v in c.get("srv_toml", []):
			qrun_toml(os.path.join(out, "srv", f), sec, k, v)
		p = Pair(out, game, cres, c.get("cli_jars", []), drop=c.get("cli_drop", []),
				 config=c.get("cli_config", []))
		try:
			ok, why = p.boot_server()
			if not ok:
				outcome, ev = "BOOT-FAIL", [why]
			else:
				p.launch_client()
				outcome, ev = p.wait_join(float(spec.get("join_secs", 600)),
										  spec.get("cli_refusal", CLI_REFUSAL), spec.get("srv_refusal", SRV_REFUSAL))
				if outcome == "JOINED":
					hs = float(c.get("hold_secs", spec.get("hold_secs", 30)))
					o2, e2 = p.hold(hs)
					if o2:
						outcome, ev = o2, ev + e2
					else:
						ev = ev + [f"both JVMs alive {hs:.0f} s after joining"]
				if outcome == "JOINED" and c.get("batch"):
					bres = qrun.run_batch(["pair", out, c["batch"], os.path.join(cres, "batch"), "--no-lint"], "pair",
										  p.target())
					nb = [k for k, v in bres.items() if v["result"] in ("FAIL", "CRASH")]
					chk("batch", c["batch"], not nb, f"batch rows not passing: {', '.join(nb)}" if nb else
						f"{len(bres)} rows, none FAIL / CRASH")
		finally:
			probs = p.stop()
			for f in ("logs/latest.log", "crash-reports"):
				for d, tag in ((os.path.join(out, "srv"), "srv"), (game, "cli")):
					src = os.path.join(d, f)
					if os.path.isfile(src):
						subprocess.run(["cp", src, os.path.join(cres, f"{tag}_latest.log")])
					elif os.path.isdir(src) and os.listdir(src):
						subprocess.run(["cp", "-r", src, os.path.join(cres, f"{tag}_crash-reports")])
			if probs:
				chk("stop", "", False, "; ".join(probs))
	exp = c.get("expect", "refuse")
	want = {"refuse": "REFUSED", "join": "JOINED"}.get(exp, exp.upper())
	ok = outcome == want
	detail = f"{outcome}: " + " | ".join(ev)[:600]
	if ok and want == "REFUSED" and c.get("refusal"):
		ok = any(re.search(c["refusal"], l) for l in ev)
		if not ok:
			detail += f" (refusal text does not match /{c['refusal']}/)"
	if outcome != "NOT-RUN":
		chk("outcome", exp, ok, detail)
	r = {"result": "CRASH" if outcome.startswith("CRASH") else ("PASS" if all(x["ok"] for x in checks) else "FAIL"),
		 "looks": [], "shots": [], "lines": 0, "started": t0, "ended": time.time()}
	crash = {"reason": detail, "evidence_dir": c["id"], "files": []} if outcome.startswith("CRASH") else None
	d = qverdict.write(res, {"id": rid, "title": c.get("title", "")}, r, checks, spec.get("_path", ""), "matrix", crash)
	d["outcome"] = outcome
	json.dump(d, open(qverdict.path(res, rid), "w"), indent=1)
	return d


def qrun_toml(path, section, key, value):
	subprocess.run([sys.executable, os.path.join(HERE, "qrun.py"), "toml", path, section, key, str(value)],
				   capture_output=True)


def main_matrix(a):
	if len(a) < 2:
		sys.exit(__doc__)
	spec_path, res = a[0], a[1]
	spec = json.load(open(spec_path))
	spec["_path"] = os.path.abspath(spec_path)
	only = set(opt(a, "--only").split(",")) if "--only" in a else None
	cases = [c for c in spec["cases"] if not only or c["id"] in only]
	if "--plan" in a:
		for c in cases:
			print(f"## {c['id']} | {c.get('title', '')}")
			for l in case_plan(spec, c, res):
				print("   " + l)
		print(f"plan: {len(cases)} case(s), nothing launched")
		return 0
	os.makedirs(res, exist_ok=True)
	rows = []
	for c in cases:
		print(f"{c['id']} ...", end=" ", flush=True)
		d = run_case(spec, c, res)
		print(d["verdict"], d.get("outcome"))
		rows.append(d)
		if d.get("outcome") == "BOOT-FAIL" and game_jvms():
			print("ABORT: a game JVM is still up after a failed boot")
			break
	with open(os.path.join(res, "matrix.md"), "w") as f:
		f.write("| Case | Expect | Outcome | Verdict | Evidence |\n|---|---|---|---|---|\n")
		for d, c in zip(rows, cases):
			ev = "; ".join(x["detail"] for x in d["checks"])[:400].replace("|", "/")
			f.write(f"| {d['id']} | {c.get('expect', 'refuse')} | {d.get('outcome')} | {d['verdict']} | {ev} |\n")
	print("->", os.path.join(res, "matrix.md"))
	return 0 if all(d["verdict"] == "PASS" for d in rows) else 1


if __name__ == "__main__":
	w = sys.argv[1] if len(sys.argv) > 1 else ""
	if w == "pair":
		sys.exit(main_pair(sys.argv[2:]))
	if w == "matrix":
		sys.exit(main_matrix(sys.argv[2:]))
	sys.exit(__doc__)

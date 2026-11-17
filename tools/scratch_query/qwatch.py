#!/usr/bin/env python3
"""Crash watch for qrun (TX-06): notices a dead game JVM, a new hs_err_pid*.log or a new crash-reports/ file while a
batch runs, so the row ends CRASH at once instead of timing out on every later waitlog.

Used by qrun.py (run and client modes); no CLI of its own except a self-check:

    qwatch.py status DIR       the JVM pids with cwd DIR and the crash files already there

The JVM is found by its working directory (pgrep -x java, readlink /proc/PID/cwd; no command line is read, so the
access token never shows). Server: DIR = OUT/srv; client: DIR = the eval_client game dir. When no JVM is found at the
start (qrun started from a shell that cannot see the game's pids, gotchas "Background run_in_background shells"),
the watch runs on files only and says so.
"""
import os
import shutil
import subprocess
import sys
import time


class Crashed(Exception):
	"""Raised inside a row when the watch fires. reason: one line; evidence: paths of the new crash files."""

	def __init__(self, reason, evidence=(), stopped=False):
		super().__init__(reason)
		self.reason = reason
		self.evidence = list(evidence)
		self.stopped = stopped  # the batch sent "stop": not a crash


def jvm_pids(d):
	"""pids of java processes whose cwd is d (Gradle daemons skipped by cwd: they never run in a game dir)."""
	d = os.path.realpath(d)
	out = subprocess.run(["pgrep", "-x", "java"], capture_output=True, text=True).stdout.split()
	pids = []
	for p in out:
		try:
			if os.path.realpath(os.readlink(f"/proc/{p}/cwd")) == d:
				pids.append(int(p))
		except OSError:
			pass
	return pids


def alive(pid):
	try:
		with open(f"/proc/{pid}/stat") as f:
			return f.read().split(") ", 1)[1][0] != "Z"  # a zombie is dead for us
	except (OSError, IndexError):
		return False


def crash_files(d):
	fs = set()
	try:
		fs |= {f for f in os.listdir(d) if f.startswith("hs_err_pid") and f.endswith(".log")}
	except OSError:
		pass
	cr = os.path.join(d, "crash-reports")
	if os.path.isdir(cr):
		fs |= {os.path.join("crash-reports", f) for f in os.listdir(cr)}
	return fs


class Watch:
	"""kind: 'server' or 'client'; d: the game dir (cwd of the JVM). pids: the JVMs to watch (found when None)."""

	def __init__(self, kind, d, pids=None, log=print):
		self.kind, self.dir, self.log = kind, d, log
		self.files0 = crash_files(d)
		self.pids = list(pids) if pids else jvm_pids(d)
		self.stopping = False  # set by qrun when the batch itself sends "stop"
		if not self.pids:
			log(f"crash watch: no game JVM with cwd {d}; watching crash files only")
		else:
			log(f"crash watch: pid {','.join(map(str, self.pids))}, {len(self.files0)} old crash file(s) ignored")

	def new_files(self):
		return sorted(crash_files(self.dir) - self.files0)

	def check(self):
		"""Raise Crashed when the JVM died or a crash file appeared."""
		new = self.new_files()
		dead = [p for p in self.pids if not alive(p)]
		if dead and self.stopping and not new:
			raise Crashed(f"{self.kind} stopped by the batch (pid {dead[0]} gone)", stopped=True)
		if new:
			raise Crashed(f"{self.kind} crash file(s): {', '.join(new)}" + (f"; pid {dead[0]} gone" if dead else ""), new)
		if dead:
			# a JVM can die a moment before its hs_err file is complete: look once more
			time.sleep(2)
			new = self.new_files()
			raise Crashed(f"{self.kind} JVM pid {dead[0]} gone" + (f"; crash file(s): {', '.join(new)}" if new else
																	" with no crash file (killed? OOM?)"), new)

	def collect(self, res, rid, extra_logs=()):
		"""Copy the evidence to RES/crash/ID/: new crash files, latest.log, debug.log tail, KubeJS logs."""
		dst = os.path.join(res, "crash", rid)
		os.makedirs(dst, exist_ok=True)
		got = []
		for f in self.new_files():
			src = os.path.join(self.dir, f)
			if os.path.isfile(src):
				shutil.copy2(src, os.path.join(dst, os.path.basename(f)))
				got.append(os.path.basename(f))
		for f in ("logs/latest.log", "logs/kubejs/server.log", "logs/kubejs/client.log", "logs/kubejs/startup.log",
				  "launch.out", *extra_logs):
			src = os.path.join(self.dir, f)
			if os.path.isfile(src):
				shutil.copy2(src, os.path.join(dst, f.replace("/", "_")))
				got.append(f)
		src = os.path.join(self.dir, "logs/debug.log")
		if os.path.isfile(src):
			with open(src, "rb") as fi:
				fi.seek(max(0, os.path.getsize(src) - 2_000_000))
				open(os.path.join(dst, "logs_debug.tail.log"), "wb").write(fi.read())
			got.append("logs/debug.log (last 2 MB)")
		return os.path.relpath(dst, res), got

	def rearm(self, wait_exit=60):
		"""After a crash: wait for the old JVM to exit (never kill it), then take the current crash files as old.
        False when the JVM is still up after wait_exit seconds."""
		end = time.time() + wait_exit
		while any(alive(p) for p in self.pids) and time.time() < end:
			time.sleep(1)
		if any(alive(p) for p in self.pids):
			return False
		self.files0 = crash_files(self.dir)
		self.pids = []
		return True

	def relaunch(self, cmd, ready, ready_secs, logfile, outfile):
		"""Run cmd (bash, cwd = instance; its output goes to outfile, never a pipe: a launcher's children would hold
        a pipe open forever), take the pid it prints ('client pid N' / 'pid N') or find the JVM by cwd, then wait for
        the regex ready in logfile. A cmd that stays in the foreground is left running. Returns (ok, message)."""
		import re
		inst = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
		off = os.path.getsize(logfile) if os.path.exists(logfile) else 0
		end = time.time() + ready_secs
		with open(outfile, "w") as out:
			p = subprocess.Popen(["bash", "-c", cmd], cwd=inst, stdout=out, stderr=subprocess.STDOUT,
								 stdin=subprocess.DEVNULL, start_new_session=True, env=dict(os.environ, INST=inst))
		rx = re.compile(ready)
		while time.time() < end:
			rc = p.poll()
			if rc:
				return False, f"relaunch exit {rc}: {open(outfile, errors='replace').read().strip()[-200:]}"
			if not self.pids:
				m = re.search(r"\bpid (\d+)", open(outfile, errors="replace").read())
				self.pids = [int(m.group(1))] if m else jvm_pids(self.dir)
			try:
				if os.path.getsize(logfile) < off:
					off = 0  # a new latest.log
				with open(logfile, "rb") as f:
					f.seek(off)
					if any(rx.search(l) for l in f.read().decode("utf-8", "replace").splitlines()):
						return True, f"relaunched (pid {','.join(map(str, self.pids)) or '?'})"
			except OSError:
				pass
			if self.pids and not any(alive(x) for x in self.pids):
				return False, "relaunched JVM died before it was ready"
			if self.new_files():
				return False, f"crash during relaunch: {', '.join(self.new_files())}"
			time.sleep(2)
		return False, f"no /{ready}/ in {int(ready_secs)} s after the relaunch"


if __name__ == "__main__":
	if len(sys.argv) == 3 and sys.argv[1] == "status":
		d = sys.argv[2]
		print("pids:", jvm_pids(d) or "none")
		print("crash files:", sorted(crash_files(d)) or "none")
	else:
		sys.exit(__doc__)

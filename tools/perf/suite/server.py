"""Scratch server lifecycle for the perf suite (feature_writeup/perf_suite.md 3.3): setup on top of
eval_server.sh, boot, liveness, RCON, stop, and triage when the server ends without warning.

Every wait checks that the JVM is alive and that the tick log still moves, so a crash, watchdog kill, OOM kill,
kill -9 or a hung server thread ends the wait at once with ServerEnded instead of a timeout.
"""
import glob
import os
import secrets
import shutil
import signal
import subprocess
import time

from rcon import Rcon, RconError
import ticklog

INST = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
EVAL = os.path.join(INST, "tools/perf/eval/eval_server.sh")
GAME_JVMS = os.path.join(INST, "tools/scratch_query/game_jvms.sh")
BOT_JAR = os.path.join(INST, "development/perfBot/build/libs")
RCON_PORT = 25598
CRASH_MARKS = ("Crash report saved", "rolling back to VANILLA", "Failed to start the minecraft server",
               "Encountered an unexpected exception")


class ServerEnded(Exception):
    """The JVM is gone (or was killed because it hung). .info holds the triage."""

    def __init__(self, info):
        super().__init__(info.get("cause", "ended"))
        self.info = info


class Aborted(Exception):
    """The runner was asked to stop (signal); state is saved, `resume` continues."""


def set_prop(path, key, value):
    lines, done = [], False
    with open(path) as fh:
        for line in fh:
            if line.split("=", 1)[0] == key:
                lines.append(f"{key}={value}\n")
                done = True
            else:
                lines.append(line)
    if not done:
        lines.append(f"{key}={value}\n")
    with open(path, "w") as fh:
        fh.writelines(lines)


def other_game_jvms(own_pid=None):
    """[(pid, cwd)] of game JVMs that are not ours (game_jvms.sh never prints command lines)."""
    try:
        out = subprocess.run(["bash", GAME_JVMS], capture_output=True, text=True, timeout=20).stdout
    except subprocess.TimeoutExpired:
        return []
    rows = []
    for line in out.splitlines():
        p = line.split(" ", 1)
        if p and p[0].isdigit() and int(p[0]) != own_pid:
            rows.append((int(p[0]), p[1] if len(p) > 1 else ""))
    return rows


def gradle_running():
    for pid in subprocess.run(["pgrep", "-x", "java"], capture_output=True, text=True).stdout.split():
        try:
            with open(f"/proc/{pid}/cmdline", "rb") as fh:
                args = fh.read()
            if b"GradleDaemon" in args or b"GradleWrapperMain" in args:
                return True
        except OSError:
            pass
    return False


def loadavg():
    with open("/proc/loadavg") as fh:
        return float(fh.read().split()[0])


class Server:
    def __init__(self, out_dir, log, should_stop, hang_s=120):
        self.out = out_dir
        self.srv = os.path.join(out_dir, "srv")
        self.log = log                    # callable(str)
        self.should_stop = should_stop    # callable() -> bool
        self.hang_s = hang_s
        self.proc = None                  # the bash run.sh child, when we started it
        self.pid = None                   # the java pid
        self.rcon = None
        self.boot_t = None
        self.contended = False
        self.events = []                  # per-window notes (foreign JVM, gradle, load)
        self.killed_by_us = None          # reason, when the runner itself sent the SIGKILL

    # ---- setup ---------------------------------------------------------------------------------------------
    def setup(self, level, world, watchdog=True, props=None, jfr_max="400m"):
        if os.path.exists(self.srv):
            shutil.rmtree(self.srv)
        env = dict(os.environ, JFR_MAX=jfr_max)
        r = subprocess.run(["bash", EVAL, "setup", self.out, level], capture_output=True, text=True, env=env)
        if r.returncode != 0:
            raise RuntimeError("eval_server.sh setup failed: " + (r.stderr or r.stdout)[-800:])
        sp = os.path.join(self.srv, "server.properties")
        if world == "flat":
            set_prop(sp, "level-type", "minecraft\\:flat")
            set_prop(sp, "generate-structures", "false")
        set_prop(sp, "enable-rcon", "true")
        set_prop(sp, "rcon.port", str(RCON_PORT))
        password = secrets.token_hex(12)
        set_prop(sp, "rcon.password", password)
        set_prop(sp, "broadcast-rcon-to-ops", "false")
        set_prop(sp, "broadcast-console-to-ops", "false")
        # the shipped server keeps the 60 s watchdog; a watchdog kill is a result the suite records
        set_prop(sp, "max-tick-time", "60000" if watchdog else "-1")
        for k, v in (props or {}).items():
            set_prop(sp, k, v)
        jars = sorted(glob.glob(os.path.join(BOT_JAR, "ceperfbot-*.jar")), key=os.path.getmtime)
        if not jars:
            raise RuntimeError("no ceperfbot jar: ./gradlew :perfBot:build --offline")
        shutil.copy2(jars[-1], os.path.join(self.srv, "mods"))
        with open(os.path.join(self.srv, "user_jvm_args.txt"), "a") as fh:
            fh.write("-Dceperfbot.out=perf/bot\n")
        with open(os.path.join(self.srv, "perf_suite.owner"), "w") as fh:
            fh.write(f"perf_suite runner pid {os.getpid()}\n")
        with open(os.path.join(self.out, "rcon.secret"), "w") as fh:
            fh.write(password)
        os.chmod(os.path.join(self.out, "rcon.secret"), 0o600)
        # which build of our own jars this run measured (results are only comparable at the same builds)
        ours = {f: time.strftime("%m-%d %H:%M", time.localtime(os.path.getmtime(os.path.join(self.srv, "mods", f))))
                for f in os.listdir(os.path.join(self.srv, "mods"))
                if f.startswith(("creativeengineering", "creative_delight", "ceperfbot"))}
        return {"jars": len(os.listdir(os.path.join(self.srv, "mods"))), "bot_jar": os.path.basename(jars[-1]),
                "our_jars": ours}

    def password(self):
        with open(os.path.join(self.out, "rcon.secret")) as fh:
            return fh.read().strip()

    # ---- process -------------------------------------------------------------------------------------------
    def find_pid(self):
        real = os.path.realpath(self.srv)
        for pid in subprocess.run(["pgrep", "-x", "java"], capture_output=True, text=True).stdout.split():
            try:
                cwd = os.readlink(f"/proc/{pid}/cwd")
            except OSError:
                continue
            # "srv (deleted)" when the folder was wiped under a live JVM
            if cwd == real or cwd.startswith(real + " ") or cwd.startswith(real + "/"):
                return int(pid)
        return None

    def alive(self):
        if self.pid is None:
            return False
        try:
            with open(f"/proc/{self.pid}/stat") as fh:
                state = fh.read().rsplit(")", 1)[1].split()[0]
            return state != "Z"
        except OSError:
            return False

    def latest_log(self):
        return os.path.join(self.srv, "logs/latest.log")

    def log_has(self, text, count=1):
        try:
            with open(self.latest_log(), errors="replace") as fh:
                return fh.read().count(text) >= count
        except OSError:
            return False

    def wait_for_slot(self, max_wait_s):
        """One game JVM at a time (memory one-game-instance-at-a-time): wait for other JVMs to go."""
        t0 = time.time()
        while True:
            others = other_game_jvms(self.pid)
            if not others:
                return
            if time.time() - t0 > max_wait_s:
                raise Aborted(f"another game JVM stayed up {max_wait_s} s: {others}")
            self.log(f"waiting: other game JVM up {[p for p, _ in others]}")
            for _ in range(30):
                self._check_stop()
                time.sleep(1)

    def boot(self, timeout_s=1500, foreign_wait_s=3600):
        existing = self.find_pid()
        if existing:
            # a server left by a killed runner: adopt it once RCON answers (it may still be booting)
            self.pid = existing
            self.boot_t = time.time()
            for _ in range(300):
                if not self.alive():
                    break
                if self._try_rcon():
                    self.log(f"adopted running server pid {existing}")
                    return {"adopted": True}
                time.sleep(1)
            self.kill("left-over JVM without RCON")
        self.wait_for_slot(foreign_wait_s)
        for f in ("logs/latest.log",):
            p = os.path.join(self.srv, f)
            if os.path.exists(p):
                os.remove(p)
        console = open(os.path.join(self.srv, "console.log"), "ab")
        self.boot_t = time.time()
        self.proc = subprocess.Popen(["bash", "run.sh", "nogui"], cwd=self.srv, stdin=subprocess.DEVNULL,
                                     stdout=console, stderr=subprocess.STDOUT, start_new_session=True)
        console.close()
        self.pid = None
        for _ in range(120):
            self.pid = self.find_pid()
            if self.pid or self.proc.poll() is not None:
                break
            time.sleep(1)
        if not self.pid:
            raise ServerEnded(self.triage("JVM never started"))
        self.log(f"booting pid {self.pid}")
        done_line = None
        while time.time() - self.boot_t < timeout_s:
            self._check_stop()
            if not self.alive():
                raise ServerEnded(self.triage("died during boot"))
            try:
                with open(self.latest_log(), errors="replace") as fh:
                    text = fh.read()
            except OSError:
                text = ""
            for mark in CRASH_MARKS:
                if mark in text:
                    self.kill("boot crash: " + mark)
                    raise ServerEnded(self.triage("crashed during boot: " + mark))
            i = text.find("Done (")
            if i >= 0:
                done_line = text[i:text.find("\n", i)]
                break
            time.sleep(1)
        if done_line is None:
            self.kill("boot timeout")
            raise ServerEnded(self.triage(f"boot timeout {timeout_s} s"))
        wall = time.time() - self.boot_t
        for _ in range(60):
            if self._try_rcon():
                break
            time.sleep(1)
        else:
            raise RuntimeError("server is up but RCON never answered")
        out = self.cmd("pbot status")
        if "ceperfbot ok" not in out:
            raise RuntimeError("ceperfbot not loaded: " + out[:200])
        return {"wall_s": round(wall, 1), "done": done_line.strip()}

    def _try_rcon(self):
        try:
            r = Rcon("127.0.0.1", RCON_PORT, self.password())
            r.connect()
            self.rcon = r
            return True
        except (OSError, RconError):
            return False

    def cmd(self, command, timeout=300):
        self._check_stop()
        for attempt in (1, 2):
            try:
                if self.rcon is None:
                    self.rcon = Rcon("127.0.0.1", RCON_PORT, self.password())
                    self.rcon.connect()
                return self.rcon.command(command, timeout)
            except (OSError, RconError) as e:
                self.rcon = None
                if not self.alive():
                    raise ServerEnded(self.triage(f"died during command: {command[:80]}"))
                if attempt == 2:
                    raise RuntimeError(f"rcon failed twice on {command[:80]}: {e}")
                time.sleep(2)

    def watch(self, seconds, note_every=10):
        """Sleeps `seconds` while checking liveness every second and the machine every note_every s."""
        t_end = time.time() + seconds
        n = 0
        while time.time() < t_end:
            self._check_stop()
            if not self.alive():
                raise ServerEnded(self.triage("died"))
            age = ticklog.heartbeat_age(os.path.join(self.srv, "perf/bot"), time.time())
            if age is not None and age > self.hang_s:
                self.hung(age)
            if n % note_every == 0:
                self.sample_machine()
            n += 1
            time.sleep(1)

    def sample_machine(self):
        others = other_game_jvms(self.pid)
        la = loadavg()
        if others:
            self.contended = True
            self.events.append(f"{int(time.time())} other game JVM {[p for p, _ in others]}")
        if gradle_running():
            self.contended = True
            self.events.append(f"{int(time.time())} gradle running")
        self.events.append(f"{int(time.time())} load {la}")

    def take_events(self):
        ev, c = self.events, self.contended
        self.events, self.contended = [], False
        return ev, c

    def _check_stop(self):
        if self.should_stop():
            raise Aborted("signal")

    # ---- ending --------------------------------------------------------------------------------------------
    def hung(self, age):
        d = self.evidence_dir("hung")
        for i in (1, 2):
            with open(os.path.join(d, f"jstack-{i}.txt"), "w") as fh:
                subprocess.run(["jstack", str(self.pid)], stdout=fh, stderr=subprocess.STDOUT, timeout=60)
            time.sleep(5)
        self.kill(f"hung: tick log still for {int(age)} s")
        raise ServerEnded(self.triage(f"hung (tick log still {int(age)} s), jstacks taken, killed", d))

    def kill(self, why):
        if self.pid and self.alive():
            self.log(f"kill -9 {self.pid}: {why}")
            self.killed_by_us = why
            try:
                os.kill(self.pid, signal.SIGKILL)
            except OSError:
                pass
            for _ in range(30):
                if not self.alive():
                    break
                time.sleep(1)

    def stop(self, timeout_s=180):
        """Clean stop; kills the JVM if it hangs after saving (Async Locator threads)."""
        if not self.alive():
            return {"stopped": "already gone"}
        try:
            self.rcon.command("stop", timeout=30)
        except Exception:
            pass
        self.rcon = None
        t0 = time.time()
        saved_at = None
        while time.time() - t0 < timeout_s and self.alive():
            if saved_at is None and self.log_has("All dimensions are saved"):
                saved_at = time.time()
            if saved_at and time.time() - saved_at > 15:
                self.kill("stop hung after saving")
                break
            time.sleep(1)
        if self.alive():
            self.kill("stop timeout")
        if self.proc:
            try:
                self.proc.wait(10)
            except subprocess.TimeoutExpired:
                pass
        self.pid = None
        return {"stopped": round(time.time() - t0, 1)}

    def evidence_dir(self, tag):
        d = os.path.join(self.out, "endings", time.strftime("%m%d-%H%M%S") + "-" + tag)
        os.makedirs(d, exist_ok=True)
        return d

    def triage(self, what, d=None):
        """Why did the server end? Copies the evidence and salvages the JFR recording."""
        d = d or self.evidence_dir("ended")
        info = {"cause": what, "at": int(time.time()), "pid": self.pid, "evidence": d}
        if self.proc is not None:
            try:
                info["exit_code"] = self.proc.wait(15)
            except subprocess.TimeoutExpired:
                info["exit_code"] = None
        since = self.boot_t or 0
        log_text = ""
        for name in ("logs/latest.log", "console.log"):
            p = os.path.join(self.srv, name)
            if os.path.exists(p):
                shutil.copy2(p, os.path.join(d, os.path.basename(name)))
                with open(p, errors="replace") as fh:
                    log_text += fh.read()[-400000:]
        found = []
        if "Considering it to be crashed" in log_text:
            found.append("watchdog (a single tick over max-tick-time)")
        if "OutOfMemoryError" in log_text:
            found.append("OutOfMemoryError")
        if "Stopping server" in log_text and "Considering it to be crashed" not in log_text:
            found.append("server ran its stop sequence (stop command or crash handler)")
        for p in glob.glob(os.path.join(self.srv, "crash-reports/*.txt")):
            if os.path.getmtime(p) >= since:
                shutil.copy2(p, d)
                found.append("crash report " + os.path.basename(p))
        for p in glob.glob(os.path.join(self.srv, "hs_err_pid*.log")):
            if os.path.getmtime(p) >= since:
                shutil.copy2(p, d)
                found.append("JVM fatal error " + os.path.basename(p))
        code = info.get("exit_code")
        if self.killed_by_us:
            found.append("killed by the runner: " + self.killed_by_us)
        elif code in (137, -9) and not found:
            found.append("SIGKILL with no log trace: OOM killer or an outside kill")
        if self.pid:
            try:
                k = subprocess.run(["journalctl", "-k", "--since", f"@{int(since)}", "--no-pager", "-q"],
                                   capture_output=True, text=True, timeout=20).stdout
                hits = [l for l in k.splitlines() if "Killed process" in l and f" {self.pid} " in l]
                if hits:
                    found.append("kernel OOM killer: " + hits[-1][-160:])
            except (OSError, subprocess.TimeoutExpired):
                pass
        info["found"] = found
        info["jfr"] = self.salvage_jfr(d, since)
        with open(os.path.join(d, "triage.txt"), "w") as fh:
            for k, v in info.items():
                fh.write(f"{k}: {v}\n")
        self.pid = None
        self.rcon = None
        self.killed_by_us = None
        self.log(f"server ended: {what}; {found}")
        return info

    def salvage_jfr(self, d, since):
        """dumponexit file when the JVM exited, else assemble the leftover disk repository (kill -9)."""
        exits = [p for p in glob.glob(os.path.join(self.srv, "perf/exit/*.jfr")) if os.path.getmtime(p) >= since]
        if exits:
            p = max(exits, key=os.path.getmtime)
            shutil.move(p, os.path.join(d, "exit.jfr"))
            return os.path.join(d, "exit.jfr")
        repos = [p for p in glob.glob(os.path.join(self.srv, "perf/jfr-repo/*")) if os.path.isdir(p)]
        if not repos:
            return None
        repo = max(repos, key=os.path.getmtime)
        target = os.path.join(d, "salvaged.jfr")
        r = subprocess.run(["jfr", "assemble", repo, target], capture_output=True, text=True, timeout=300)
        if r.returncode == 0 and os.path.exists(target):
            shutil.rmtree(repo, ignore_errors=True)
            return target
        return "assemble failed: " + (r.stderr or r.stdout)[-200:]

    def jfr_dump(self, path, seconds):
        if not self.alive():
            return None
        r = subprocess.run(["jcmd", str(self.pid), "JFR.dump", "name=ce", f"maxage={int(seconds)}s",
                            f"filename={os.path.abspath(path)}"], capture_output=True, text=True, timeout=120)
        return path if r.returncode == 0 and os.path.exists(path) else None

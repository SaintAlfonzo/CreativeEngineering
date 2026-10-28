#!/usr/bin/env python3
"""Offline check of the perf suite's end-of-server handling, no Minecraft (feature_writeup/perf_suite.md 6).

  python3 tools/perf/suite/selftest/selftest.py [WORKDIR]

Runs the real runner (perf_suite.execute) against fake_server.py: a clean window, a kill -9 mid-window (retried,
dies again), a hung tick log, a crash on the next boot, an OOM exit, then kills the RUNNER mid-scenario and
resumes it (the left-over server must be adopted). Only Server.setup is replaced, and jstack/jcmd (their attach
sends SIGQUIT, which would kill the fake). Prints PASS/FAIL per expectation; exit 1 on any FAIL.
"""
import json
import os
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import types

HERE = os.path.dirname(os.path.abspath(__file__))
SUITE = os.path.dirname(HERE)
sys.path.insert(0, SUITE)
sys.path.insert(0, os.path.join(SUITE, "plans"))

import perf_suite  # noqa: E402
import server  # noqa: E402

PLAN = types.ModuleType("selftest_plan")
PLAN.GROUPS = [dict(id="T", title="fake", world="flat", level="t", scenarios=[
    dict(id="T01", title="clean window", settle=2, measure=5, jfr=False),
    dict(id="T02", title="kill -9 mid-window", settle=1, measure=10, jfr=False, start=["fake die9"]),
    dict(id="T03", title="hung tick log", settle=1, measure=30, jfr=False, retries=0, start=["fake hang"]),
    dict(id="T04", title="next boot crashes, then OOM", settle=1, measure=10, jfr=False, retries=0,
         start=["fake crashnext", "fake oom"]),
    dict(id="T05", title="runner killed here, resumed", settle=1, measure=20, jfr=False),
    dict(id="T06", title="after resume", settle=1, measure=3, jfr=False),
])]
sys.modules["selftest_plan"] = PLAN


def fake_setup(self, level, world, watchdog=True, props=None, jfr_max="400m"):
    if os.path.exists(self.srv):
        shutil.rmtree(self.srv)
    os.makedirs(self.srv)
    shutil.copy(os.path.realpath(shutil.which("python3")), os.path.join(self.srv, "java"))
    shutil.copy(os.path.join(HERE, "fake_server.py"), self.srv)
    with open(os.path.join(self.srv, "run.sh"), "w") as fh:
        fh.write("#!/bin/bash\nexec ./java -I fake_server.py\n")
    with open(os.path.join(self.srv, "server.properties"), "w") as fh:
        fh.write(f"rcon.port={server.RCON_PORT}\nrcon.password=selftest\n")
    with open(os.path.join(self.out, "rcon.secret"), "w") as fh:
        fh.write("selftest")
    open(os.path.join(self.srv, "perf_suite.owner"), "w").write("selftest\n")
    return {"fake": True}


real_run = subprocess.run


def fake_tools(args, *a, **k):
    if args and args[0] in ("jstack", "jcmd"):
        if k.get("stdout") not in (None, subprocess.PIPE):
            k["stdout"].write("selftest: jstack stubbed\n")
        return subprocess.CompletedProcess(args, 1, "", "")
    return real_run(args, *a, **k)


def patch():
    server.Server.setup = fake_setup
    server.subprocess.run = fake_tools


def child(workdir, what, name):
    patch()
    perf_suite.RUNS = workdir
    run = perf_suite.Run(name)
    if what == "run":
        os.makedirs(run.dir, exist_ok=True)
        run.state = {"plan": "selftest_plan", "created": int(time.time()), "status": "running", "groups": {},
                     "scenarios": {}, "options": {"hang_s": 8}}
        run.save()
    else:
        run.load()
    perf_suite.execute(run)


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "child":
        return child(sys.argv[2], sys.argv[3], sys.argv[4])
    workdir = sys.argv[1] if len(sys.argv) > 1 else tempfile.mkdtemp(prefix="perfsuite-selftest-")
    name = "st"
    rdir = os.path.join(workdir, name)
    me = os.path.abspath(__file__)
    p = subprocess.Popen([sys.executable, me, "child", workdir, "run", name])
    # kill the runner (not the server) once T05's window is running
    t0 = time.time()
    while time.time() - t0 < 600:
        try:
            st = json.load(open(os.path.join(rdir, "state.json")))
            if st["scenarios"].get("T05", {}).get("attempts", 0) >= 1:
                time.sleep(8)
                os.kill(p.pid, signal.SIGKILL)
                break
        except (OSError, ValueError):
            pass
        if p.poll() is not None:
            break
        time.sleep(1)
    p.wait()
    killed_runner = p.returncode == -9
    r = subprocess.run([sys.executable, me, "child", workdir, "resume", name], timeout=600)
    rows = [json.loads(l) for l in open(os.path.join(rdir, "results.jsonl"))]
    st = json.load(open(os.path.join(rdir, "state.json")))
    log = open(os.path.join(rdir, "runner.log")).read()

    def rows_of(i):
        return [x for x in rows if x["id"] == i]

    checks = []

    def check(name, ok, detail=""):
        checks.append(ok)
        print(("PASS " if ok else "FAIL ") + name + (f"  [{detail}]" if detail and not ok else ""))

    t01 = rows_of("T01")
    check("T01 clean window has ticks", t01 and t01[0]["status"] == "ok" and t01[0]["window"]["ticks"] > 60,
          str(t01))
    t02 = rows_of("T02")
    check("T02 kill -9: two attempts, both server_died, then failed",
          len(t02) == 2 and all(x["status"] == "server_died" for x in t02) and st["scenarios"]["T02"]["status"] == "failed",
          str([x["status"] for x in t02]))
    check("T02 triage names SIGKILL", t02 and any("SIGKILL" in f for f in t02[0]["ending"]["found"]),
          str(t02[0]["ending"] if t02 else None))
    check("T02 partial window kept and cut short", t02 and (t02[0].get("window") or {}).get("cut_short") is True,
          str(t02[0].get("window") if t02 else None))
    t03 = rows_of("T03")
    check("T03 hung server detected and killed", t03 and t03[0]["status"] == "server_died"
          and "hung" in t03[0]["ending"]["cause"], str(t03[0].get("ending") if t03 else None))
    t04 = rows_of("T04")
    check("T04 OOM named", t04 and any("OutOfMemoryError" in f for f in t04[0]["ending"]["found"]),
          str(t04[0].get("ending") if t04 else None))
    bf = rows_of("T-boot")
    check("crash on the next boot recorded, boot retried", bf and bf[0]["status"] == "boot_failed"
          and any("crash report" in f for f in bf[0]["ending"]["found"]), str(bf))
    check("runner was killed mid-T05", killed_runner, str(p.returncode))
    check("resume adopted the left-over server", "adopted running server" in log)
    check("T05 and T06 ok after resume", st["scenarios"]["T05"]["status"] == "ok" and st["scenarios"]["T06"]["status"] == "ok",
          str(st["scenarios"]))
    check("run done, resume exit 0", st["status"] == "done" and r.returncode == 0, st["status"])
    check("no fake server left running", subprocess.run(["pgrep", "-x", "java"], capture_output=True, text=True).stdout.strip() == ""
          or all(not os.readlink(f"/proc/{q}/cwd").startswith(workdir)
                 for q in subprocess.run(["pgrep", "-x", "java"], capture_output=True, text=True).stdout.split()))
    print(f"{sum(checks)}/{len(checks)} PASS; work dir {workdir}")
    sys.exit(0 if all(checks) else 1)


if __name__ == "__main__":
    main()

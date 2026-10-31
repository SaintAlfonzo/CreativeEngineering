#!/usr/bin/env python3
"""Scratch-server performance suite runner (feature_writeup/perf_suite.md).

  perf_suite.py run PLAN RUN [--groups S,N] [--only G03a,G04] [--scale N] [--include-disabled]
  perf_suite.py resume RUN          continue after a runner kill, a signal or a reboot of the machine
  perf_suite.py status RUN
  perf_suite.py report RUN [--baseline OTHER_RUN]

PLAN is a module in plans/ (general, ...). RUN is a name; everything lands in perf/suite/RUN/ (untracked): state.json,
results.jsonl (one line per scenario attempt, written as soon as it ends), runner.log, <group>/srv (the scratch
server, deleted when the group is done), <group>/ticks/ (tick logs), jfr/, census/, <group>/endings/ (triage of every
server that ended unasked). Start it detached so the session can end:
  setsid nohup python3 tools/perf/suite/perf_suite.py run general g1 > /dev/null 2>&1 &

A server that ends without warning (crash, watchdog, OOM kill, kill -9, hung tick) is triaged, its partial window is
kept, the server is rebooted on the same world and the scenario retried (retries per scenario, default 1); a group
whose server can't boot 3 times in a row is skipped. A runner that is killed resumes with `resume`.
"""
import argparse
import glob
import importlib
import json
import os
import shutil
import signal
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "plans"))

import ticklog  # noqa: E402
from server import Server, ServerEnded, Aborted, INST  # noqa: E402

RUNS = os.path.join(INST, "perf/suite")
MIN_FREE_GB = 5
STOP = {"flag": False}


def on_signal(signum, _frame):
    STOP["flag"] = True


class Run:
    def __init__(self, name):
        self.name = name
        self.dir = os.path.join(RUNS, name)
        self.state_path = os.path.join(self.dir, "state.json")
        self.state = None

    def load(self):
        with open(self.state_path) as fh:
            self.state = json.load(fh)

    def save(self):
        tmp = self.state_path + ".tmp"
        with open(tmp, "w") as fh:
            json.dump(self.state, fh, indent=1)
        os.replace(tmp, self.state_path)

    def log(self, text):
        line = time.strftime("%m-%d %H:%M:%S ") + text
        with open(os.path.join(self.dir, "runner.log"), "a") as fh:
            fh.write(line + "\n")

    def result(self, row):
        row["logged"] = int(time.time())
        with open(os.path.join(self.dir, "results.jsonl"), "a") as fh:
            fh.write(json.dumps(row) + "\n")
            fh.flush()
            os.fsync(fh.fileno())
        try:
            write_summary(self.dir, self.name)
        except Exception as e:  # a report bug must not stop the run
            self.log(f"summary.md not written: {e!r}")

    def lock(self):
        p = os.path.join(self.dir, "runner.pid")
        if os.path.exists(p):
            with open(p) as fh:
                old = fh.read().strip()
            if old.isdigit() and os.path.exists(f"/proc/{old}"):
                with open(f"/proc/{old}/cmdline", "rb") as fh:
                    if b"perf_suite.py" in fh.read():
                        sys.exit(f"run {self.name} already has a runner (pid {old})")
        with open(p, "w") as fh:
            fh.write(str(os.getpid()))

    def unlock(self):
        p = os.path.join(self.dir, "runner.pid")
        if os.path.exists(p):
            os.remove(p)


class Ctx:
    """What scenario builders may read."""

    def __init__(self, gs, scale):
        self.spawn = tuple(gs.get("spawn") or (0, 64, 0))
        self.scale = scale

    @staticmethod
    def site(k):
        # flat lab sites 2000 blocks apart, away from the always-loaded spawn chunks
        return (2000 * (k + 1), 0)


def as_list(v, ctx):
    if v is None:
        return []
    return list(v(ctx) if callable(v) else v)


def send_all(srv, cmds, run, sid):
    for c in cmds:
        out = srv.cmd(c)
        low = out.lower()
        if "unknown" in low or "incorrect argument" in low or "error" in low or "no such" in low:
            run.log(f"{sid}: '{c[:100]}' -> {out[:160]}")


def wait_log_quiet(srv, quiet_s, max_s):
    """Seconds until latest.log stopped growing for quiet_s (watching liveness meanwhile)."""
    t0, last, same = time.time(), -1, 0
    while time.time() - t0 < max_s:
        srv.watch(1)
        try:
            n = os.path.getsize(srv.latest_log())
        except OSError:
            n = -1
        if n == last:
            same += 1
            if same >= quiet_s:
                return round(time.time() - t0 - quiet_s, 1)
        else:
            same, last = 0, n
    return None


def boot_with_retries(run, srv, gid, gs, tries=3):
    for i in range(1, tries + 1):
        try:
            info = srv.boot()
            gs.setdefault("boots", []).append(dict(info, t=int(time.time())))
            run.save()
            return info
        except ServerEnded as e:
            run.result({"id": f"{gid}-boot", "group": gid, "status": "boot_failed", "attempt": i, "ending": e.info})
            run.log(f"{gid}: boot {i} failed: {e.info.get('cause')} {e.info.get('found')}")
            time.sleep(10)
    return None


def init_world(run, srv, group, gs, ctx_scale):
    """Runs every boot: gamerules etc. (idempotent). First boot only: spawn lookup and pregen."""
    if not gs.get("spawn"):
        out = srv.cmd("pbot spawn spawnprobe ~ ~ ~")
        srv.cmd("pbot remove spawnprobe")
        try:
            at = out.split(" at ", 1)[1].split(" in ", 1)[0].split()
            gs["spawn"] = [int(at[0]), int(at[1]), int(at[2])]
        except (IndexError, ValueError):
            raise RuntimeError("spawn lookup failed: " + out[:200])
        run.save()
    ctx = Ctx(gs, ctx_scale)
    send_all(srv, as_list(group.get("init"), ctx), run, group["id"])
    if group.get("pregen") and not gs.get("pregen_done"):
        for i, (cx, cz, rx, rz) in enumerate(as_list(group["pregen"], ctx)):
            if i < gs.get("pregen_areas_done", 0):
                continue
            with open(srv.latest_log(), errors="replace") as fh:
                n0 = fh.read().count("Task finished")
            for c in ("chunky world minecraft:overworld", "chunky shape rectangle", f"chunky center {cx} {cz}",
                      f"chunky radius {rx} {rz}", "chunky start"):
                srv.cmd(c)
            t0 = time.time()
            run.log(f"{group['id']}: pregen {cx} {cz} r {rx}x{rz}")
            while not srv.log_has("Task finished", n0 + 1):
                if time.time() - t0 > 3600:
                    raise RuntimeError("pregen over 60 min (corrupt chunk? perf.md 10.9 problem 1)")
                srv.watch(5)
            gs["pregen_areas_done"] = i + 1
            gs.setdefault("pregen_s", []).append(round(time.time() - t0))
            run.save()
        gs["pregen_done"] = True
        srv.cmd("save-all flush", timeout=600)
        run.save()


def run_scenario(run, srv, gid, sc, ctx, attempt):
    sid = sc["id"]
    label = f"{sid}#{attempt}"
    kind = sc.get("kind", "window")
    row = {"id": sid, "title": sc.get("title"), "group": gid, "kind": kind, "attempt": attempt, "status": "ok",
           "scale": ctx.scale, "t_start": int(time.time())}
    tick_file = None
    try:
        if kind == "restart":
            boots = []
            for _ in range(sc.get("repeat", 1)):
                srv.stop()
                info = srv.boot()
                boots.append(info)
                send_all(srv, as_list(run.group_init, ctx), run, sid)
            row["boots"] = boots
            return row
        srv.cmd("pbot remove-all")
        send_all(srv, as_list(sc.get("setup"), ctx), run, sid)
        srv.take_events()
        srv.watch(sc.get("settle", 60))
        tick_file = ticklog.newest(os.path.join(srv.srv, "perf/bot"))
        srv.cmd(f"pbot mark start:{label}")
        t0 = time.time()
        row["load_start"] = round(os.getloadavg()[0], 2)
        send_all(srv, as_list(sc.get("start"), ctx), run, sid)
        measure = sc.get("measure", 120)
        if kind == "save":
            t = time.time()
            out = srv.cmd("save-all flush", timeout=900)
            row["save_flush_s"] = round(time.time() - t, 2)
            row["save_output"] = out[:200]
            srv.watch(max(0, measure - (time.time() - t0)))
        elif kind == "reload":
            t = time.time()
            srv.cmd("reload", timeout=600)
            row["reload_cmd_s"] = round(time.time() - t, 2)
            row["reload_log_quiet_s"] = wait_log_quiet(srv, 10, measure)
            srv.watch(max(0, measure - (time.time() - t0)))
        else:
            srv.watch(measure)
        srv.cmd(f"pbot mark end:{label}")
        row["load_end"] = round(os.getloadavg()[0], 2)
        row["window"] = ticklog.window(tick_file, f"start:{label}", f"end:{label}")
        if sc.get("slices"):
            row["slices"] = ticklog.slices(tick_file, f"start:{label}", f"end:{label}", sc["slices"])
        if sc.get("jfr", True):
            os.makedirs(os.path.join(run.dir, "jfr"), exist_ok=True)
            row["jfr"] = srv.jfr_dump(os.path.join(run.dir, "jfr", f"{sid}-{attempt}.jfr"), time.time() - t0 + 5)
        if sc.get("census", True):
            row["census"] = srv.cmd(f"pbot census {sid}").strip()
            src = os.path.join(srv.srv, f"perf/bot/census-{sid}.json")
            if os.path.exists(src):
                os.makedirs(os.path.join(run.dir, "census"), exist_ok=True)
                shutil.copy2(src, os.path.join(run.dir, "census", f"{sid}-{attempt}.json"))
        send_all(srv, as_list(sc.get("teardown"), ctx), run, sid)
        return row
    except ServerEnded as e:
        row["status"] = "server_died"
        row["ending"] = e.info
        if tick_file:
            row["window"] = ticklog.window(tick_file, f"start:{label}")
        return row
    finally:
        ev, contended = srv.take_events()
        row["contended"] = contended
        if contended:
            row["machine"] = [x for x in ev if "load" not in x][:20]
        row["t_end"] = int(time.time())


def copy_group_logs(srv, gdir):
    d = os.path.join(gdir, "ticks")
    os.makedirs(d, exist_ok=True)
    for p in glob.glob(os.path.join(srv.srv, "perf/bot/ticks-*.csv")):
        shutil.copy2(p, d)
    for p in ("logs/latest.log", "console.log"):
        q = os.path.join(srv.srv, p)
        if os.path.exists(q):
            shutil.copy2(q, os.path.join(gdir, os.path.basename(p)))


def run_group(run, group, opts):
    gid = group["id"]
    gs = run.state["groups"].setdefault(gid, {"status": "pending"})
    if gs["status"] in ("done", "boot_failed"):
        return
    gdir = os.path.join(run.dir, gid)
    os.makedirs(gdir, exist_ok=True)
    srv = Server(gdir, lambda t: run.log(f"{gid}: {t}"), lambda: STOP["flag"], hang_s=opts.get("hang_s", 120))
    run.group_init = group.get("init")
    if not gs.get("setup_done"):
        free = shutil.disk_usage(run.dir).free / 2 ** 30
        if free < MIN_FREE_GB:
            raise Aborted(f"only {free:.1f} GB free (need {MIN_FREE_GB})")
        info = srv.setup(f"{group['level']}_{run.name}", group["world"], props=group.get("props"),
                         watchdog=group.get("watchdog", True))
        gs.update(setup_done=True, setup=info)
        run.save()
        run.log(f"{gid}: setup {info}")
    scale = run.state["options"].get("scale", 1)
    fails_in_row = 0
    scenarios = [s for s in group["scenarios"] if selected(s, run.state["options"])]
    i = 0
    while i < len(scenarios):
        sc = scenarios[i]
        ss = run.state["scenarios"].setdefault(sc["id"], {"status": "pending", "attempts": 0})
        if ss["status"] in ("ok", "failed"):
            i += 1
            continue
        if not srv.alive():
            first = not gs.get("boots")
            info = boot_with_retries(run, srv, gid, gs)
            if info is None:
                gs["status"] = "boot_failed"
                for s in scenarios[i:]:
                    run.state["scenarios"].setdefault(s["id"], {"attempts": 0})["status"] = "skipped_boot_failed"
                run.save()
                copy_group_logs(srv, gdir)
                return
            if first:
                run.result({"id": f"G01-{gid}", "title": "boot, new world", "group": gid, "status": "ok", **info})
            try:
                init_world(run, srv, group, gs, scale)
            except ServerEnded as e:
                run.result({"id": f"{gid}-init", "group": gid, "status": "server_died", "ending": e.info})
                fails_in_row += 1
                if fails_in_row >= 3:
                    gs["status"] = "boot_failed"
                    run.save()
                    return
                continue
        ctx = Ctx(gs, scale)
        ss["attempts"] += 1
        run.save()
        run.log(f"{gid}: {sc['id']} attempt {ss['attempts']} ({sc.get('title')})")
        row = run_scenario(run, srv, gid, sc, ctx, ss["attempts"])
        run.result(row)
        if row["status"] == "ok":
            ss["status"] = "ok"
            fails_in_row = 0
            i += 1
        else:
            fails_in_row += 1
            if ss["attempts"] > sc.get("retries", 1):
                ss["status"] = "failed"
                i += 1
        run.save()
    srv.stop()
    copy_group_logs(srv, gdir)
    if not opts.get("keep_srv"):
        shutil.rmtree(srv.srv, ignore_errors=True)
    gs["status"] = "done"
    run.save()
    run.log(f"{gid}: done")


def selected(sc, options):
    only = options.get("only")
    if only:
        return sc["id"] in only
    return sc.get("enabled", True) or options.get("include_disabled")


def plan_groups(plan, options):
    mod = importlib.import_module(plan)
    groups = mod.GROUPS
    if options.get("groups"):
        groups = [g for g in groups if g["id"] in options["groups"]]
    return groups


def execute(run):
    run.lock()
    for s in (signal.SIGTERM, signal.SIGINT, signal.SIGHUP):
        signal.signal(s, on_signal)
    opts = run.state["options"]
    try:
        for group in plan_groups(run.state["plan"], opts):
            run_group(run, group, opts)
        run.state["status"] = "done"
        run.log("run done")
    except Aborted as e:
        run.state["status"] = f"aborted: {e}"
        run.log(f"aborted: {e} (resume with: perf_suite.py resume {run.name})")
    except Exception as e:  # keep the state resumable whatever broke
        run.state["status"] = f"error: {e!r}"
        run.log(f"error: {e!r}")
        raise
    finally:
        run.save()
        # never leave our JVM behind after an abort or a runner error
        for gid in run.state["groups"]:
            s = Server(os.path.join(run.dir, gid), run.log, lambda: False)
            s.pid = s.find_pid()
            if s.pid:
                s._try_rcon()
                s.stop()
        run.unlock()


def cmd_run(a):
    run = Run(a.run)
    if os.path.exists(run.state_path):
        sys.exit(f"run {a.run} exists; use resume, or pick a new name")
    os.makedirs(run.dir, exist_ok=True)
    importlib.import_module(a.plan)  # fail early on a bad plan
    run.state = {"plan": a.plan, "created": int(time.time()), "status": "running", "groups": {}, "scenarios": {},
                 "options": {"groups": a.groups.split(",") if a.groups else None,
                             "only": a.only.split(",") if a.only else None, "scale": a.scale,
                             "include_disabled": a.include_disabled, "hang_s": a.hang_s, "keep_srv": a.keep_srv}}
    run.save()
    execute(run)


def cmd_resume(a):
    run = Run(a.run)
    run.load()
    run.state["status"] = "running"
    run.log("resume")
    execute(run)


def cmd_status(a):
    run = Run(a.run)
    run.load()
    st = run.state
    print(f"{a.run}: plan {st['plan']}, {st['status']}")
    for gid, g in st["groups"].items():
        print(f"  group {gid}: {g['status']}, boots {len(g.get('boots', []))}, pregen {g.get('pregen_s')}")
    for sid, s in st["scenarios"].items():
        print(f"  {sid}: {s['status']} ({s['attempts']} attempts)")


def read_results(run_dir):
    rows = []
    p = os.path.join(run_dir, "results.jsonl")
    if os.path.exists(p):
        with open(p) as fh:
            rows = [json.loads(l) for l in fh if l.strip()]
    return rows


def cmd_report(a):
    run = Run(a.run)
    path, out = write_summary(run.dir, a.run, a.baseline)
    print(path)
    print("\n".join(out[:60]))


def write_summary(run_dir, name, baseline=None):
    """summary.md from every row of results.jsonl; the runner rewrites it after each result."""
    rows = read_results(run_dir)
    base = {}
    if baseline:
        for r in read_results(os.path.join(RUNS, baseline)):
            if r.get("status") == "ok" and r.get("window"):
                base[r["id"]] = r["window"]
    out = [f"# Perf suite run {name}", "", "| ID | Scenario | Status | MSPT mean | p95 | p99 | max | >50 ms | TPS | "
           "entities | heap floor MB | extra | vs baseline (mean / p95) |", "|---|---|---|---|---|---|---|---|---|---|---|---|---|"]
    for r in rows:
        w = r.get("window") or {}
        extra = []
        for k in ("wall_s", "save_flush_s", "reload_cmd_s", "reload_log_quiet_s"):
            if k in r and k != "wall_s":
                extra.append(f"{k} {r[k]}")
        if r.get("done"):
            extra.append(f"boot {r.get('wall_s')} s, {r['done'][:40]}")
        if r.get("boots"):
            extra.append("restarts " + ", ".join(f"{b.get('wall_s')} s" for b in r["boots"]))
        if r.get("contended"):
            extra.append("CONTENDED")
        if w.get("cut_short"):
            extra.append("window cut short")
        if r.get("ending"):
            extra.append("ended: " + str(r["ending"].get("cause")) + " " + "; ".join(r["ending"].get("found", [])))
        b = base.get(r["id"])
        vs = ""
        if b and w.get("mspt_mean") is not None and b.get("mspt_mean"):
            vs = f"{w['mspt_mean'] - b['mspt_mean']:+.2f} / {(w.get('mspt_p95') or 0) - (b.get('mspt_p95') or 0):+.2f} ms"
        out.append(f"| {r['id']} | {r.get('title', '')} | {r['status']} | {w.get('mspt_mean', '')} | "
                   f"{w.get('mspt_p95', '')} | {w.get('mspt_p99', '')} | {w.get('mspt_max', '')} | "
                   f"{w.get('over_50ms', '')} | {w.get('tps', '')} | {w.get('entities_mean', '')} | "
                   f"{w.get('heap_floor_mb', '')} | {'; '.join(extra)} | {vs} |")
    for r in rows:
        if r.get("slices"):
            out += ["", f"## {r['id']} slices ({r.get('title')})", "", "| from s | MSPT mean | p95 | max | heap floor MB | entities |",
                    "|---|---|---|---|---|---|"]
            for s in r["slices"]:
                out.append(f"| {s['from_s']} | {s['mspt_mean']} | {s['mspt_p95']} | {s['mspt_max']} | "
                           f"{s.get('heap_floor_mb', '')} | {s.get('entities_mean', '')} |")
    path = os.path.join(run_dir, "summary.md")
    with open(path + ".tmp", "w") as fh:
        fh.write("\n".join(out) + "\n")
    os.replace(path + ".tmp", path)
    return path, out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="what", required=True)
    r = sub.add_parser("run")
    r.add_argument("plan")
    r.add_argument("run")
    r.add_argument("--groups")
    r.add_argument("--only")
    r.add_argument("--scale", type=int, default=1)
    r.add_argument("--include-disabled", action="store_true")
    r.add_argument("--hang-s", type=int, default=120)
    r.add_argument("--keep-srv", action="store_true")
    r.set_defaults(fn=cmd_run)
    for name, fn in (("resume", cmd_resume), ("status", cmd_status)):
        p = sub.add_parser(name)
        p.add_argument("run")
        p.set_defaults(fn=fn)
    p = sub.add_parser("report")
    p.add_argument("run")
    p.add_argument("--baseline")
    p.set_defaults(fn=cmd_report)
    a = ap.parse_args()
    a.fn(a)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Runs a scratch-queue batch file against a scratch server or a throwaway client and records a result per row.

    qrun.py run OUT BATCH RES [--rows ID,ID,...] [--from ID]    server: OUT = scratch_server.sh dir (OUT/srv)
    qrun.py client GAME BATCH RES [--rows ...] [--from ID]      client: GAME = eval_client.py game dir, window up
    qrun.py list BATCH                                          row ids, titles and kinds
    qrun.py toml FILE SECTION KEY VALUE                         set KEY under [SECTION] (added if absent)

Batch format (one directive per line, '#' comments, blank lines ignored):
    ## ID | title                 starts a row
    > command                     console command (server) or chat command typed into the window (client)
    sleep N                       wait N seconds
    waitlog REGEX [SECS]          wait until the row's log segment matches (default 120 s); FAIL on timeout
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
    pace N                        seconds after each command for the rest of the batch (default 2)
    repeat N ... end              the steps between run N times (no nesting)
    capture NAME REGEX            last segment line matching REGEX sets {NAME} (group 1), {NAME1}..{NAMEn} (groups);
                                  {NAME}, {NAME+N}, {NAME-N} in later commands, shell lines and waitlog patterns are replaced;
                                  kept in RES/vars.json for later batches and boots; FAIL if nothing matches. Locate: capture P is at \[(-?\d+), (~|-?\d+), (-?\d+)\]
    shell CMD                     bash CMD run in OUT/srv (server) or GAME (client), e.g. to edit a scratch file;
                                  $INST = the instance root

Every row is also a perf segment: when a perf session is open (tools/perf/session/perf_session.sh) the row ID is
marked there; RES/timeline.tsv always gets the wall-clock start and end of each row, so a JFR recording (server or
client) can be cut to a row later. Results: RES/results.json (merged across runs), RES/results.md, RES/rows/ID.log
(the row's log segment). Regexes are Python, matched per line.
"""
import json
import os
import re
import subprocess
import sys
import time

INST = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
SCRATCH = os.path.join(INST, "tools/creative_labels_test/scratch_server.sh")
PERF = os.path.join(INST, "tools/perf/session/perf_session.sh")
OPS = ("==", ">=", "<=", ">", "<")


def parse(path):
    rows, pace, cur, rep = [], 2.0, None, None
    with open(path) as f:
        for n, raw in enumerate(f, 1):
            line = raw.rstrip("\n")
            s = line.strip()
            if not s or (s.startswith("#") and not s.startswith("## ")):
                continue
            if s.startswith("## "):
                rid, _, title = s[3:].partition("|")
                cur = {"id": rid.strip(), "title": title.strip(), "steps": []}
                rows.append(cur)
                continue
            if s.startswith("pace "):
                pace = float(s.split()[1])
                continue
            if cur is None:
                sys.exit(f"{path}:{n}: directive before the first row")
            if s.startswith("repeat "):
                rep = (int(s.split()[1]), len(cur["steps"]))
                continue
            if s == "end":
                if rep is None:
                    sys.exit(f"{path}:{n}: end without repeat")
                times, at = rep
                cur["steps"].extend(cur["steps"][at:] * (times - 1))
                rep = None
                continue
            if s.startswith("> "):
                cur["steps"].append(("cmd", s[2:], pace))
            else:
                word, _, rest = s.partition(" ")
                if word not in ("capture", "shell", "sleep", "waitlog", "expect", "reject", "count", "bootexpect", "bootreject",
                                "kjsexpect", "kjsreject", "look", "shot", "key", "click"):
                    sys.exit(f"{path}:{n}: unknown directive {word!r}")
                cur["steps"].append((word, rest.strip(), pace))
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
        time.sleep(0.4)
        self._x("type", "--window", self.w, "--delay", "15", "--", c)
        self._x("key", "--window", self.w, "Return")

    def shot(self, path):
        subprocess.run(["import", "-window", self.w, path], check=False)
        return True

    def key(self, k):
        self._x("windowactivate", "--sync", self.w)
        self._x("key", "--window", self.w, *k.split())

    def click(self, x, y):
        self._x("mousemove", "--window", self.w, x, y, "click", "1")


def perf_mark(label):
    if os.path.exists(os.path.join(INST, "perf/sessions/CURRENT")):
        subprocess.run(["bash", PERF, "mark", label], capture_output=True)


def compare(n, op, m):
    return {"==": n == m, ">=": n >= m, "<=": n <= m, ">": n > m, "<": n < m}[op]


VARS = {}


def sub(s):
    def one(m):
        v = VARS.get(m.group(1))
        if v is None:
            return m.group(0)
        return str(int(v) + int(m.group(2))) if m.group(2) else v
    return re.sub(r"\{([A-Za-z_][A-Za-z_0-9]*)([+-]\d+)?\}", one, s)


def run_row(t, row, res):
    off = t.log.size()
    fails, looks, shots = [], [], []
    t0 = time.time()
    perf_mark(row["id"])
    for word, arg, pace in row["steps"]:
        if word in ("cmd", "shell", "waitlog"):
            arg = sub(arg)
        if word == "capture":
            name, _, pat = arg.partition(" ")
            hit = [m for m in (re.search(pat, l) for l in t.log.since(off)) if m]
            if not hit:
                fails.append(f"capture {name}: no line matches /{pat}/")
            else:
                g = hit[-1].groups() or (hit[-1].group(0),)
                VARS[name] = g[0]
                for i, v in enumerate(g, 1):
                    VARS[f"{name}{i}"] = v
        elif word == "cmd":
            t.cmd(arg)
            time.sleep(pace)
        elif word == "shell":
            r = subprocess.run(["bash", "-c", arg], cwd=t.dir, capture_output=True, text=True,
                               env=dict(os.environ, INST=INST))
            if r.returncode:
                fails.append(f"shell exit {r.returncode}: {(r.stderr or r.stdout).strip()[:200]}")
        elif word == "sleep":
            time.sleep(float(arg))
        elif word == "waitlog":
            parts = arg.rsplit(" ", 1)
            pat, secs = (parts[0], float(parts[1])) if len(parts) == 2 and parts[1].isdigit() else (arg, 120.0)
            rx, end = re.compile(pat), time.time() + secs
            while not any(rx.search(l) for l in t.log.since(off)):
                if time.time() > end:
                    fails.append(f"timeout {int(secs)} s waiting for /{pat}/")
                    break
                time.sleep(1)
        elif word == "look":
            looks.append(arg)
        elif word == "shot":
            p = os.path.join(res, "shots", f"{row['id']}_{arg}.png")
            os.makedirs(os.path.dirname(p), exist_ok=True)
            if t.shot(p):
                shots.append(os.path.relpath(p, res))
        elif word == "key":
            t.key(arg)
            time.sleep(1)
        elif word == "click":
            x, y = arg.split()
            t.click(x, y)
            time.sleep(1)
    time.sleep(2)
    seg = t.log.since(off)
    whole = None
    for word, arg, _ in row["steps"]:
        if word in ("expect", "reject"):
            hit = [l for l in seg if re.search(arg, l)]
            if word == "expect" and not hit:
                fails.append(f"no line matches /{arg}/")
            if word == "reject" and hit:
                fails.append(f"/{arg}/ matched: {hit[0].strip()[:200]}")
        elif word == "count":
            m = re.match(r"(.+)\s+(==|>=|<=|>|<)\s+(\d+)$", arg)
            if not m:
                fails.append(f"bad count directive: {arg}")
                continue
            n = sum(1 for l in seg if re.search(m.group(1), l))
            if not compare(n, m.group(2), int(m.group(3))):
                fails.append(f"count /{m.group(1)}/ = {n}, wanted {m.group(2)} {m.group(3)}")
        elif word in ("bootexpect", "bootreject", "kjsexpect", "kjsreject"):
            kjs = word.startswith("kjs")
            src = t.kjs.since(0) if kjs else (whole if whole is not None else t.log.since(0))
            if not kjs:
                whole = src
            name = "KubeJS log" if kjs else "boot log"
            hit = [l for l in src if re.search(arg, l)]
            if word.endswith("expect") and not hit:
                fails.append(f"{name}: no line matches /{arg}/")
            if word.endswith("reject") and hit:
                fails.append(f"{name}: /{arg}/ matched: {hit[0].strip()[:200]}")
    os.makedirs(os.path.join(res, "rows"), exist_ok=True)
    with open(os.path.join(res, "rows", row["id"] + ".log"), "w") as f:
        f.write("\n".join(seg) + "\n")
    with open(os.path.join(res, "timeline.tsv"), "a") as f:
        f.write(f"{t0:.1f}\t{time.time():.1f}\t{t.kind}\t{row['id']}\n")
    result = "FAIL" if fails else ("LOOK" if looks or shots else "PASS")
    return {"result": result, "fails": fails, "looks": looks, "shots": shots, "title": row["title"],
            "at": time.strftime("%Y-%m-%d %H:%M:%S"), "lines": len(seg)}


def write_results(res, results):
    with open(os.path.join(res, "results.json"), "w") as f:
        json.dump(results, f, indent=1)
    with open(os.path.join(res, "results.md"), "w") as f:
        f.write("| ID | Result | Detail |\n|---|---|---|\n")
        for rid, r in results.items():
            d = "; ".join(r["fails"] + ["look: " + x for x in r["looks"]] + ["shot: " + x for x in r["shots"]])
            f.write(f"| {rid} | {r['result']} | {d.replace('|', '/')} |\n")


def main():
    a = sys.argv[1:]
    if not a:
        sys.exit(__doc__)
    if a[0] == "toml":
        path, section, key, value = a[1:5]
        lines = open(path).read().splitlines()
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
    if a[0] not in ("run", "client") or len(a) < 4:
        sys.exit(__doc__)
    target, batch, res = a[1], a[2], a[3]
    only = set(a[a.index("--rows") + 1].split(",")) if "--rows" in a else None
    start = a[a.index("--from") + 1] if "--from" in a else None
    os.makedirs(res, exist_ok=True)
    t = Server(target) if a[0] == "run" else Client(target)
    rp = os.path.join(res, "results.json")
    results = json.load(open(rp)) if os.path.exists(rp) else {}
    vp = os.path.join(res, "vars.json")
    if os.path.exists(vp):
        VARS.update(json.load(open(vp)))
    started = start is None
    for row in parse(batch):
        if not started:
            started = row["id"] == start
            if not started:
                continue
        if only and row["id"] not in only:
            continue
        print(f"{row['id']} ...", end=" ", flush=True)
        try:
            r = run_row(t, row, res)
        except RuntimeError as e:
            print(f"ABORT: {e}")
            break
        results[row["id"]] = r
        write_results(res, results)
        with open(vp, "w") as f:
            json.dump(VARS, f, indent=1)
        print(r["result"], "; ".join(r["fails"])[:200])
    write_results(res, results)
    n = {}
    for r in results.values():
        n[r["result"]] = n.get(r["result"], 0) + 1
    print("totals:", ", ".join(f"{k} {v}" for k, v in sorted(n.items())), "->", os.path.join(res, "results.md"))


if __name__ == "__main__":
    main()

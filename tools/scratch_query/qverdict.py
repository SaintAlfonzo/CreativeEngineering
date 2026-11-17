#!/usr/bin/env python3
"""Per-row verdict files (TX-45): qrun writes RES/verdicts/ID.json for every row it runs; the judge step only fills
the row's LOOKs; run-doc verdict tables are generated from the files. Called through qrun.py:

    qrun.py judge RES                                   list the open LOOKs (row, look number, text, shots, log)
    qrun.py judge RES ID N VERDICT EVIDENCE [--by WHO] [--text]
                                                        fill look N (1-based) of row ID
    qrun.py judge RES --file TSV [--by WHO]             same from a file: ID<TAB>N<TAB>VERDICT<TAB>EVIDENCE[<TAB>text]
    qrun.py table RES [RES ...] [--ids ID,..] [--open]  markdown | ID | Result | Evidence | for the run doc, then counts
                                                        (--open: rows not PASS; a later RES wins for a rerun row)

VERDICT for a look: PASS, FAIL, BATCH-FAULT, INCONCLUSIVE, or OWNER (left for the owner). WHO: agent (default),
owner, or a name. A row with shots holds pixels: per the item 2 judgement rule an agent may not give it PASS or FAIL
unless the evidence is text (--text, or a 5th TSV column "text": the log, a shell file, a READ line); otherwise its
look stays OWNER. Checks (expect, waitlog, count, shell, ...) are qrun's and are never changed here.

Row verdict (field "verdict"): CRASH if the game crashed in the row; FAIL if any check failed; LOOK while a look is
open; else the worst look verdict (FAIL > BATCH-FAULT > INCONCLUSIVE > OWNER > PASS); PASS with no looks.

File: {"id", "title", "batch", "mode", "result" (qrun's), "verdict", "started", "ended", "checks": [{"step", "arg",
"ok", "detail"}], "looks": [{"text", "verdict", "evidence", "by", "at"}], "shots", "log", "shell", "lines",
"crash": null | {"reason", "evidence_dir", "files"}, "previous": [{"result", "verdict", "at", "batch"}]}.
"""
import csv
import json
import os
import sys
import time

LOOK_VERDICTS = ("PASS", "FAIL", "BATCH-FAULT", "INCONCLUSIVE", "OWNER")
RANK = {"FAIL": 5, "BATCH-FAULT": 4, "INCONCLUSIVE": 3, "OWNER": 2, "PASS": 1}


def path(res, rid):
    return os.path.join(res, "verdicts", rid + ".json")


def verdict_of(d):
    if d.get("crash"):
        return "CRASH"
    if any(not c["ok"] for c in d["checks"]):
        return "FAIL"
    if any(l["verdict"] is None for l in d["looks"]):
        return "LOOK"
    if not d["looks"]:
        return "PASS"
    return max((l["verdict"] for l in d["looks"]), key=RANK.get)


def write(res, row, r, checks, batch, mode, crash=None):
    """Called by qrun after each row. r: qrun's result dict (result, looks, shots, lines, started, ended)."""
    os.makedirs(os.path.join(res, "verdicts"), exist_ok=True)
    p = path(res, row["id"])
    prev = []
    if os.path.exists(p):
        old = json.load(open(p))
        prev = old.get("previous", []) + [{k: old.get(k) for k in ("result", "verdict", "batch")} |
                                          {"at": old.get("ended_at")}]
    looks = [{"text": x, "verdict": None, "evidence": None, "by": None, "at": None} for x in r.get("looks", [])]
    if r.get("shots") and not looks:
        looks.append({"text": "judge the shots", "verdict": None, "evidence": None, "by": None, "at": None})
    rid = row["id"]
    d = {"id": rid, "title": row.get("title", ""), "batch": os.path.abspath(batch), "mode": mode,
         "result": r["result"], "verdict": None,
         "started": time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(r.get("started", time.time()))),
         "ended_at": time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(r.get("ended", time.time()))),
         "checks": checks, "looks": looks, "shots": r.get("shots", []),
         "log": f"rows/{rid}.log" if os.path.exists(os.path.join(res, "rows", rid + ".log")) else None,
         "shell": f"rows/{rid}.shell.txt" if os.path.exists(os.path.join(res, "rows", rid + ".shell.txt")) else None,
         "lines": r.get("lines"), "crash": crash, "previous": prev}
    d["verdict"] = verdict_of(d)
    json.dump(d, open(p, "w"), indent=1)
    return d


def load_all(res):
    vd = os.path.join(res, "verdicts")
    out = []
    if os.path.isdir(vd):
        for f in sorted(os.listdir(vd)):
            if f.endswith(".json"):
                out.append(json.load(open(os.path.join(vd, f))))
    return out


def fill(res, rid, n, verdict, evidence, by="agent", text=False):
    p = path(res, rid)
    if not os.path.exists(p):
        return f"{rid}: no verdict file {p}"
    d = json.load(open(p))
    verdict = verdict.upper()
    if verdict not in LOOK_VERDICTS:
        return f"{rid}: verdict must be one of {', '.join(LOOK_VERDICTS)}"
    if not 1 <= n <= len(d["looks"]):
        return f"{rid}: look {n} of {len(d['looks'])}"
    if d["shots"] and by != "owner" and verdict in ("PASS", "FAIL") and not text:
        return (f"{rid}: the row has shots; only the owner judges pixels (plan item 2 rule). Give OWNER, or --text when "
                f"the evidence is log text")
    if not evidence.strip():
        return f"{rid}: evidence is empty"
    d["looks"][n - 1].update(verdict=verdict, evidence=evidence.strip(), by=by, at=time.strftime("%Y-%m-%d %H:%M"))
    d["verdict"] = verdict_of(d)
    json.dump(d, open(p, "w"), indent=1)
    return None


def evidence_of(d):
    parts = []
    if d.get("crash"):
        parts.append(f"CRASH: {d['crash']['reason']} (evidence {d['crash']['evidence_dir']})")
    parts += [c["detail"] for c in d["checks"] if not c["ok"]]
    for i, l in enumerate(d["looks"], 1):
        if l["verdict"]:
            parts.append(f"{l['verdict'] + ': ' if l['verdict'] != d['verdict'] or len(d['looks']) > 1 else ''}"
                         f"{l['evidence']}" + (f" ({l['by']})" if l["by"] and l["by"] != "agent" else ""))
        else:
            parts.append(f"open look {i}: {l['text']}")
    if not parts:
        n = len(d["checks"])
        parts.append(f"{n} check{'s' if n != 1 else ''} passed")
    return "; ".join(parts).replace("|", "/").replace("\n", " ")


def main_judge(a):
    if not a:
        sys.exit(__doc__)
    res = a[0]
    by = a[a.index("--by") + 1] if "--by" in a else "agent"
    text = "--text" in a
    rest = [x for i, x in enumerate(a[1:], 1) if x not in ("--by", "--text", "--file") and a[i - 1] not in ("--by", "--file")]
    if "--file" in a:
        errs = 0
        with open(a[a.index("--file") + 1], newline="") as f:
            for row in csv.reader(f, delimiter="\t"):
                if not row or row[0].startswith("#"):
                    continue
                e = fill(res, row[0], int(row[1]), row[2], row[3], by, text or (len(row) > 4 and row[4] == "text"))
                if e:
                    print(e)
                    errs += 1
        print(f"judge: {'errors ' + str(errs) if errs else 'all filled'}")
        return 1 if errs else 0
    if len(rest) >= 4:
        e = fill(res, rest[0], int(rest[1]), rest[2], " ".join(rest[3:]), by, text)
        print(e or f"{rest[0]}: look {rest[1]} = {rest[2].upper()}")
        return 1 if e else 0
    n = 0
    for d in load_all(res):
        for i, l in enumerate(d["looks"], 1):
            if l["verdict"] is None and d["verdict"] != "CRASH":
                n += 1
                extra = []
                if d["shots"]:
                    extra.append("shots " + " ".join(d["shots"]))
                if d["log"]:
                    extra.append(d["log"])
                if d["shell"]:
                    extra.append(d["shell"])
                failed = " [row already FAIL]" if d["verdict"] == "FAIL" else ""
                print(f"{d['id']}\t{i}\t{l['text']}\t{'; '.join(extra)}{failed}")
    print(f"open looks: {n}")
    return 0


def main_table(a):
    ids = set(a[a.index("--ids") + 1].split(",")) if "--ids" in a else None
    only_open = "--open" in a
    dirs = [x for i, x in enumerate(a) if not x.startswith("--") and (i == 0 or a[i - 1] != "--ids")]
    if not dirs:
        sys.exit(__doc__)
    rows = {}
    for res in dirs:
        for d in load_all(res):
            rows[d["id"]] = d  # a later RES wins (reruns)
    counts = {}
    print("| ID | Result | Evidence |\n|---|---|---|")
    for rid, d in rows.items():
        if ids and rid not in ids:
            continue
        counts[d["verdict"]] = counts.get(d["verdict"], 0) + 1
        if only_open and d["verdict"] == "PASS":
            continue
        print(f"| {rid} | {d['verdict']} | {evidence_of(d)} |")
    print("\nCounts: " + ", ".join(f"{k} {v}" for k, v in sorted(counts.items(), key=lambda x: -x[1])))
    return 0


if __name__ == "__main__":
    w = sys.argv[1] if len(sys.argv) > 1 else ""
    if w == "judge":
        sys.exit(main_judge(sys.argv[2:]))
    if w == "table":
        sys.exit(main_table(sys.argv[2:]))
    sys.exit(__doc__)

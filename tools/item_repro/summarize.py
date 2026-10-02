#!/usr/bin/env python3
"""Summarizes an item-loss repro run.

    python3 summarize.py SERVER_LATEST_LOG CLIENT_KUBEJS_LOG [--detail]

Per phase (the part of the round label before "-r"):
  rounds    CHECKs the server answered
  events    rounds where the item totals changed from the previous CHECK (each is one new loss/dupe)
  lost      CHECKs where the server's item totals differ from the setup baseline (real loss/dupe)
  deltas    DELTA lines that moved away from baseline (each is a change the ledger saw)
  desync    rounds where the client's view of the open menu (CVIEW) differs from the
            server's (SVIEW) after settling: a visual-only "deletion"
"""
import re
import sys
from collections import defaultdict

srv_log, cli_log = sys.argv[1], sys.argv[2]
detail = "--detail" in sys.argv

checks, sview, cview = {}, {}, {}
deltas = defaultdict(list)
for line in open(srv_log, errors="replace"):
    m = re.search(r"\[imrepro\] CHECK (\S+) tick=\d+ (.*)$", line)
    if m:
        checks[m.group(1)] = m.group(2).strip()
    m = re.search(r"\[imrepro\] SVIEW (\S+) (.*)$", line)
    if m:
        sview[m.group(1)] = m.group(2).strip()
    m = re.search(r"\[imrepro\] DELTA tick=(\d+) mark=(\S+) (.*)$", line)
    if m:
        deltas[m.group(2)].append((int(m.group(1)), m.group(3).strip()))
for line in open(cli_log, errors="replace"):
    m = re.search(r"\[imrepro\] CVIEW (\S+) (.*)$", line)
    if m:
        cview[m.group(1)] = m.group(2).strip()


def phase(label):
    return label.rsplit("-r", 1)[0]


def slots(view):
    head, _, rest = view.partition(" carried=")
    carried, _, slots_part = rest.partition(" ")
    return head, carried, set(slots_part.split())


stats = defaultdict(lambda: defaultdict(int))
for label, res in checks.items():
    ph = phase(label)
    stats[ph]["rounds"] += 1
    if res.startswith("MISMATCH"):
        stats[ph]["lost"] += 1
        if detail:
            print(f"LOST   {label}: {res}")
    if label in cview and label in sview:
        c, s = slots(cview[label]), slots(sview[label])
        if c != s:
            stats[ph]["desync"] += 1
            if detail:
                print(f"DESYNC {label}: client-only {sorted(c[2] - s[2])} server-only {sorted(s[2] - c[2])}"
                      f" carried c={c[1]} s={s[1]} menus c={c[0]} s={s[0]}")
for mark, rows in deltas.items():
    for _, text in rows:
        if not text.startswith("back to baseline"):
            stats[phase(mark)]["deltas"] += 1
            if detail:
                print(f"DELTA  {mark}: {text[:300]}")

# A loss persists, so every later CHECK repeats it. An "event" is a round whose CHECK result
# differs from the previous round's: a new loss (or dupe) happened during that round.
def parse(res):
    out = {}
    if res.startswith("MISMATCH "):
        for part in re.split(r", (?=[+-]\d+ )", res[len("MISMATCH "):]):
            n, _, key = part.partition(" ")
            out[key] = int(n)
    return out


prev = {}
for label, res in checks.items():  # dicts keep log order
    cur = parse(res)
    if cur != prev:
        change = {k: cur.get(k, 0) - prev.get(k, 0) for k in set(cur) | set(prev)}
        change = {k: v for k, v in change.items() if v}
        stats[phase(label)]["events"] += 1
        if detail:
            print(f"EVENT  {label}: " + ", ".join(f"{v:+d} {k}" for k, v in sorted(change.items()))[:400])
    prev = cur

print(f"{'phase':<16}{'rounds':>8}{'events':>8}{'lost':>6}{'deltas':>8}{'desync':>8}")
for ph in sorted(stats):
    s = stats[ph]
    print(f"{ph:<16}{s['rounds']:>8}{s['events']:>8}{s['lost']:>6}{s['deltas']:>8}{s['desync']:>8}")

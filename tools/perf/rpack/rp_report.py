#!/usr/bin/env python3
"""Report for a resource pack bench run (feature_writeup/perf_rpack.md section 5).

    python3 tools/perf/rpack/rp_report.py RES

RES is rp_bench.py's results dir: client_kubejs.log ("[rpbench] <kind> {json}" lines), client_latest.log (atlas
sizes per reload), plan.json and rpack-scan.json. Writes RES/rpack-report.txt and RES/rpack-report.json.

A pack's cost is "all" minus "-<pack>": reload time against the mean of every "all" reload, frame time and chunk
rebuild in the pack's own scene against the "all" full sweeps. Noise is the spread of the "all" reloads and of the
control scene across every "all" config; a cost counts only above 2 sd and above the floors below.
"""
import json
import os
import re
import statistics as st
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rp_bench import short  # noqa: E402

LINE = re.compile(r"\[rpbench\] (\w+) (\{.*\})\s*$")
ATLAS = re.compile(r"^\[(\d\d):(\d\d):(\d\d)\].*Created: (\d+)x(\d+)x(\d+) (\S+)-atlas")
MIPW = re.compile(r"^\[(\d\d):(\d\d):(\d\d)\].*Texture (\S+) with size (\d+x\d+) limits mip level from (\d+) to (\d+)")
RELOAD_FLOOR_MS = 1000
FRAME_FLOOR = 0.03
REBUILD_FLOOR = 0.10


def secs(h, m, s):
    return int(h) * 3600 + int(m) * 60 + int(s)


def wall(t):
    return secs(*t.split(":"))


def mean(xs):
    return st.mean(xs) if xs else None


def sd(xs):
    return st.stdev(xs) if len(xs) > 1 else 0.0


def load(res):
    ev = []
    for line in open(os.path.join(res, "client_kubejs.log"), errors="replace"):
        m = LINE.search(line)
        if m:
            try:
                ev.append((m[1], json.loads(m[2])))
            except ValueError:
                pass
    atlases, mips = [], []
    lp = os.path.join(res, "client_latest.log")
    if os.path.exists(lp):
        for line in open(lp, errors="replace"):
            m = ATLAS.match(line)
            if m:
                atlases.append((secs(m[1], m[2], m[3]), m[7], int(m[4]), int(m[5]), int(m[6])))
                continue
            m = MIPW.match(line)
            if m:
                mips.append((secs(m[1], m[2], m[3]), m[4], m[5], int(m[6]), int(m[7])))
    return ev, atlases, mips


def attach_atlases(reloads, atlases, mips):
    for r in reloads:
        if "wall0" not in r:
            continue
        a, b = wall(r["wall0"]), wall(r["wall1"])
        b = b + 86400 if b < a else b
        inside = lambda t: a - 1 <= t <= b + 2 or a - 1 <= t + 86400 <= b + 2
        r["atlases"] = {n: [w, h, mp] for t, n, w, h, mp in atlases if inside(t)}
        r["mip_limits"] = [f"{tex} {size} {f}->{to}" for t, tex, size, f, to in mips if inside(t)]


def block_atlas(r):
    a = (r.get("atlases") or {}).get("minecraft:textures/atlas/blocks.png")
    return f"{a[0]}x{a[1]}" if a else "-"


def vram_mb(r):
    tot = 0
    for w, h, mp in (r.get("atlases") or {}).values():
        tot += w * h * 4 * (4 / 3 if mp else 1)
    return tot / 1048576


def pct(a, b):
    return (a - b) / b if a is not None and b else None


def fmt(x, f="{:+.1%}"):
    return "-" if x is None else f.format(x)


def main(res):
    ev, atlases, mips = load(res)
    plan = json.load(open(os.path.join(res, "plan.json")))
    scan = {}
    sp = os.path.join(res, "rpack-scan.json")
    if os.path.exists(sp):
        scan = {p["id"]: p for p in json.load(open(sp))["packs"]}
    reloads = [o for k, o in ev if k == "reload"]
    scenes = [o for k, o in ev if k == "scene"]
    attach_atlases(reloads, atlases, mips)
    problems = [(k, o) for k, o in ev if k in ("error", "timeout")]
    built = {}
    for k, o in ev:
        if k == "built":
            built.setdefault(o["scene"], o)
    site = next((o for k, o in ev if k == "site"), None)
    done = any(k == "done" for k, _ in ev)
    cfg_meta = plan["configs"]

    out = ["# Resource pack bench report", "",
           f"Configs run {len(reloads)} of {len(cfg_meta)}{'' if done else ' (bench did not finish)'}; "
           f"scene samples {len(scenes)}; errors/timeouts {len(problems)}; site check "
           f"{'void' if site and site['nonAirSamples'] == 0 else (site and str(site['nonAirSamples']) + ' non-air samples (scenes not clean)') or 'not run'}"]
    for k, o in problems[:15]:
        out.append(f"  {k}: {json.dumps(o)[:200]}")
    for n, b in built.items():
        if b.get("missing") or b.get("badEntities"):
            out.append(f"  scene {n}: unknown blocks {b.get('missing')[:8]} entities {b.get('badEntities')}")

    base = [r["ms"] for r in reloads if r["config"] == "all"]
    bm, bs = mean(base), sd(base)
    out += ["", "## Reloads", "",
            f"all: {len(base)} reloads, mean {bm / 1000 if bm else 0:.1f} s, sd {bs / 1000:.1f} s", "",
            f"{'config':44} {'reload s':>8} {'vs all':>7} {'blocks atlas':>12} {'atlas MB':>8}"]
    for r in reloads:
        d = (r["ms"] - bm) / 1000 if bm else None
        out.append(f"{r['config'][:44]:44} {r['ms'] / 1000:>8.1f} {fmt(d, '{:+.1f}'):>7} {block_atlas(r):>12} {vram_mb(r):>8.0f}")

    def scene_vals(cfg, name, key="mean_ms", drift=None):
        return [s[key] for s, c in scene_cfgs if c["name"] == cfg and s["scene"] == name
                and (drift is None or bool(c.get("drift")) == drift)]
    # pair each scene sample with its config entry (scenes arrive in config order)
    scene_cfgs, i = [], -1
    for k, o in ev:
        if k == "reload":
            i = o["index"]
        elif k == "scene" and 0 <= i < len(cfg_meta):
            scene_cfgs.append((o, cfg_meta[i]))
    rb = lambda s: mean(s["rebuild_ms"]) if s.get("rebuild_ms") else None

    ctl = scene_vals("all", "control")
    drift = sd(ctl) / mean(ctl) if len(ctl) > 1 and mean(ctl) else 0.0
    vis = scene_vals("all", "vista")
    out += ["", "## Drift (control scene in every \"all\" config)", "",
            f"control mean frame ms: {', '.join(f'{x:.2f}' for x in ctl) or '-'}; sd {drift:.1%} of mean",
            f"vista mean frame ms:   {', '.join(f'{x:.2f}' for x in vis) or '-'}"]

    out += ["", "## All vs none (every scene)", "",
            f"{'scene':40} {'all ms':>7} {'none ms':>7} {'frame':>7} {'all p99':>7} {'none p99':>8} "
            f"{'rebuild all':>11} {'none':>6}"]
    for name in plan["scenes"]:
        a_s = [s for s, c in scene_cfgs if c["name"] == "all" and not c.get("drift") and s["scene"] == name]
        n_s = [s for s, c in scene_cfgs if c["name"] == "none" and s["scene"] == name]
        am, nm = mean([s["mean_ms"] for s in a_s]), mean([s["mean_ms"] for s in n_s])
        ap, np_ = mean([s["p99_ms"] for s in a_s]), mean([s["p99_ms"] for s in n_s])
        ar = mean([x for x in (rb(s) for s in a_s) if x is not None])
        nr = mean([x for x in (rb(s) for s in n_s) if x is not None])
        out.append(f"{name[:40]:40} {fmt(am, '{:.2f}'):>7} {fmt(nm, '{:.2f}'):>7} {fmt(pct(am, nm)):>7} "
                   f"{fmt(ap, '{:.2f}'):>7} {fmt(np_, '{:.2f}'):>8} {fmt(ar, '{:.0f}'):>11} {fmt(nr, '{:.0f}'):>6}")

    out += ["", "## Per pack (cost = all minus -pack)", "",
            "reload: s added by the pack; frame / p99 / rebuild: in its own scene; atlas: block atlas without it;",
            f"* = above noise (reload > 2 sd and {RELOAD_FLOOR_MS / 1000:.0f} s; frame > 2 drift sd and "
            f"{FRAME_FLOOR:.0%}; rebuild > {REBUILD_FLOOR:.0%}). Scan columns from rp_scan.py.", "",
            f"{'pack':40} {'reload':>7} {'frame':>7} {'p99':>7} {'rebuild':>7} {'atlas w/o':>10} "
            f"{'scan atlas px':>13} {'anim px/s':>9} {'dead':>5}  verdict"]
    summary = []
    all_atlas = next((block_atlas(r) for r in reloads if r["config"] == "all" and r.get("atlases")), "-")
    for p in plan["targets"]:
        name = "-" + short(p)
        rs = [r for r in reloads if r["config"] == name]
        rcost = (bm - mean([r["ms"] for r in rs])) if rs and bm else None
        scene = next((s for s in plan["scenes"] if plan["scenes"][s].get("pack") == p), None)
        fa = fm = pa = pm = ra = rm = None
        if scene:
            a_s = [s for s, c in scene_cfgs if c["name"] == "all" and s["scene"] == scene]
            m_s = [s for s, c in scene_cfgs if c["name"] == name and s["scene"] == scene]
            fa, fm = mean([s["mean_ms"] for s in a_s]), mean([s["mean_ms"] for s in m_s])
            pa, pm = mean([s["p99_ms"] for s in a_s]), mean([s["p99_ms"] for s in m_s])
            ra = mean([x for x in (rb(s) for s in a_s) if x is not None])
            rm = mean([x for x in (rb(s) for s in m_s) if x is not None])
        fc, pc, rc = pct(fa, fm), pct(pa, pm), pct(ra, rm)
        atlas_wo = block_atlas(rs[0]) if rs else "-"
        flags = []
        if rcost is not None and rcost > max(2 * bs, RELOAD_FLOOR_MS):
            flags.append(f"reload +{rcost / 1000:.1f} s")
        if fc is not None and fc > max(2 * drift, FRAME_FLOOR):
            flags.append(f"frame {fc:+.0%}")
        if rc is not None and rc > REBUILD_FLOOR:
            flags.append(f"rebuild {rc:+.0%}")
        if atlas_wo not in ("-", all_atlas):
            flags.append(f"atlas {all_atlas} (without: {atlas_wo})")
        sc = scan.get(p, {})
        verdict = "* " + "; ".join(flags) if flags else "no measurable cost" if rs else "not run"
        out.append(f"{name[1:][:40]:40} {fmt(rcost and rcost / 1000, '{:+.1f}'):>7} {fmt(fc):>7} {fmt(pc):>7} "
                   f"{fmt(rc):>7} {atlas_wo:>10} {sc.get('atlas_px_delta', '-'):>13} {sc.get('anim_upload_px_s', '-'):>9} "
                   f"{sum(sc['dead'].values()) if sc else '-':>5}  {verdict}"
                   + ("" if scene else " (no scene: " + plan["notes"].get(p, "nothing placeable") + ")"))
        summary.append({"pack": p, "reload_ms": rcost, "frame": fc, "p99": pc, "rebuild": rc, "atlas_without": atlas_wo,
                        "flags": flags})
    txt = "\n".join(out) + "\n"
    open(os.path.join(res, "rpack-report.txt"), "w").write(txt)
    json.dump({"reload_all_ms": base, "drift": drift, "packs": summary}, open(os.path.join(res, "rpack-report.json"), "w"),
              indent=1)
    print(txt)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(os.path.abspath(sys.argv[1]))

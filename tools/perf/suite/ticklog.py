"""Reads the cdevperfbot tick log (perf/bot/ticks-<epoch ms>.csv in the scratch server, format in TickLog.java)
and turns a window between two markers into numbers. A window whose end marker is missing (the server died)
ends at the last tick in the file."""
import glob
import os


def newest(bot_dir):
    files = glob.glob(os.path.join(bot_dir, "ticks-*.csv"))
    return max(files, key=os.path.getmtime) if files else None


def heartbeat_age(bot_dir, now):
    f = newest(bot_dir)
    return None if f is None else now - os.path.getmtime(f)


def read(path):
    ticks, status, marks = [], [], []
    with open(path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            p = line.rstrip("\n").split(",", 3)
            try:
                if p[0] == "t" and len(p) == 3:
                    ticks.append((int(p[1]), int(p[2])))
                elif p[0] == "s":
                    q = line.rstrip("\n").split(",")
                    if len(q) == 7:
                        status.append(tuple(int(x) for x in q[1:]))
                elif p[0] == "m" and len(p) == 4:
                    marks.append((int(p[1]), int(p[2]), p[3]))
            except ValueError:
                pass  # a line cut short by a kill
    return ticks, status, marks


def pct(sorted_vals, q):
    if not sorted_vals:
        return None
    i = min(len(sorted_vals) - 1, max(0, int(round(q * (len(sorted_vals) - 1)))))
    return sorted_vals[i]


def stats(ticks, status, t0_tick, t1_tick, t0_ms=None, t1_ms=None):
    """ticks: [(tick, us)]; status: [(epoch_ms, tick, players, entities, chunks, heap_mb)]."""
    durs = [us / 1000.0 for t, us in ticks if t0_tick < t <= t1_tick]
    st = [s for s in status if t0_tick <= s[1] <= t1_tick]
    if t0_ms is None and st:
        t0_ms = st[0][0]
    if t1_ms is None and st:
        t1_ms = st[-1][0]
    wall = (t1_ms - t0_ms) / 1000.0 if t0_ms and t1_ms and t1_ms > t0_ms else None
    s = sorted(durs)
    out = {
        "ticks": len(durs),
        "wall_s": round(wall, 1) if wall else None,
        "tps": round(len(durs) / wall, 2) if wall else None,
        "mspt_mean": round(sum(durs) / len(durs), 2) if durs else None,
        "mspt_p50": pct(s, 0.50),
        "mspt_p95": pct(s, 0.95),
        "mspt_p99": pct(s, 0.99),
        "mspt_max": s[-1] if s else None,
        "over_50ms": sum(1 for d in durs if d > 50),
    }
    for k in ("mspt_p50", "mspt_p95", "mspt_p99", "mspt_max"):
        if out[k] is not None:
            out[k] = round(out[k], 2)
    if st:
        out["players"] = max(x[2] for x in st)
        out["entities_mean"] = round(sum(x[3] for x in st) / len(st))
        out["entities_max"] = max(x[3] for x in st)
        out["chunks_mean"] = round(sum(x[4] for x in st) / len(st))
        # heap "floor": the lowest used-heap reading, a rough stand-in for live heap after GC (JFR has the real one)
        out["heap_floor_mb"] = min(x[5] for x in st)
        out["heap_peak_mb"] = max(x[5] for x in st)
    return out


def window(path, start_label, end_label=None):
    """Stats between marker start_label and end_label (or the end of the file)."""
    ticks, status, marks = read(path)
    start = next((m for m in reversed(marks) if m[2] == start_label), None)
    if start is None:
        return None
    end = next((m for m in marks if end_label and m[2] == end_label and m[1] >= start[1]), None)
    if end is not None:
        return stats(ticks, status, start[1], end[1], start[0], end[0])
    last_tick = ticks[-1][0] if ticks else start[1]
    out = stats(ticks, status, start[1], last_tick, start[0], None)
    out["cut_short"] = True
    return out


def slices(path, start_label, end_label, slice_s):
    """Per-slice stats inside a window (soak drift)."""
    ticks, status, marks = read(path)
    start = next((m for m in reversed(marks) if m[2] == start_label), None)
    if start is None:
        return []
    end = next((m for m in marks if m[2] == end_label and m[1] >= start[1]), None)
    end_tick = end[1] if end else (ticks[-1][0] if ticks else start[1])
    st = [s for s in status if start[1] <= s[1] <= end_tick]
    out, i = [], 0
    while i < len(st):
        j = i
        while j < len(st) and st[j][0] - st[i][0] < slice_s * 1000:
            j += 1
        if j - i >= 2:
            row = stats(ticks, status, st[i][1], st[j - 1][1], st[i][0], st[j - 1][0])
            row["from_s"] = round((st[i][0] - start[0]) / 1000)
            out.append(row)
        i = j
    return out

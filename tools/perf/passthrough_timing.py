#!/usr/bin/env python3
"""Pass-through hook timing from a log (feature_writeup/plan_passthrough.md 7.3, blockrunner.md 3.10).

    python3 tools/perf/passthrough_timing.py [LOG ...] [--json OUT]

Reads the '[passthrough-timing]' lines Creative Compat writes every 200 ticks while
[debug.passthrough] timing = true (config/creativecompat-common.toml), from latest.log or *.log.gz (default
logs/latest.log), and reports per side (server, client): windows, ticks, hook calls, the hook's share of tick time
(total and the worst window), mean and longest single call, entity-ticks inside a pass-through block (mean per tick and
peak in one tick) and neighbor counts per tick. Pair it with a JFR recording of the same session (perf.md).
"""
import gzip
import json
import re
import sys

LINE = re.compile(r"\[passthrough-timing\] (.*)$")
KV = re.compile(r"(\w+)=(\S+)")


def lines(path):
	op = gzip.open if path.endswith(".gz") else open
	with op(path, "rt", errors="replace") as f:
		yield from f


def parse(paths):
	rows = []
	for p in paths:
		for l in lines(p):
			m = LINE.search(l)
			if not m:
				continue
			d = dict(KV.findall(m[1]))
			try:
				rows.append({"side": d["side"], "ticks": int(d["ticks"]), "calls": int(d["calls"]),
							 "total_us": float(d["total_us"]), "max_us": float(d["max_us"]),
							 "inside": int(d["inside"]), "inside_peak": int(d["inside_peak"]),
							 "counts": int(d["counts"]), "tick_us": float(d["tick_us"])})
			except (KeyError, ValueError):
				continue
	return rows


def summary(rows):
	out = {}
	for side in sorted({r["side"] for r in rows}):
		rs = [r for r in rows if r["side"] == side]
		ticks = sum(r["ticks"] for r in rs)
		calls = sum(r["calls"] for r in rs)
		total = sum(r["total_us"] for r in rs)
		tick = sum(r["tick_us"] for r in rs)
		worst = max(rs, key=lambda r: r["total_us"] / r["tick_us"] if r["tick_us"] else 0)
		out[side] = {
			"windows": len(rs), "ticks": ticks, "calls": calls,
			"share_pct": round(100 * total / tick, 3) if tick else None,
			"worst_window_share_pct": round(100 * worst["total_us"] / worst["tick_us"], 3) if worst["tick_us"] else None,
			"hook_ms_per_tick": round(total / ticks / 1000, 4) if ticks else None,
			"tick_ms_mean": round(tick / ticks / 1000, 3) if ticks else None,
			"mean_call_ns": round(1000 * total / calls, 1) if calls else None,
			"max_call_us": max(r["max_us"] for r in rs),
			"inside_per_tick": round(sum(r["inside"] for r in rs) / ticks, 2) if ticks else None,
			"inside_peak": max(r["inside_peak"] for r in rs),
			"counts_per_tick": round(sum(r["counts"] for r in rs) / ticks, 2) if ticks else None,
		}
	return out


def main():
	args = sys.argv[1:]
	out_json = None
	if "--json" in args:
		i = args.index("--json")
		out_json = args[i + 1]
		del args[i:i + 2]
	paths = args or ["logs/latest.log"]
	rows = parse(paths)
	if not rows:
		print("no [passthrough-timing] lines (is [debug.passthrough] timing = true in creativecompat-common.toml?)")
		return 1
	s = summary(rows)
	for side, v in s.items():
		print(f"{side}: {v['windows']} windows, {v['ticks']} ticks, {v['calls']} hook calls")
		print(f"  share of tick time {v['share_pct']}% (worst window {v['worst_window_share_pct']}%), "
			  f"{v['hook_ms_per_tick']} ms per tick of {v['tick_ms_mean']} ms")
		print(f"  per call: mean {v['mean_call_ns']} ns, longest {v['max_call_us']} us")
		print(f"  inside a pass-through block: {v['inside_per_tick']} entities per tick (peak {v['inside_peak']}); "
			  f"neighbor counts {v['counts_per_tick']} per tick")
	if out_json:
		with open(out_json, "w") as f:
			json.dump({"files": paths, "sides": s}, f, indent=2)
			f.write("\n")
	return 0


if __name__ == "__main__":
	sys.exit(main())

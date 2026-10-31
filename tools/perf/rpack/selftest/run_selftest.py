#!/usr/bin/env python3
"""Offline selftest of the resource pack bench chain (feature_writeup/perf_rpack.md 6): rp_scan -> plan ->
rp_bench.js under node with mocks (js_harness.js) -> synthetic latest.log -> rp_report. No game.

    python3 tools/perf/rpack/selftest/run_selftest.py [OUT_DIR]

The synthetic log gives "-Fancy Crops v1.2" a smaller block atlas, so the report must flag that pack's atlas.
"""
import argparse
import json
import os
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
import rp_bench  # noqa: E402
import rp_report  # noqa: E402


def main():
    out = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else tempfile.mkdtemp(prefix="rpbench-selftest-")
    os.makedirs(out, exist_ok=True)
    a = argparse.Namespace(packs=None, builtin=False, repeat=1, every=6, quick=True, no_scenes=False)
    plan = rp_bench.make_plan(a, rp_bench.scan(out))
    json.dump(plan, open(os.path.join(out, "plan.json"), "w"))
    klog = os.path.join(out, "client_kubejs.log")
    subprocess.run(["node", os.path.join(HERE, "js_harness.js"), os.path.join(out, "plan.json"), klog], check=True)
    lines = []
    for line in open(klog):
        m = rp_report.LINE.search(line)
        if m and m[1] == "reload":
            r = json.loads(m[2])
            h = "2048" if r["config"] == "-Fancy Crops v1.2" else "4096"
            lines.append(f"[{r['wall1']}] [Render thread/INFO]: Created: 8192x{h}x4 minecraft:textures/atlas/blocks.png-atlas")
            lines.append(f"[{r['wall1']}] [Render thread/INFO]: Created: 2048x2048x0 minecraft:textures/atlas/particles.png-atlas")
    open(os.path.join(out, "client_latest.log"), "w").write("\n".join(lines) + "\n")
    rp_report.main(out)
    rep = open(os.path.join(out, "rpack-report.txt")).read()
    checks = {
        "every config reloaded": f"Configs run {len(plan['configs'])} of {len(plan['configs'])};" in rep,
        "no errors": "errors/timeouts 0" in rep,
        "atlas flag on Fancy Crops": any(l.startswith("Fancy Crops") and "atlas 8192x4096 (without: 8192x2048)" in l
                                         for l in rep.splitlines()),
        "no false frame flag": "frame +" not in rep,
    }
    for k, v in checks.items():
        print(("PASS " if v else "FAIL ") + k)
    print("selftest dir:", out)
    sys.exit(0 if all(checks.values()) else 1)


if __name__ == "__main__":
    main()

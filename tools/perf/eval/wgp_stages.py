#!/usr/bin/env python3
"""Per-stage chunk generation means (minecraft.ChunkGeneration) of each wgp_run.sh world's pregen.jfr:
   wgp_stages.py perf/wgp/wgp_old perf/wgp/wgp_new ...      (needs the JDK's `jfr` on PATH)"""
import os, re, subprocess, sys
STAGES = ['structure_starts', 'biomes', 'noise', 'surface', 'features', 'light', 'full']
rows = []
for d in sys.argv[1:]:
    f = os.path.join(d, 'pregen.jfr')
    if not os.path.exists(f):
        continue
    out = subprocess.run(['jfr', 'print', '--events', 'minecraft.ChunkGeneration', f], capture_output=True, text=True).stdout
    tot, n = {}, {}
    for ev in out.split('minecraft.ChunkGeneration {')[1:]:
        st = re.search(r'status = "([^"]+)"', ev)
        du = re.search(r'duration = ([\d.]+) (ms|s|us)', ev)
        if not st or not du:
            continue
        v = float(du.group(1)) * {'ms': 1, 's': 1000, 'us': 0.001}[du.group(2)]
        s = st.group(1).replace('minecraft:', '')
        tot[s] = tot.get(s, 0) + v; n[s] = n.get(s, 0) + 1
    rows.append((os.path.basename(d.rstrip('/')), {s: (tot[s] / n[s], n[s]) for s in tot}))
print('%-20s' % 'world' + ''.join('%18s' % s for s in STAGES))
for name, m in rows:
    print('%-20s' % name + ''.join('%18s' % ('%.1f ms (%d)' % m[s] if s in m else '-') for s in STAGES))

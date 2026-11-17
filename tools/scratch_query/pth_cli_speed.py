#!/usr/bin/env python3
"""Per-sample player speed from zpth watch lines in a scratch server log, per "PTHCLI <label>" marker (labels starting
with PREFIX). Prints "coord:speed(mod)" along AXIS (x, y or z); the server's player motion is not the client's, so the
client-side walk speed is read from position steps.  pth_cli_speed.py LOG PREFIX [AXIS=z]"""
import re
import sys
log, pre = sys.argv[1], sys.argv[2]
ax = {"x": 0, "y": 1, "z": 2}[sys.argv[3] if len(sys.argv) > 3 else "z"]
lab, prev = None, None
for line in open(log, errors="replace"):
	m = re.search(r"PTHCLI (\S+)", line)
	if m:
		lab, prev = m.group(1), None
		if lab.startswith(pre):
			print("\n==", lab, end=" ")
		continue
	if not lab or not lab.startswith(pre):
		continue
	m = re.search(r"w me t=(\d+) minecraft:player mod=(\S+).*?pos=(\S+) (\S+) (\S+)", line)
	if not m:
		continue
	t, mod = int(m.group(1)), m.group(2)
	c = float(m.group(3 + ax))
	if prev and t > prev[0]:
		v = (c - prev[1]) / (t - prev[0])
		if abs(v) > 0.001:
			print(f"{c:.2f}:{v:.3f}{'' if mod == 'none' else '(' + mod + ')'}", end=" ")
	prev = (t, c)
print()

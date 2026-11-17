#!/usr/bin/env python3
"""Summarizes a scratch server log for pth_client_drive.py runs: per "PTHCLI <label>" marker, the player's watch
lines condensed to one entry per change of pass-through modifier (pos, motion, modifier, speed attribute, hp).
    pth_cli_summary.py LOG [LABEL_PREFIX]"""
import re
import sys
lab, last = None, None
pre = sys.argv[2] if len(sys.argv) > 2 else ""
for line in open(sys.argv[1], errors="replace"):
	m = re.search(r"PTHCLI (\S+)", line)
	if m:
		lab, last = m.group(1), None
		if lab.startswith(pre):
			print("==", lab, line[12:20])
		continue
	if not lab or not lab.startswith(pre):
		continue
	m = re.search(r"\[zpth\] (?:w me t=(\d+)|player \S+) minecraft:player mod=(\S+) hp=(\S+) speed=(\S+) climb=(\S+) pos=(\S+) (\S+) (\S+) mot=(\S+) (\S+) (\S+).*?ground=(\S+)", line)
	if not m:
		continue
	t, mod, hp, sp, cl, x, y, z, vx, vy, vz, gr = m.groups()
	key = (mod, cl, gr)
	if key != last:
		print(f"  t={t} mod={mod} hp={hp} speed={sp} climb={cl} ground={gr} pos={x} {y} {z} mot={vx} {vy} {vz}")
		last = key

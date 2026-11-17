#!/usr/bin/env python3
"""Generates s1_base.qb and s1_fix.qb from s1_common.inc (see its header). Usage: s1_gen.py [INC] (default s1_common.inc)."""
import os, sys
here = os.path.dirname(os.path.abspath(__file__))
inc = sys.argv[1] if len(sys.argv) > 1 else os.path.join(here, "s1_common.inc")
stem = os.path.basename(inc).replace("_common.inc", "")
src = open(inc).read().splitlines()
for kind in ("base", "fix"):
	out = []
	for line in src:
		if line.startswith("!"):
			tag, _, rest = line[1:].partition(" ")
			if tag == kind:
				out.append(rest)
			continue
		out.append(line)
	open(os.path.join(here, "%s_%s.qb" % (stem, kind)), "w").write("\n".join(out) + "\n")
	print("wrote", "%s_%s.qb" % (stem, kind))

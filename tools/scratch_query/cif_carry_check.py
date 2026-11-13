#!/usr/bin/env python3
"""SQ-CIF-01 (r1113 c3_a3.qb): after a boot on pre-carve configs, does every key of the carried sections keep its old value?
    cif_carry_check.py OLD_COMMON NEW_COMPAT NEW_EQUIPMENT      run in the scratch server folder (OUT/srv)
Prints one DIFF line per key whose value changed (or vanished), then "carried keys differing: N". Exit 0 always: judged by hand.
"""
import re
import sys

SECTIONS = ("jei", "gui_title", "toasts", "tooltips", "coaster", "wthit", "armor_overflow")


def sections(path):
    out, cur = {}, None
    for line in open(path):
        m = re.match(r"^\s*\[([A-Za-z_.]+)\]", line)
        if m:
            cur = m.group(1)
            continue
        m = re.match(r"^\s*([A-Za-z_]+)\s*=\s*(.+?)\s*$", line)
        if m and cur:
            out[(cur, m.group(1))] = m.group(2)
    return out


old, compat, equip = (sections(p) for p in sys.argv[1:4])
diff = 0
for (sec, key), val in sorted(old.items()):
    if sec.split(".")[0] not in SECTIONS:
        continue
    now = (equip if sec.startswith("armor_overflow") else compat).get((sec, key))
    if now != val:
        diff += 1
        print("DIFF", sec, key, "old", val, "now", now)
print("carried keys differing:", diff)

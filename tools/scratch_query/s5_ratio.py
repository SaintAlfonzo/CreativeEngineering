#!/usr/bin/env python3
"""Spring/winter spread counts for the SQ-PST grass rows (run r1102b, batches/r1102b/s5_A.qb).

    s5_ratio.py LOG TAG [LO HI]

The batch says "S5R TAG spring" (or winter) right before each counting /fill; the first "Successfully filled N
block(s)" (or "No blocks were filled" = 0) after that marker is the run's count. Prints the counts, the means and
winter/spring; exit 0 when the spring mean is above 0 and LO <= ratio <= HI (default 0.6 to 0.9), else 1.
"""
import re
import sys


def main():
    log, tag = sys.argv[1], sys.argv[2]
    lo, hi = (float(sys.argv[3]), float(sys.argv[4])) if len(sys.argv) > 4 else (0.6, 0.9)
    mark = re.compile(r"S5R " + re.escape(tag) + r" (spring|winter)\b")
    filled = re.compile(r"Successfully filled (\d+) block|No blocks were filled")
    runs = {"spring": [], "winter": []}
    want = None
    with open(log, errors="replace") as f:
        for line in f:
            m = mark.search(line)
            if m:
                want = m.group(1)
                continue
            if want:
                m = filled.search(line)
                if m:
                    runs[want].append(int(m.group(1) or 0))
                    want = None
    sp, wi = runs["spring"], runs["winter"]
    msp = sum(sp) / len(sp) if sp else 0.0
    mwi = sum(wi) / len(wi) if wi else 0.0
    ratio = mwi / msp if msp else float("nan")
    print(f"{tag}: spring {sp} mean {msp:.1f}; winter {wi} mean {mwi:.1f}; winter/spring {ratio:.2f} (pass {lo}-{hi})")
    ok = len(sp) >= 3 and len(wi) >= 3 and msp > 0 and lo <= ratio <= hi
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()

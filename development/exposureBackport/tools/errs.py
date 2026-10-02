#!/usr/bin/env python3
"""Show javac errors from a gradle log, filtered by path substring: tools/errs.py <log> [filter] [--max N]"""
import re, sys
log, filt = sys.argv[1], (sys.argv[2] if len(sys.argv) > 2 and not sys.argv[2].startswith('--') else '')
mx = int(sys.argv[sys.argv.index('--max') + 1]) if '--max' in sys.argv else 400
lines = open(log, errors='replace').read().split('\n')
out = 0
i = 0
while i < len(lines) and out < mx:
    m = re.match(r'(.*/src/\w+/java/)(.*\.java):(\d+): error: (.*)', lines[i])
    if m and filt in m.group(2):
        msg = m.group(4)
        j = i + 1
        extra = []
        while j < len(lines) and not re.match(r'.*\.java:\d+: (error|warning):', lines[j]) and j < i + 6:
            if lines[j].strip() and not lines[j].strip() == '^':
                extra.append(lines[j].strip())
            j += 1
        sym = [e for e in extra if e.startswith('symbol') or e.startswith('location') or e.startswith('required') or e.startswith('found') or e.startswith('reason')]
        code = extra[0] if extra else ''
        print(f"{m.group(2).split('/exposure/',1)[-1]}:{m.group(3)}: {msg} | {' | '.join(sym)}\n    {code[:220]}")
        out += 1
    i += 1

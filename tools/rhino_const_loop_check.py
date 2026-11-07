#!/usr/bin/env python3
"""Lists `const` declarations inside loop bodies (for/while/forEach callbacks excluded: those are function scopes) in
KubeJS scripts. The pack's Rhino keeps a loop-body const's FIRST value (memory rhino-fieldandmethods-gotcha).
Usage: tools/rhino_const_loop_check.py FILE_OR_DIR ...   Heuristic brace matching; strings and comments are not parsed."""
import os, re, sys
LOOP = re.compile(r'\b(for|while)\s*\(.*\)\s*\{\s*$|\b(for|while)\s*\(.*\)\s*\{')
def check(path):
    lines = open(path, encoding='utf-8', errors='replace').read().split('\n')
    stack = []  # brace depth at which each open loop body started
    depth = 0
    for n, l in enumerate(lines, 1):
        code = re.sub(r'//.*', '', l)
        if LOOP.search(code):
            stack.append(depth)
        for ch in code:
            if ch == '{': depth += 1
            elif ch == '}':
                depth -= 1
                while stack and depth <= stack[-1]:
                    stack.pop()
        # a callback inside the loop (=> or function) is its own scope: crude skip
        if stack and re.search(r'\bconst\s', code) and not re.search(r'=>|function', code):
            print(f'{path}:{n}: {l.strip()[:120]}')
for a in sys.argv[1:]:
    if os.path.isdir(a):
        for r, _, fs in os.walk(a):
            for f in fs:
                if f.endswith('.js'): check(os.path.join(r, f))
    else: check(a)

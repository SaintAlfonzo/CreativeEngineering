#!/usr/bin/env python3
"""Claude Code Stop hook (plan_process_overhaul.md P1.1 and P1.5): runs tools/check_all.py --fast --changed and shows
its FAIL lines, plus a count of uncommitted tracked files, as a message to the user. Report only: it never blocks.

Stop fires at the end of every turn, so the run is skipped while the changed files (path, size, mtime) are the same as
at the last run; the last result is shown again instead. State: jar_archive/check_stop_state.json (not tracked).
"""
import hashlib
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STATE = ROOT / 'jar_archive' / 'check_stop_state.json'


def git(*a):
    r = subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True)
    return [l for l in r.stdout.splitlines() if l]


def main():
    try:
        sys.stdin.read()
    except OSError:
        pass
    changed = sorted(set(git('diff', '--name-only', 'HEAD')) | set(git('ls-files', '--others', '--exclude-standard')))
    if not changed:
        return 0
    h = hashlib.sha1()
    for c in changed:
        p = ROOT / c
        st = p.stat() if p.exists() else None
        h.update(('%s %s %s\n' % (c, st.st_size if st else '-', st.st_mtime_ns if st else '-')).encode())
    key = h.hexdigest()
    try:
        state = json.loads(STATE.read_text())
    except (OSError, ValueError):
        state = {}
    if state.get('key') == key:
        lines = state.get('lines', [])
    else:
        r = subprocess.run([sys.executable, str(ROOT / 'tools' / 'check_all.py'), '--fast', '--changed'], cwd=ROOT,
                           capture_output=True, text=True, timeout=170)
        lines = [l for l in r.stdout.splitlines() if l.startswith(('FAIL', 'WARN'))]
        try:
            STATE.parent.mkdir(exist_ok=True)
            STATE.write_text(json.dumps({'key': key, 'lines': lines}))
        except OSError:
            pass
    tracked = len(git('diff', '--name-only', 'HEAD'))
    msg = []
    if lines:
        msg.append('check_all --fast --changed:\n' + '\n'.join(lines))
    if tracked:
        msg.append('%d tracked file(s) uncommitted: commit finished plan phases (CLAUDE.md "Commits").' % tracked)
    if msg:
        print(json.dumps({'systemMessage': '\n'.join(msg)}))
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as e:  # a hook must never break the session
        print(json.dumps({'systemMessage': 'check_stop_hook: %s' % e}))
        sys.exit(0)

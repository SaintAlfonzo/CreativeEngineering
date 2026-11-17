#!/usr/bin/env python3
"""Declare what this Claude Code session is doing, for the Stop hook (tools/check_stop_hook.py).

Usage: python3 tools/session_role.py plan|impl|test|show|clear

  plan  planning, docs, reviews: the Stop hook stays silent
  impl  implementing gameplay code: fast checks on this session's own code files, commit reminder for them
  test  scratch testing: reminder to commit the run docs and queue rows this session wrote

A session that never declares is treated as impl, with a hint to declare. Change the role whenever the work changes.
The marker is keyed by $CLAUDE_CODE_SESSION_ID: jar_archive/session_roles/<id>.json (not tracked).
"""
import json
import os
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DIR = ROOT / 'jar_archive' / 'session_roles'
ROLES = ('plan', 'impl', 'test')


def marker(sid):
    return DIR / ('%s.json' % sid)


def read_role(sid):
    try:
        return json.loads(marker(sid).read_text()).get('role')
    except (OSError, ValueError):
        return None


def main(argv):
    if len(argv) != 2 or argv[1] not in ROLES + ('show', 'clear'):
        print(__doc__.strip())
        return 2
    sid = os.environ.get('CLAUDE_CODE_SESSION_ID')
    if not sid:
        print('session_role: CLAUDE_CODE_SESSION_ID not set (run this from a Claude Code session)')
        return 1
    cmd = argv[1]
    if cmd == 'show':
        print(read_role(sid) or 'undeclared (treated as impl)')
        return 0
    if cmd == 'clear':
        try:
            marker(sid).unlink()
        except OSError:
            pass
        print('session_role: cleared')
        return 0
    DIR.mkdir(parents=True, exist_ok=True)
    marker(sid).write_text(json.dumps({'role': cmd, 'set': time.strftime('%Y-%m-%d %H:%M:%S')}))
    print('session_role: %s' % cmd)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))

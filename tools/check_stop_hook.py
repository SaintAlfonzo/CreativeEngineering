#!/usr/bin/env python3
"""Claude Code Stop hook (plan_process_overhaul.md P1.1 and P1.5). Report only: it never blocks.

Behavior follows the role the session declared with tools/session_role.py (undeclared = impl, plus a hint):
  plan  nothing.
  impl  runs tools/check_all.py --fast --changed on this session's own uncommitted code files and shows its FAIL/WARN
        lines, plus a count of those files; when the session touched feature_writeup/, tools/ or jar_archive/, one line
        if `status_index.py --check` finds a generated block or a derived status word out of date (P3.4 k). Docs (*.md, feature_writeup/, todo/, .claude/) never count, and nor do
        other sessions' files, so plan files from a parallel planning session are not pushed along with a build.
  test  no checks; a count of this session's own uncommitted run docs and queue rows (feature_writeup/testing/).

"Own" files: Edit/Write/NotebookEdit paths in this session's transcript and its subagents' transcripts, plus
uncommitted paths named in a Bash command that writes (sed -i, >, tee, cp, mv, ...). Files written by a script without
being named (generator output) are not seen.

Stop fires at the end of every turn, so the check run is skipped while the own changed files (path, size, mtime) are
the same as at the last run; the last result is shown again instead. State: jar_archive/check_stop_state.json.
"""
import hashlib
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STATE = ROOT / 'jar_archive' / 'check_stop_state.json'
sys.path.insert(0, str(ROOT / 'tools'))
from session_role import read_role  # noqa: E402

DOC_DIRS = ('feature_writeup/', 'todo/', '.claude/', 'communications/', 'deploy/')
TEST_DIRS = ('feature_writeup/testing/',)
EDIT_TOOLS = ('Edit', 'Write', 'NotebookEdit', 'MultiEdit')
BASH_WRITE = re.compile(r'sed\s+(-\w*\s+)*-i|perl\s+-\w*i|>|\btee\b|\bcp\b|\bmv\b|\binstall\b|\bpatch\b|open\(')


def git(*a):
    r = subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True)
    return [l for l in r.stdout.splitlines() if l]


def is_doc(p):
    return p.endswith('.md') or p == 'CLAUDE.md' or p.startswith(DOC_DIRS)


def rel(p):
    try:
        return str(Path(p).resolve().relative_to(ROOT))
    except (ValueError, OSError):
        return None


def transcripts(path):
    if not path:
        return []
    t = Path(path)
    return [t] + sorted((t.parent / t.stem / 'subagents').glob('*.jsonl'))


def own_files(transcript_path, uncommitted):
    """Uncommitted repo paths this session (or its subagents) wrote."""
    out, bash = set(), []
    for t in transcripts(transcript_path):
        try:
            fh = t.open(errors='replace')
        except OSError:
            continue
        with fh:
            for line in fh:
                if '"tool_use"' not in line:
                    continue
                try:
                    content = json.loads(line).get('message', {}).get('content')
                except ValueError:
                    continue
                if not isinstance(content, list):
                    continue
                for b in content:
                    if not isinstance(b, dict) or b.get('type') != 'tool_use':
                        continue
                    inp = b.get('input') or {}
                    if b.get('name') in EDIT_TOOLS:
                        r = rel(inp.get('file_path') or inp.get('notebook_path') or '')
                        if r:
                            out.add(r)
                    elif b.get('name') == 'Bash' and BASH_WRITE.search(inp.get('command', '')):
                        bash.append(inp['command'])
    if bash:
        blob = '\n'.join(bash)
        out |= {u for u in uncommitted if u in blob}
    return sorted(out & uncommitted)


def run_checks(files):
    h = hashlib.sha1()
    for c in files:
        p = ROOT / c
        st = p.stat() if p.exists() else None
        h.update(('%s %s %s\n' % (c, st.st_size if st else '-', st.st_mtime_ns if st else '-')).encode())
    key = h.hexdigest()
    try:
        state = json.loads(STATE.read_text())
    except (OSError, ValueError):
        state = {}
    if state.get('key') == key:
        return state.get('lines', [])
    with tempfile.NamedTemporaryFile('w', suffix='.txt', delete=False) as tf:
        tf.write('\n'.join(files) + '\n')
    try:
        r = subprocess.run([sys.executable, str(ROOT / 'tools' / 'check_all.py'), '--fast', '--changed',
                            '--changed-file', tf.name], cwd=ROOT, capture_output=True, text=True, timeout=170)
    finally:
        Path(tf.name).unlink(missing_ok=True)
    lines = [l for l in r.stdout.splitlines() if l.startswith(('FAIL', 'WARN'))]
    try:
        STATE.parent.mkdir(exist_ok=True)
        STATE.write_text(json.dumps({'key': key, 'lines': lines}))
    except OSError:
        pass
    return lines


def status_blocks_stale():
    """One line when a generated status block or a derived feature status is out of date (P3.4 k), else ''."""
    try:
        r = subprocess.run(['python3', 'tools/status_index.py', '--check'], cwd=ROOT, capture_output=True, text=True,
                           timeout=60)
    except (OSError, subprocess.SubprocessError):
        return ''
    n = sum(1 for l in r.stdout.splitlines() if l.startswith('WARN') and 'out of date' in l)
    return ('%d generated status block(s) or feature status word(s) out of date: python3 tools/status_index.py' % n
            if n else '')


def main():
    try:
        data = json.loads(sys.stdin.read() or '{}')
    except (OSError, ValueError):
        data = {}
    role = read_role(data.get('session_id') or '')
    if role == 'plan':
        return 0
    uncommitted = set(git('diff', '--name-only', 'HEAD')) | set(git('ls-files', '--others', '--exclude-standard'))
    if not uncommitted:
        return 0
    own = own_files(data.get('transcript_path'), uncommitted)
    msg = []
    if role == 'test':
        runs = [f for f in own if f.startswith(TEST_DIRS)]
        if runs:
            msg.append('%d run doc / queue file(s) uncommitted: %s' % (len(runs), ', '.join(runs[:6])
                                                                    + (' ...' if len(runs) > 6 else '')))
    else:
        code = [f for f in own if not is_doc(f)]
        if code:
            lines = run_checks(code)
            if lines:
                msg.append('check_all --fast --changed (own files):\n' + '\n'.join(lines))
            msg.append('%d own code file(s) uncommitted: commit finished plan phases (CLAUDE.md "Commits").'
                       % len(code))
        if any(f.startswith(('feature_writeup/', 'tools/', 'jar_archive/')) for f in own):
            stale = status_blocks_stale()
            if stale:
                msg.append(stale)
        if role is None and code:
            msg.append('Session role undeclared (treated as impl): python3 tools/session_role.py plan|impl|test')
    if msg:
        print(json.dumps({'systemMessage': '\n'.join(msg)}))
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as e:  # a hook must never break the session
        print(json.dumps({'systemMessage': 'check_stop_hook: %s' % e}))
        sys.exit(0)

#!/usr/bin/env python3
"""Edit TODO rows (plan_process_overhaul.md P3.5 item 2). Rows: feature_writeup/todo/rows/<category>.csv; TODO.md is
generated from them by tools/status_index.py (run it after an edit, or pass --render).

  todo.py show ID                         every field of a row
  todo.py find TEXT [--status S] [--family F] [--plan P] [--open]
  todo.py add --family F --change TEXT [--status Planned] [--tests "S P"] [--work W] [--details D] [--decisions D]
              [--writeup W] [--feature K] [--plan P] [--source S] [--priority high|normal|low]
                                          new row; id = the family slug and its next number (D17)
  todo.py set ID field=value ...          set fields (status, priority, tests, work, change, details, decisions,
                                          writeup, feature, plan, source, queue)
  todo.py log ID TEXT                     add a dated log entry (newest first): what was built, run, installed
  todo.py move ID --family F              to another family (and its category file)
  todo.py lint [--changed]                schema check of every rows file; with --changed, new or changed Details text
                                          with status or install words FAILs (they belong in `log` and the generated
                                          state). Exit 1 on a problem.
Every edit writes `updated` (today) and keeps each file in stable order (status rank, then id).
"""
import argparse
import datetime as dt
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import table_io  # noqa: E402
import todo_rows as tr  # noqa: E402

EDITABLE = ('status', 'priority', 'tests', 'work', 'change', 'details', 'decisions', 'writeup', 'feature', 'plan',
            'source', 'queue', 'table')
PROSE_STATUS_RE = re.compile(r'not installed|\binstalled\b|\*\*BUILT|\*\*Built|Status was', re.I)


def today():
    return dt.date.today().isoformat()


def family_files(rows, layout_text):
    """{family: category file} from the rows, and for empty families from the layout's `#` headings."""
    out = {r['family']: r['_file'] for r in rows if r.get('family')}
    cat = None
    for l in layout_text.split('\n'):
        if l.startswith('# ') and not l.startswith('# TODO'):
            cat = l[2:].strip()
        m = tr.PLACEHOLDER_RE.match(l.strip())
        if m and m.group(1) == 'rows' and cat:
            out.setdefault(m.group(2).strip(), tr.slug(cat) + '.csv')
    return out


def find_row(rows, rid):
    hit = [r for r in rows if r['id'] == rid]
    if not hit:
        sys.exit('no row %r (todo.py find TEXT)' % rid)
    return hit[0]


def check_value(field, value, rows, fams):
    if field == 'status' and value not in tr.STATUS_ORDER:
        sys.exit('status must be one of: %s' % ', '.join(tr.STATUS_ORDER))
    if field == 'priority' and value not in tr.PRIORITIES:
        sys.exit('priority must be high, normal or low')
    if field == 'plan' and value and not (tr.FW / value.split()[0]).exists():
        sys.exit('plan %r: no such file in feature_writeup/' % value)
    if field == 'feature' and value:
        keys = {f['key'] for f in table_io.read_dicts(tr.FW / 'status' / 'features.csv')}
        if value not in keys:
            sys.exit('feature %r is not a features.csv key' % value)
    if field == 'details' and PROSE_STATUS_RE.search(value):
        sys.exit('details carry no status or install words (%r): use `todo.py log`; install state is generated'
                 % PROSE_STATUS_RE.search(value).group(0))


def render():
    subprocess.run([sys.executable, str(ROOT / 'tools' / 'status_index.py')], cwd=ROOT)


def cmd_lint(changed):
    rows = tr.load()
    layout = tr.LAYOUT.read_text() if tr.LAYOUT.exists() else None
    probs = ['%s line %s %s: %s' % e for e in tr.validate(rows, layout)]
    for p in tr.category_files():
        hdr = table_io.read(p)[1]
        if hdr != tr.COLS:
            probs.append('%s: header is not %s' % (p.name, ','.join(tr.COLS)))
    if changed:
        for p in tr.category_files():
            rel = str(p.relative_to(ROOT))
            r = subprocess.run(['git', 'show', 'HEAD:' + rel], cwd=ROOT, capture_output=True, text=True)
            old = {}
            if r.returncode != 0:
                continue   # a new rows file (the migration itself) is the baseline, not a change
            if r.returncode == 0:
                tmp = ROOT / 'jar_archive' / '.todo_lint_head.csv'
                tmp.write_text(r.stdout)
                old = {d['id']: d.get('details', '') for d in table_io.read_dicts(tmp)}
                tmp.unlink()
            for d in table_io.read_dicts(p):
                if d.get('details', '') != old.get(d['id']) and PROSE_STATUS_RE.search(d.get('details', '')):
                    if old.get(d['id']) and PROSE_STATUS_RE.search(old[d['id']]) and \
                            len(PROSE_STATUS_RE.findall(d['details'])) <= len(PROSE_STATUS_RE.findall(old[d['id']])):
                        continue   # the words were there before this change
                    probs.append('%s line %d %s: details has %r; write it with `todo.py log`, install state is '
                                 'generated' % (p.name, d['_line'], d['id'], PROSE_STATUS_RE.search(d['details']).group(0)))
    if changed:
        # briefs are design notes for rows: status words in new brief text are a warning (P3.5 item 5)
        r = subprocess.run(['git', 'diff', '-U0', 'HEAD', '--', 'feature_writeup/briefs/'], cwd=ROOT,
                           capture_output=True, text=True)
        f = None
        for l in r.stdout.split('\n'):
            if l.startswith('+++ '):
                f = l[6:]
            elif l.startswith('+') and not l.startswith('+++') and PROSE_STATUS_RE.search(l):
                print('WARN %s: status words in a brief (%r); status lives in the TODO row' % (
                    f, PROSE_STATUS_RE.search(l).group(0)))
    for x in probs:
        print('FAIL ' + x)
    print('todo lint: %d row(s) in %d file(s), %d problem(s)' % (len(rows), len(tr.category_files()), len(probs)))
    return 1 if probs else 0


def main(argv):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('cmd', choices=['show', 'find', 'add', 'set', 'log', 'move', 'lint'])
    ap.add_argument('args', nargs='*')
    for f in ('family', 'change', 'status', 'tests', 'work', 'details', 'decisions', 'writeup', 'feature', 'plan',
              'source', 'priority'):
        ap.add_argument('--' + f)
    ap.add_argument('--open', action='store_true')
    ap.add_argument('--changed', action='store_true')
    ap.add_argument('--render', action='store_true', help='run status_index.py after the edit')
    a = ap.parse_args(argv)

    if a.cmd == 'lint':
        return cmd_lint(a.changed)
    rows = tr.load()
    layout = tr.LAYOUT.read_text()
    fams = family_files(rows, layout)

    if a.cmd == 'show':
        r = find_row(rows, ' '.join(a.args))
        for c in tr.COLS:
            print('%-10s %s' % (c, r.get(c, '')))
        print('%-10s %s line %s' % ('file', r['_file'], r['_line']))
        return 0
    if a.cmd == 'find':
        text = ' '.join(a.args).lower()
        for r in sorted(rows, key=tr.row_key):
            blob = ' '.join(r.get(c, '') for c in tr.COLS).lower()
            if text and text not in blob:
                continue
            if (a.status and r['status'] != a.status) or (a.family and r.get('family') != a.family) or \
                    (a.plan and a.plan not in r.get('plan', '')) or (a.open and r['status'] == 'Complete'):
                continue
            print('%-40s %-11s %-28s %s' % (r['id'], r['status'], (r.get('family') or '-')[:28], r['change'][:70]))
        return 0
    if a.cmd == 'add':
        if not a.family or not a.change:
            sys.exit('add needs --family and --change')
        if a.family not in fams:
            sys.exit('family %r has no <?todo rows ...?> in todo/layout.md' % a.family)
        r = {c: '' for c in tr.COLS}
        r.update(id=tr.next_id(rows, a.family), family=a.family, status=a.status or 'Planned', updated=today(),
                 _file=fams[a.family])
        for f in ('change', 'tests', 'work', 'details', 'decisions', 'writeup', 'feature', 'plan', 'source', 'priority'):
            v = getattr(a, f)
            if v is not None:
                check_value(f, v, rows, fams)
                r[f] = v
        check_value('status', r['status'], rows, fams)
        rows.append(r)
        tr.save_file(r['_file'], [x for x in rows if x['_file'] == r['_file']])
        print('added %s (%s)' % (r['id'], r['_file']))
    elif a.cmd in ('set', 'log', 'move'):
        if not a.args:
            sys.exit('which row?')
        # the id has a space ("family #n"): take words until one starts with '#'
        k = next((i for i, w in enumerate(a.args) if w.startswith('#')), None)
        if k is None:
            sys.exit('row id like "food/salt #3"')
        rid, rest = ' '.join(a.args[:k + 1]), a.args[k + 1:]
        r = find_row(rows, rid)
        if a.cmd == 'set':
            if not rest:
                sys.exit('set ID field=value ...')
            for kv in rest:
                f, _, v = kv.partition('=')
                if f not in EDITABLE:
                    sys.exit('field %r is not editable (%s)' % (f, ', '.join(EDITABLE)))
                check_value(f, v, rows, fams)
                r[f] = v
        elif a.cmd == 'log':
            entry = '%s %s' % (today(), ' '.join(rest).strip())
            r['log'] = entry + ('; ' + r['log'] if r.get('log') else '')
        else:
            if not a.family or a.family not in fams:
                sys.exit('move needs --family with a heading in todo/layout.md')
            old = r['_file']
            r['family'], r['_file'] = a.family, fams[a.family]
            tr.save_file(old, [x for x in rows if x['_file'] == old])
        r['updated'] = today()
        tr.save_file(r['_file'], [x for x in rows if x['_file'] == r['_file']])
        print('%s %s' % (a.cmd, rid))
    if a.render:
        render()
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))

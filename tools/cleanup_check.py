#!/usr/bin/env python3
"""Clean-up threshold warnings (plan_process_overhaul.md P3.6 item 1, owner ruling D10). WARN only, exit 0.

Each check counts something that should be cleaned up and warns when the count is over its limit in
tools/cleanup_thresholds.csv (check, limit, reason). check_all runs it as the `cleanup` check; the release skill runs
the full pass (`--report FILE` writes the list for the owner).

Usage: python3 tools/cleanup_check.py [--report FILE] [--quiet]
"""
import datetime as dt
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FW = ROOT / 'feature_writeup'
sys.path.insert(0, str(ROOT / 'tools'))
import table_io  # noqa: E402

TODAY = dt.date.today()
STATUS_WORDS = re.compile(r'not installed|\binstalled\b|\*\*BUILT|\bBUILT \d{4}|\bDEPLOYED\b|\bVERIFIED\b|^Status:', re.M)
DETAIL_WORDS = re.compile(r'not installed|\binstalled\b|\*\*BUILT|\*\*Built|Status was', re.I)


def days_since(day):
    try:
        return (TODAY - dt.date.fromisoformat(day[:10])).days
    except ValueError:
        return None


def run(cmd):
    r = subprocess.run(cmd, cwd=ROOT, capture_output=True, text=True, timeout=300)
    return r.stdout


def checks():
    """{check: (count, [examples])}"""
    out = {}
    import todo_rows
    rows = todo_rows.load()
    ip = [r for r in rows if r['status'] == 'In Progress' and (days_since(r.get('updated') or '2000-01-01') or 0) >
          LIMITS.get('todo_in_progress_days', 30)]
    out['todo_in_progress_days'] = (len(ip), [r['id'] for r in ip])
    tp = [r for r in rows if r['status'] == 'Testing' and r.get('tests') and r['tests'] != 'none' and
          all(t.endswith('+') for t in r['tests'].split())]
    out['todo_testing_passed'] = (len(tp), [r['id'] for r in tp])
    dw = [r for r in rows if r['status'] != 'Complete' and DETAIL_WORDS.search(r.get('details', ''))]
    out['todo_details_status_words'] = (len(dw), [r['id'] for r in dw])
    un = [r for r in rows if not r.get('family') and r['status'] != 'Complete']
    out['todo_unfiled_rows'] = (len(un), ['%s (%s)' % (r['id'], r['status']) for r in un])

    lint = run([sys.executable, 'tools/scratch_query/qrowlint.py', '--quiet'])
    m = re.search(r'(\d+) error\(s\), (\d+) warning\(s\)', lint)
    out['queue_dead_ids_errors'] = (int(m.group(1)) if m else 0, [])
    out['queue_stale_rows'] = (int(m.group(2)) if m else 0, [])
    arch = run([sys.executable, 'tools/scratch_query/qrun.py', 'archive'])
    m = re.search(r'archive: (\d+) section', arch)
    out['queue_archive_eligible'] = (int(m.group(1)) if m else 0,
                                     [l.strip() for l in arch.splitlines() if 'rows current' in l])
    st = run([sys.executable, 'tools/scratch_query/qrun.py', 'states'])
    lines = st.splitlines()
    if len(lines) >= 2 and lines[-1].startswith('total'):
        cols, tot = lines[0].split()[1:], [int(x) for x in lines[-1].split()[1:]]
        d = dict(zip(cols, tot))
        share = d.get('never', 0) / max(1, sum(tot))
        out['queue_never_share'] = (round(share, 2), ['%d of %d rows never run' % (d.get('never', 0), sum(tot))])

    g = ROOT / '.claude' / 'skills' / 'scratch-test' / 'gotchas.md'
    found = re.findall(r'^#+ .*Found in r\d+.*$', g.read_text(), re.M) if g.exists() else []
    out['gotchas_found_in_sections'] = (len(found), found)

    notes = sorted((FW / 'agent_notes').glob('*.md'))
    sw = [n.name for n in notes if n.name != 'README.md' and STATUS_WORDS.search(n.read_text(errors='replace'))]
    out['agent_notes_status_words'] = (len(sw), sw)
    docs = list(FW.rglob('*.md')) + [ROOT / 'CLAUDE.md'] + list((ROOT / '.claude' / 'skills').rglob('*.md'))
    texts = {p: p.read_text(errors='replace') for p in docs}
    unl = [n.name for n in notes if n.name != 'README.md' and
           not any(n.name in t for p, t in texts.items() if p != n)]
    out['agent_notes_unlinked'] = (len(unl), unl)

    mem = Path.home() / '.claude' / 'projects' / re.sub(r'[/.]', '-', str(ROOT)) / 'memory'
    mfiles = [p for p in mem.glob('*.md') if p.name != 'MEMORY.md'] if mem.is_dir() else []
    out['memory_files'] = (len(mfiles), [])
    proj = [p.name for p in mfiles if re.search(r'^\s*type:\s*project\b', p.read_text(errors='replace'), re.M)]
    out['memory_project_typed'] = (len(proj), proj)

    old = [d for d in table_io.read_dicts(ROOT / 'tools' / 'check_known.csv')
           if (days_since(d.get('date', '')) or 0) > LIMITS.get('check_known_days', 30)]
    out['check_known_days'] = (len(old), [d['check'] for d in old])

    import jarstamp
    extra = jarstamp.stale_outputs()
    out['build_libs_extra_jars'] = (len(extra), ['%s/%s' % (p, j.name) for p, j in extra])
    disp = ROOT / 'jar_archive' / 'displaced'
    size = sum(f.stat().st_size for f in disp.rglob('*') if f.is_file()) if disp.is_dir() else 0
    out['displaced_gb'] = (round(size / 1e9, 2), [])

    try:
        log = json.load(open(ROOT / 'deploy' / 'DEPLOYED.json')).get('log', [])
        hist = (FW / 'deploy.md').read_text().split('## 8. History', 1)[-1]
        miss = sorted({e['version'] for e in log if e.get('version') and e['version'] not in hist})
    except (OSError, ValueError):
        miss = []
    out['deploy_missing_bullets'] = (len(miss), miss)

    plans = {}
    for r in rows:
        for p in (x.strip().split()[0] for x in r.get('plan', '').split(',') if x.strip()):
            plans.setdefault(p, []).append(r['status'])
    done = []
    for p, sts in sorted(plans.items()):
        f = FW / p
        if f.exists() and all(s == 'Complete' for s in sts):
            head = '\n'.join(f.read_text(errors='replace').split('\n')[:20])
            if not re.search(r'\b(Closed|CLOSED|Done|DONE|Complete|COMPLETE)\b', head):
                done.append(p)
    out['plans_all_complete_open'] = (len(done), done)

    ov = [f['key'] for f in table_io.read_dicts(FW / 'status' / 'features.csv')
          if f.get('override') and (days_since(f.get('override_date', '')) or 0) > LIMITS.get('feature_override_days', 30)]
    out['feature_override_days'] = (len(ov), ov)
    return out


def load_limits():
    return {d['check']: float(d['limit']) for d in table_io.read_dicts(ROOT / 'tools' / 'cleanup_thresholds.csv')}


LIMITS = {}


def main(argv):
    LIMITS.update(load_limits())
    reasons = {d['check']: d['reason'] for d in table_io.read_dicts(ROOT / 'tools' / 'cleanup_thresholds.csv')}
    res = checks()
    warns = []
    for k, (n, ex) in res.items():
        lim = LIMITS.get(k)
        if lim is None:
            print('WARN cleanup: no threshold for %s (add it to tools/cleanup_thresholds.csv)' % k)
            continue
        if k.endswith('_days'):
            over = n > 0
        else:
            over = n > lim
        if over:
            warns.append((k, n, lim, ex))
            if True:
                print('WARN %s: %s (limit %s; %s)%s' % (k, n, lim if not k.endswith('_days') else '0 rows over %d days'
                                                          % lim, reasons.get(k, ''),
                                                          (': ' + ', '.join(map(str, ex[:4])) + (' ...' if len(ex) > 4
                                                                                                 else '')) if ex else ''))
    print('cleanup: %d warning(s) over %d checks' % (len(warns), len(res)))
    if '--report' in argv:
        out = Path(argv[argv.index('--report') + 1])
        md = ['# Clean-up pass %s' % TODAY.isoformat(), '',
              'Written by `tools/cleanup_check.py --report` (plan_process_overhaul.md P3.6). Limits: '
              '[tools/cleanup_thresholds.csv](../../tools/cleanup_thresholds.csv).', '',
              '| Check | Count | Limit | Reason |', '|---|---|---|---|']
        for k, (n, ex) in res.items():
            md.append('| %s%s | %s | %s | %s |' % ('**' if any(w[0] == k for w in warns) else '', k + (
                '**' if any(w[0] == k for w in warns) else ''), n, LIMITS.get(k, ''), reasons.get(k, '')))
        for k, n, lim, ex in warns:
            if ex:
                md += ['', '## %s (%s)' % (k, n), ''] + ['- %s' % e for e in ex]
        out.write_text('\n'.join(md) + '\n')
        print('report: %s' % out)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))

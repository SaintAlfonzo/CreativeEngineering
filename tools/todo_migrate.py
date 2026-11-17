#!/usr/bin/env python3
"""One-time move of feature_writeup/todo/TODO.md into rows/<category>.csv plus layout.md (plan_process_overhaul.md
P3.5 item 4). Run on a committed TODO.md.

  - every 8-column row table -> rows of its `##` family; the table becomes `<?todo rows FAMILY?>` in layout.md;
  - the archive table -> rows/archive.csv (6-cell rows Complete; 8-cell rows keep their open status, no family);
  - the plan-named tables (Scratch 1115 Follow-ups 1 and 2) are refiled into the family that holds most rows with
    the same id prefix (else the family most rows citing the same writeup sit in), with `plan` set; their sections
    leave the layout;
  - `**High priority ...**` markers -> priority=high; leading bold status prose in Details (BUILT, Built, Swept, Run,
    rNNNN, Done, Closed, PASS, FAIL, installed, not built) -> `log`; plan_*.md named in that prose -> `plan`;
  - feature (from the first writeup link, via features.csv `file`), queue (SQ-/CQ- row ids in the row), source
    (first "scratchpad DATE" or run id), updated (newest date in the row);
  - stale head prose removed (2026-10-21 priorities, the run list), Work legend's patcherMod updated, the snapshot
    block and a High priority section become placeholders.
Checks the round trip: the same ids; change, status, tests, work, decisions and writeup cells equal; every word of
the old Details cell still in the rendered one. Usage: python3 tools/todo_migrate.py [--dry] [--report FILE]
"""
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import table_io  # noqa: E402
import todo_rows as tr  # noqa: E402

ROW_HEAD = tr.TABLE_HEAD[0]
ARCH_HEAD = '| Id | Change | Work | Details | Decisions | Writeup |'
PLAN_TABLES = {'Scratch 1115 Follow-ups': 'plan_post_scratch_1115.md',
               'Scratch 1115 Follow-ups 2 (pass-through rework)': 'plan_post_scratch_1115b.md'}
# Plan-table rows have no family link (their writeup is the plan): filed by hand from their titles (P3.5 item 4)
REFILE = {}
for n in (1, 2):
    REFILE['tooling/scratch-1115-follow-ups #%d' % n] = 'TNT and Portal'
for n in (3, 4, 5, 6, 7, 8, 9, 21):
    REFILE['tooling/scratch-1115-follow-ups #%d' % n] = 'Seasoning'
for n in (10, 20, 22):
    REFILE['tooling/scratch-1115-follow-ups #%d' % n] = 'Misc Gameplay'
for n in (11, 12, 13, 17, 18):
    REFILE['tooling/scratch-1115-follow-ups #%d' % n] = 'Hearth and Harvest'   # Power Grid rows already sit there
REFILE.update({'tooling/scratch-1115-follow-ups #14': 'Wild Crops', 'tooling/scratch-1115-follow-ups #15': 'Placeable Food',
               'tooling/scratch-1115-follow-ups #16': 'Biomes', 'tooling/scratch-1115-follow-ups #19': 'Biomes',
               'tooling/scratch-1115-follow-ups #23': 'BlockRunner', 'tooling/scratch-1115-follow-ups #24': 'Interface'})
for n in range(1, 12):
    REFILE['tooling/scratch-1115-follow-ups-2 #%d' % n] = 'BlockRunner'
REFILE.update({'tooling/scratch-1115-follow-ups-2 #12': 'Seasoning', 'tooling/scratch-1115-follow-ups-2 #13': 'Seasoning',
               'tooling/scratch-1115-follow-ups-2 #14': 'Hearth and Harvest',
               'tooling/scratch-1115-follow-ups-2 #15': 'Biomes', 'tooling/scratch-1115-follow-ups-2 #16': 'Creative Split'})
HP_RE = re.compile(r'\*\*High priority \([^)]*\)\.\*\*\s*')
STATUS_WORDS = re.compile(r'\b(BUILT|Built|built|Swept|Run r|r1\d{3}|Done|DONE|Closed|PASS|FAIL|installed|'
                          r'Status|Fixed|fixed|Not run)\b')
LEAD_BOLD = re.compile(r'^\*\*(.+?)\*\*\s*')
DATE_RE = re.compile(r'(?<!\d)(20\d\d-\d\d-\d\d)(?!\d)')
QROW_RE = re.compile(r'\b((?:SQ|CQ)-[A-Z][A-Z0-9]*-[0-9A-Za-z]+)\b')
LINK_RE = re.compile(r'\]\(\.\./([^)#]+\.md)')


def cells(line):
    return [c.strip().replace('\\|', '|') for c in re.split(r'(?<!\\)\|', line.strip())[1:-1]]


def split_details(d):
    """(details without leading status prose, [moved segments], priority)."""
    pri = ''
    if HP_RE.search(d):
        pri = 'high'
        d = HP_RE.sub('', d, count=1).strip()
    moved = []
    while True:
        m = LEAD_BOLD.match(d)
        if not m or not STATUS_WORDS.search(m.group(1)):
            break
        moved.append(m.group(1).strip())
        d = d[m.end():]
    return d.strip(), moved, pri


def feature_map():
    out = {}
    for f in table_io.read_dicts(tr.FW / 'status' / 'features.csv'):
        for p in f['file'].split(','):
            out.setdefault(p.strip(), f['key'])
    return out


def migrate(text):
    lines = text.split('\n')
    layout, rows = [], []
    cat = fam = None
    in_archive = False
    skip_section = False
    i = 0
    plan_rows = []
    while i < len(lines):
        l = lines[i]
        if l.startswith('# ') and i > 0:
            cat = l[2:].strip()
            in_archive = cat == 'archive'
            fam = None
            skip_section = False
        elif l.startswith('## '):
            fam = l[3:].strip()
            skip_section = fam in PLAN_TABLES
        if l.startswith('<!-- generated:status_index begin todo'):
            while not lines[i].startswith('<!-- generated:status_index end todo'):
                i += 1
            layout += ['<?todo snapshot?>']
            i += 1
            continue
        if l == ROW_HEAD or (in_archive and l == ARCH_HEAD):
            i += 2
            found = []
            while i < len(lines) and lines[i].startswith('| `'):
                c = cells(lines[i])
                if l == ROW_HEAD or len(c) >= 8 or (len(c) > 2 and c[2] in tr.STATUS_ORDER):
                    rid, change, status, tests, work, details, decisions, writeup = (c + [''] * 8)[:8]
                else:
                    rid, change, work, details, decisions, writeup = (c + [''] * 6)[:6]
                    status, tests = 'Complete', ''
                found.append(dict(id=rid.strip('`'), change=change, status=status, tests=tests, work=work,
                                  details=details, decisions=decisions, writeup=writeup))
                i += 1
            for r in found:
                if in_archive:
                    r.update(family='', _file=tr.ARCHIVE_FILE)
                elif skip_section:
                    r.update(family='', _file='', plan=PLAN_TABLES[fam], _plan_table=fam)
                    plan_rows.append(r)
                else:
                    r.update(family=fam, _file=tr.slug(cat) + '.csv')
                rows.append(r)
            if in_archive:
                layout.append('<?todo archive?>')
            elif not skip_section:
                if fam in tr.families_in_layout('\n'.join(layout)):
                    raise SystemExit('family %r has two row tables' % fam)
                layout.append('<?todo rows %s?>' % fam)
            continue
        if not skip_section:
            layout.append(l)
        i += 1
    return layout, rows, plan_rows


def refile(rows, plan_rows):
    by_prefix, by_writeup = defaultdict(Counter), defaultdict(Counter)
    fam_file = {}
    for r in rows:
        if r.get('family'):
            by_prefix[tr.id_parts(r['id'])[0]][r['family']] += 1
            for w in LINK_RE.findall(r.get('writeup', '')):
                by_writeup[w][r['family']] += 1
            fam_file[r['family']] = r['_file']
    report = []
    for r in plan_rows:
        pre = tr.id_parts(r['id'])[0]
        target, how = REFILE.get(r['id']), 'by hand'
        if not target and by_prefix[pre]:
            target, how = by_prefix[pre].most_common(1)[0][0], 'id prefix'
        if not target:
            for w in LINK_RE.findall(r.get('writeup', '') + ' ' + r.get('details', '')):
                if by_writeup[w]:
                    target, how = by_writeup[w].most_common(1)[0][0], 'writeup %s' % w
                    break
        if not target:
            target, how = 'Process', 'fallback'
        r['family'], r['_file'] = target, fam_file[target]
        report.append('%s (%s) -> %s [%s]' % (r['id'], r.pop('_plan_table'), target, how))
    return report


def enrich(rows):
    fmap = feature_map()
    moved_n = Counter()
    for r in rows:
        d, moved, pri = split_details(r['details'])
        r['details'] = d
        r['priority'] = pri
        r['log'] = '; '.join(moved)
        moved_n['rows with status prose moved'] += bool(moved)
        if not r.get('plan'):
            plans = re.findall(r'\b(plan_[a-z0-9_.]+?\.md)', ' '.join(moved))
            r['plan'] = plans[0] if plans and (tr.FW / plans[0]).exists() else ''
        text = ' '.join(r.get(k, '') for k in ('change', 'details', 'decisions', 'writeup', 'log'))
        links = LINK_RE.findall(r.get('writeup', ''))
        r['feature'] = next((fmap[w] for w in links if w in fmap), '')
        r['queue'] = ', '.join(sorted(set(QROW_RE.findall(text))))
        src = re.search(r'scratchpad (20\d\d-\d\d-\d\d)', r.get('writeup', '') + ' ' + r.get('details', ''))
        r['source'] = 'scratchpad %s' % src.group(1) if src else ''
        dates = [x for x in DATE_RE.findall(text) if x <= '2026-11-16']
        r['updated'] = max(dates) if dates else ''
        r.setdefault('table', '')
        moved_n['high priority'] += pri == 'high'
    return moved_n


HEAD_EDITS = [
    (re.compile(r'^\*\*Priorities \(scratchpad 2026-10-21\):\*\*.*?(?=\n\n)', re.S | re.M), ''),
    (re.compile(r'^\*\*Scratch test queue:\*\*.*?(?=\n\n)', re.S | re.M),
     '**Scratch test queue:** [testing/scratch_queue.md](../testing/scratch_queue.md), rendered from '
     '`testing/rows/*.qb`; its coverage block is generated. A row\'s `Tests` cell (see [Test tiers](#test-tiers)) says '
     'which tiers are still open; its `queue` field names the queue rows that verify it.'),
    (re.compile(r'\*\*patcherMod\*\* \(the in-tree Forge mod\)'),
     '**Creative Common** (the core jar; patcherMod before 2026-11-04)'),
]


def edit_layout(layout):
    text = '\n'.join(layout)
    for rx, rep in HEAD_EDITS:
        text, n = rx.subn(rep, text, count=1)
        if not n:
            print('WARN head edit not applied: %s' % rx.pattern[:60])
    for fam in PLAN_TABLES:
        text = re.sub(r'^\s*- \[%s\]\(#[^)]*\)\n' % re.escape(fam), '', text, flags=re.M)
    text = text.replace('<?todo snapshot?>', '<?todo snapshot?>\n\n## High priority\n\nOpen rows with '
                        '`priority=high` (owner ruling D13), from every family:\n\n<?todo high_priority?>', 1)
    text = re.sub(r'\n{3,}', '\n\n', text)
    head = ('<!-- Layout of TODO.md: headings and prose with <?todo ...?> placeholders (tools/todo_rows.py). Rows are '
            'in rows/*.csv; tools/status_index.py renders TODO.md. -->')
    return head + '\n' + text


def check(old_text, rows, layout_text):
    new = tr.render(rows, layout_text).split('\n')
    def rowmap(lines):
        out = {}
        for l in lines:
            if l.startswith('| `'):
                c = cells(l)
                rid = c[0].strip('`')
                out.setdefault(rid, []).append(c)
        return out
    old_m = rowmap(old_text.split('\n'))
    new_m = rowmap(new)
    probs = []
    old_ids = {k for k, v in old_m.items() for c in v if len(c) >= 6}
    for rid in sorted(old_ids):
        if rid not in new_m:
            probs.append('%s: missing after render' % rid)
            continue
        for oc in old_m[rid]:
            if len(oc) < 6:
                continue
            eight = len(oc) >= 8 or oc[2] in tr.STATUS_ORDER
            o = dict(zip(['id', 'change', 'status', 'tests', 'work', 'details', 'decisions', 'writeup'], oc + [''] * 8)) \
                if eight else dict(zip(['id', 'change', 'work', 'details', 'decisions', 'writeup'], oc))
            nc = new_m[rid][0]
            n = dict(zip(['id', 'change', 'status', 'tests', 'work', 'details', 'decisions', 'writeup'], nc)) \
                if len(nc) >= 8 else dict(zip(['id', 'change', 'work', 'details', 'decisions', 'writeup'], nc))
            for k in ('change', 'work', 'decisions', 'writeup') + (('status', 'tests') if eight and len(nc) >= 8 else ()):
                if o.get(k, '') != n.get(k, ''):
                    probs.append('%s: %s differs' % (rid, k))
            ow = Counter(re.findall(r'\w+', HP_RE.sub('', o['details'])))
            nw = Counter(re.findall(r'\w+', n['details']))
            if ow - nw:
                probs.append('%s: details lost words %s' % (rid, list((ow - nw).elements())[:5]))
    for rid in set(new_m) - set(old_m):
        probs.append('%s: new after render' % rid)
    return probs


def main(argv):
    dry = '--dry' in argv
    old = tr.TODO_MD.read_text()
    layout, rows, plan_rows = migrate(old)
    report = refile(rows, plan_rows)
    counts = enrich(rows)
    layout_text = edit_layout(layout)
    probs = check(old, rows, layout_text)
    errs = tr.validate(rows, layout_text)
    files = Counter(r['_file'] for r in rows)
    print('rows %d in %d files; %s' % (len(rows), len(files), dict(counts)))
    print('refiled %d plan-table rows:' % len(report))
    for x in report:
        print('  ' + x)
    for p in probs[:30]:
        print('ROUNDTRIP ' + p)
    print('round trip problems: %d; schema problems: %d' % (len(probs), len(errs)))
    for e in errs[:20]:
        print('SCHEMA %s:%s %s %s' % e)
    if dry or probs:
        return 1 if probs else 0
    tr.ROWS_DIR.mkdir(exist_ok=True)
    tr.save_all(rows)
    tr.LAYOUT.write_text(layout_text)
    print('wrote %s and %s' % (tr.ROWS_DIR.relative_to(ROOT), tr.LAYOUT.relative_to(ROOT)))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
